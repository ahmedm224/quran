package com.quranmedia.player.presentation.screens.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quranmedia.player.data.repository.BookmarkRepository
import com.quranmedia.player.data.repository.SettingsRepository
import com.quranmedia.player.domain.repository.QuranRepository
import com.quranmedia.player.media.controller.PlaybackController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playbackController: PlaybackController,
    private val quranRepository: QuranRepository,
    private val settingsRepository: SettingsRepository,
    private val bookmarkRepository: BookmarkRepository
) : ViewModel() {

    val playbackState = playbackController.playbackState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = playbackController.playbackState.value
        )

    suspend fun getSavedPosition(reciterId: String, surahNumber: Int): Long? {
        return try {
            val settings = settingsRepository.settings.first()
            if (settings.lastReciterId == reciterId && settings.lastSurahNumber == surahNumber) {
                Timber.d("Found saved position: ${settings.lastPositionMs}ms")
                settings.lastPositionMs
            } else {
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting saved position")
            null
        }
    }

    fun loadAudio(reciterId: String, surahNumber: Int, resumePosition: Long? = null, startFromAyah: Int? = null) {
        viewModelScope.launch {
            try {
                val surah = quranRepository.getSurahByNumber(surahNumber)
                val reciter = quranRepository.getReciterById(reciterId)
                val audioVariants = quranRepository.getAudioVariants(reciterId, surahNumber).first()

                if (surah == null || reciter == null) {
                    Timber.e("Surah or reciter not found")
                    return@launch
                }

                if (audioVariants.isEmpty()) {
                    Timber.e("No audio variant found for reciter $reciterId, surah $surahNumber")
                    return@launch
                }

                // Get the first audio variant (there should typically be one per reciter/surah combo)
                val audioVariant = audioVariants.first()
                val audioUrl = audioVariant.url

                if (audioUrl.isBlank()) {
                    Timber.e("Audio URL is blank for reciter $reciterId, surah $surahNumber")
                    return@launch
                }

                Timber.d("Loading audio: $audioUrl, resumePosition=$resumePosition, startFromAyah=$startFromAyah")

                playbackController.playAudio(
                    reciterId = reciterId,
                    surahNumber = surahNumber,
                    audioUrl = audioUrl,
                    surahNameArabic = surah.nameArabic,
                    surahNameEnglish = surah.nameEnglish,
                    reciterName = reciter.name,
                    startFromAyah = startFromAyah,
                    startFromPositionMs = resumePosition
                )
            } catch (e: Exception) {
                Timber.e(e, "Error loading audio")
            }
        }
    }

    fun togglePlayPause() {
        if (playbackState.value.isPlaying) {
            playbackController.pause()
        } else {
            playbackController.play()
        }
    }

    fun seekTo(positionMs: Long) {
        playbackController.seekTo(positionMs)
    }

    fun nextAyah() {
        playbackController.nextAyah()
    }

    fun previousAyah() {
        playbackController.previousAyah()
    }

    fun nudgeForward() {
        playbackController.nudgeForward()
    }

    fun nudgeBackward() {
        playbackController.nudgeBackward()
    }

    fun setPlaybackSpeed(speed: Float) {
        viewModelScope.launch {
            playbackController.setPlaybackSpeed(speed)
        }
    }

    // Bookmark state
    private val _bookmarkSaved = MutableStateFlow(false)
    val bookmarkSaved = _bookmarkSaved.asStateFlow()

    fun createBookmark(label: String? = null) {
        viewModelScope.launch {
            try {
                val state = playbackState.value
                val reciterId = state.currentReciter
                val surahNumber = state.currentSurah
                val ayahNumber = state.currentAyah
                val positionMs = state.currentPosition

                if (reciterId != null && surahNumber != null && ayahNumber != null) {
                    val bookmarkId = bookmarkRepository.insertBookmark(
                        reciterId = reciterId,
                        surahNumber = surahNumber,
                        ayahNumber = ayahNumber,
                        positionMs = positionMs,
                        label = label
                    )
                    Timber.d("Bookmark created: $bookmarkId")
                    _bookmarkSaved.value = true
                    // Reset after a short delay
                    kotlinx.coroutines.delay(2000)
                    _bookmarkSaved.value = false
                } else {
                    Timber.e("Cannot create bookmark: missing playback state")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error creating bookmark")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Timber.d("PlayerViewModel cleared")
    }
}
