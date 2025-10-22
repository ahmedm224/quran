package com.quranmedia.player.media.controller

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import com.google.common.util.concurrent.ListenableFuture
import com.quranmedia.player.data.repository.SettingsRepository
import com.quranmedia.player.domain.model.AyahIndex
import com.quranmedia.player.domain.repository.QuranRepository
import com.quranmedia.player.media.model.PlaybackState
import com.quranmedia.player.media.service.QuranMediaService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaybackController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mediaControllerFuture: ListenableFuture<MediaController>,
    private val quranRepository: QuranRepository,
    private val settingsRepository: SettingsRepository,
    private val coroutineScope: CoroutineScope
) {
    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private var mediaController: MediaController? = null
    private var positionUpdateJob: Job? = null
    private var ayahIndices: List<AyahIndex> = emptyList()

    // App icon URI for artwork
    private val appIconUri: Uri by lazy {
        // Try to get the launcher icon resource ID
        val resourceId = try {
            context.packageManager.getApplicationInfo(context.packageName, 0).icon
        } catch (e: Exception) {
            Timber.e(e, "Failed to get app icon resource ID")
            0
        }

        val uri = if (resourceId != 0) {
            Uri.parse("android.resource://${context.packageName}/$resourceId")
        } else {
            // Fallback to a well-known resource path
            Uri.parse("android.resource://${context.packageName}/${android.R.drawable.ic_media_play}")
        }

        Timber.d("App icon URI for artwork: $uri")
        uri
    }

    init {
        mediaControllerFuture.addListener({
            mediaController = mediaControllerFuture.get()
            setupPlayerListener()
            startPositionUpdates()
        }, { it.run() })
    }

    private fun setupPlayerListener() {
        mediaController?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playbackState.value = _playbackState.value.copy(isPlaying = isPlaying)

                // Save position when pausing
                if (!isPlaying) {
                    coroutineScope.launch {
                        val currentReciter = _playbackState.value.currentReciter
                        val currentSurah = _playbackState.value.currentSurah
                        val currentAyah = _playbackState.value.currentAyah
                        if (currentReciter != null && currentSurah != null && currentAyah != null) {
                            settingsRepository.updateLastPlaybackState(currentReciter, currentSurah, currentAyah.toLong())
                            Timber.d("Saved on pause: reciter=$currentReciter, surah=$currentSurah, ayah=$currentAyah")
                        }
                    }
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                val stateString = when (playbackState) {
                    Player.STATE_IDLE -> "IDLE"
                    Player.STATE_BUFFERING -> "BUFFERING"
                    Player.STATE_READY -> "READY"
                    Player.STATE_ENDED -> "ENDED"
                    else -> "UNKNOWN"
                }
                Timber.d("Playback state changed to: $stateString")

                _playbackState.value = _playbackState.value.copy(
                    isBuffering = playbackState == Player.STATE_BUFFERING
                )

                // Check if playback ended and auto-play next surah
                if (playbackState == Player.STATE_ENDED) {
                    Timber.d("Playback ended, checking continuous playback...")
                    handlePlaybackEnded()
                }
            }

            override fun onMediaItemTransition(mediaItem: androidx.media3.common.MediaItem?, reason: Int) {
                // Update current ayah text when track changes
                mediaItem?.let { updateCurrentMediaItem(it) }
            }
        })
    }

    private fun handlePlaybackEnded() {
        coroutineScope.launch {
            try {
                // Check if continuous playback is enabled
                val isContinuousPlaybackEnabled = settingsRepository.settings.first().continuousPlaybackEnabled

                if (!isContinuousPlaybackEnabled) {
                    Timber.d("Playback ended. Continuous playback is disabled")
                    return@launch
                }

                val currentSurah = _playbackState.value.currentSurah ?: return@launch
                val currentReciter = _playbackState.value.currentReciter ?: return@launch

                // Move to next surah (1-114)
                val nextSurahNumber = currentSurah + 1

                if (nextSurahNumber > 114) {
                    Timber.d("Reached end of Quran. No more surahs to play")
                    return@launch
                }

                Timber.d("Auto-playing next surah: $nextSurahNumber")

                // Load next surah data
                val nextSurah = quranRepository.getSurahByNumber(nextSurahNumber)
                val reciter = quranRepository.getReciterById(currentReciter)
                val audioVariants = quranRepository.getAudioVariants(currentReciter, nextSurahNumber).first()

                if (nextSurah == null || reciter == null || audioVariants.isEmpty()) {
                    Timber.e("Failed to load next surah data")
                    return@launch
                }

                val audioVariant = audioVariants.first()
                playAudio(
                    reciterId = currentReciter,
                    surahNumber = nextSurahNumber,
                    audioUrl = audioVariant.url,
                    surahNameArabic = nextSurah.nameArabic,
                    surahNameEnglish = nextSurah.nameEnglish,
                    reciterName = reciter.name
                )
            } catch (e: Exception) {
                Timber.e(e, "Error auto-playing next surah")
            }
        }
    }

    private fun updateCurrentMediaItem(mediaItem: androidx.media3.common.MediaItem) {
        val metadata = mediaItem.mediaMetadata
        val extras = metadata.extras

        _playbackState.value = _playbackState.value.copy(
            currentAyahText = metadata.title?.toString(),
            currentAyah = extras?.getInt("ayahNumber"),
            totalAyahs = extras?.getInt("totalAyahs"),
            currentSurahNameArabic = extras?.getString("surahNameArabic"),
            currentSurahNameEnglish = extras?.getString("surahNameEnglish")
        )
    }

    private fun startPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = coroutineScope.launch(kotlinx.coroutines.Dispatchers.Main) {
            while (isActive) {
                mediaController?.let { controller ->
                    val position = controller.currentPosition
                    val duration = controller.duration

                    _playbackState.value = _playbackState.value.copy(
                        currentPosition = position,
                        duration = duration
                    )

                    // Update current ayah based on position
                    updateCurrentAyah(position)
                }
                delay(100) // Update every 100ms for smooth UI
            }
        }
    }

    private fun updateCurrentAyah(positionMs: Long) {
        val currentAyah = ayahIndices.find { ayah ->
            positionMs >= ayah.startMs && positionMs < ayah.endMs
        }

        if (currentAyah != null && currentAyah.ayahNumber != _playbackState.value.currentAyah) {
            _playbackState.value = _playbackState.value.copy(
                currentAyah = currentAyah.ayahNumber
            )
        }
    }

    suspend fun playAudio(
        reciterId: String,
        surahNumber: Int,
        audioUrl: String,
        surahNameArabic: String,
        surahNameEnglish: String,
        reciterName: String,
        startFromAyah: Int? = null,
        startFromPositionMs: Long? = null
    ) {
        try {
            // Ensure MediaController is ready
            if (mediaController == null) {
                Timber.e("MediaController not initialized yet")
                return
            }

            // Check if we have a downloaded file first (for offline playback)
            val audioVariants = quranRepository.getAudioVariants(reciterId, surahNumber).first()
            val downloadedVariant = audioVariants.firstOrNull { !it.localPath.isNullOrBlank() }

            if (downloadedVariant != null && downloadedVariant.localPath != null) {
                val localFile = java.io.File(downloadedVariant.localPath)
                if (localFile.exists()) {
                    Timber.d("Playing downloaded file: ${downloadedVariant.localPath}")
                    // Use Uri.fromFile() for proper local file handling
                    val fileUri = Uri.fromFile(localFile)
                    playSingleAudio(
                        audioUrl = fileUri.toString(),
                        surahNameArabic = surahNameArabic,
                        surahNameEnglish = surahNameEnglish,
                        reciterName = reciterName,
                        reciterId = reciterId,
                        surahNumber = surahNumber
                    )
                    return
                } else {
                    Timber.w("Downloaded file not found at: ${downloadedVariant.localPath}")
                }
            }

            // Fetch surah data with ayah audio URLs from API (on background thread)
            Timber.d("Fetching ayah audio URLs from API for reciter: $reciterId, surah: $surahNumber")
            val surahResponse = quranRepository.getSurahWithAudio(surahNumber, reciterId)

            if (surahResponse == null) {
                Timber.e("Failed to fetch surah data from API")
                // Fallback to single URL approach
                playSingleAudio(audioUrl, surahNameArabic, surahNameEnglish, reciterName, reciterId, surahNumber)
                return
            }

            val ayahs = surahResponse.ayahs
            if (ayahs.isEmpty()) {
                Timber.e("No ayahs found in surah data")
                return
            }

            // Build playlist of MediaItems (one per ayah)
            val mediaItems = ayahs.mapNotNull { ayah ->
                val ayahAudioUrl = ayah.audio
                if (ayahAudioUrl.isNullOrBlank()) {
                    Timber.w("Ayah ${ayah.numberInSurah} has no audio URL")
                    return@mapNotNull null
                }

                MediaItem.Builder()
                    .setMediaId("${reciterId}_${surahNumber}_${ayah.numberInSurah}")
                    .setUri(Uri.parse(ayahAudioUrl))
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(ayah.text)  // Arabic ayah text - will scroll in notifications and Android Auto
                            .setArtist(reciterName)
                            .setAlbumTitle("$surahNameEnglish - $surahNameArabic")
                            .setDisplayTitle(ayah.text)  // CRITICAL: Display title for Android Auto scrolling
                            .setSubtitle("$surahNameEnglish (${ayah.numberInSurah}/${ayahs.size})")
                            .setArtworkUri(appIconUri)  // App icon as artwork
                            .setExtras(Bundle().apply {
                                putString("surahNameArabic", surahNameArabic)
                                putString("surahNameEnglish", surahNameEnglish)
                                putInt("ayahNumber", ayah.numberInSurah)
                                putInt("totalAyahs", ayahs.size)
                                putString("ayahText", ayah.text)  // Store full ayah text for reference
                            })
                            .build()
                    )
                    .build()
            }

            if (mediaItems.isEmpty()) {
                Timber.e("No valid media items created from ayah data")
                return
            }

            Timber.d("Created playlist with ${mediaItems.size} ayahs for $surahNameEnglish")

            // All MediaController operations must run on main thread
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                mediaController?.apply {
                    // Set repeat mode to OFF so continuous playback can work
                    repeatMode = Player.REPEAT_MODE_OFF

                    setMediaItems(mediaItems)
                    prepare()

                    // Seek to specific ayah (either from parameter or resume position)
                    val targetAyah = if (startFromPositionMs != null && startFromPositionMs > 0) {
                        startFromPositionMs.toInt().also {
                            Timber.d("Resuming from ayah: $it")
                        }
                    } else {
                        startFromAyah
                    }

                    targetAyah?.let { ayahNum ->
                        val ayahIndex = ayahs.indexOfFirst { it.numberInSurah == ayahNum }
                        if (ayahIndex >= 0) {
                            seekToDefaultPosition(ayahIndex)
                            Timber.d("Starting from ayah index: $ayahIndex (ayah #$ayahNum)")
                        }
                    }

                    play()

                    _playbackState.value = _playbackState.value.copy(
                        currentReciter = reciterId,
                        currentReciterName = reciterName,
                        currentSurah = surahNumber,
                        currentSurahNameArabic = surahNameArabic,
                        currentSurahNameEnglish = surahNameEnglish,
                        currentAyah = startFromAyah ?: 1,
                        totalAyahs = ayahs.size
                    )

                    // Get first ayah to set initial text
                    if (mediaItems.isNotEmpty()) {
                        updateCurrentMediaItem(mediaItems[startFromAyah?.minus(1) ?: 0])
                    }

                    // Save last playback state
                    settingsRepository.updateLastPlaybackState(reciterId, surahNumber, currentPosition)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error playing audio")
        }
    }

    private suspend fun playSingleAudio(
        audioUrl: String,
        surahNameArabic: String,
        surahNameEnglish: String,
        reciterName: String,
        reciterId: String,
        surahNumber: Int
    ) {
        val mediaItem = MediaItem.Builder()
            .setUri(Uri.parse(audioUrl))
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(surahNameArabic)
                    .setArtist(reciterName)
                    .setAlbumTitle("Quran")
                    .setDisplayTitle(surahNameEnglish)
                    .setArtworkUri(appIconUri)  // App icon as artwork
                    .build()
            )
            .build()

        Timber.d("Playing single audio: $surahNameEnglish by $reciterName")

        // All MediaController operations must run on main thread
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
            mediaController?.apply {
                setMediaItem(mediaItem)
                prepare()
                play()

                _playbackState.value = _playbackState.value.copy(
                    currentReciter = reciterId,
                    currentSurah = surahNumber
                )
            }
        }
    }

    fun play() {
        mediaController?.play()
    }

    fun pause() {
        mediaController?.pause()
    }

    fun seekTo(positionMs: Long) {
        mediaController?.seekTo(positionMs)
    }

    fun seekToAyah(ayahNumber: Int) {
        mediaController?.let { controller ->
            // Each ayah is a separate media item in the playlist
            // Ayah numbers are 1-based, but media item indices are 0-based
            val ayahIndex = ayahNumber - 1

            if (ayahIndex >= 0 && ayahIndex < controller.mediaItemCount) {
                controller.seekToDefaultPosition(ayahIndex)
                Timber.d("Seeking to ayah $ayahNumber (media item index: $ayahIndex)")

                _playbackState.value = _playbackState.value.copy(currentAyah = ayahNumber)
            } else {
                Timber.e("Invalid ayah number: $ayahNumber (valid range: 1-${controller.mediaItemCount})")
            }
        }
    }

    fun nextAyah() {
        mediaController?.let { controller ->
            if (controller.hasNextMediaItem()) {
                controller.seekToNextMediaItem()
            }
        }
    }

    fun previousAyah() {
        mediaController?.let { controller ->
            if (controller.hasPreviousMediaItem()) {
                controller.seekToPreviousMediaItem()
            }
        }
    }

    fun nudgeForward() {
        coroutineScope.launch {
            try {
                val incrementMs = settingsRepository.settings.first().smallSeekIncrementMs
                mediaController?.let { controller ->
                    val currentPos = controller.currentPosition
                    val duration = controller.duration
                    if (duration > 0) {
                        val newPosition = (currentPos + incrementMs).coerceAtMost(duration)
                        controller.seekTo(newPosition)
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error nudging forward")
            }
        }
    }

    fun nudgeBackward() {
        coroutineScope.launch {
            try {
                val incrementMs = settingsRepository.settings.first().smallSeekIncrementMs
                mediaController?.let { controller ->
                    val currentPos = controller.currentPosition
                    val newPosition = (currentPos - incrementMs).coerceAtLeast(0)
                    controller.seekTo(newPosition)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error nudging backward")
            }
        }
    }

    suspend fun setPlaybackSpeed(speed: Float) {
        mediaController?.setPlaybackSpeed(speed)
        settingsRepository.setPlaybackSpeed(speed)
        _playbackState.value = _playbackState.value.copy(playbackSpeed = speed)
    }

    fun release() {
        positionUpdateJob?.cancel()
        MediaController.releaseFuture(mediaControllerFuture)
    }
}
