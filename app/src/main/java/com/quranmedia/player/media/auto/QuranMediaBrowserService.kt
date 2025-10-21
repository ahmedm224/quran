package com.quranmedia.player.media.auto

import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import androidx.media.MediaBrowserServiceCompat
import com.quranmedia.player.domain.repository.QuranRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class QuranMediaBrowserService : MediaBrowserServiceCompat() {

    @Inject
    lateinit var quranRepository: QuranRepository

    @Inject
    lateinit var coroutineScope: CoroutineScope

    companion object {
        const val MEDIA_ROOT_ID = "root"
        const val MEDIA_RECITERS_ID = "reciters"
        const val MEDIA_SURAHS_ID = "surahs"
        const val MEDIA_RECITER_PREFIX = "reciter_"
        const val MEDIA_SURAH_PREFIX = "surah_"
        const val MEDIA_BOOKMARKS_ID = "bookmarks"
    }

    override fun onCreate() {
        super.onCreate()

        // Connect to the main media service session
        // This allows Android Auto to control the same playback session
        // Note: The session token will be set when the main service connects

        Timber.d("QuranMediaBrowserService created for Android Auto browsing")
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        Timber.d("onGetRoot: $clientPackageName")
        return BrowserRoot(MEDIA_ROOT_ID, null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        Timber.d("onLoadChildren: $parentId")

        result.detach()

        coroutineScope.launch {
            try {
                val mediaItems = when {
                    parentId == MEDIA_ROOT_ID -> getRootItems()
                    parentId == MEDIA_RECITERS_ID -> getRecitersItems()
                    parentId == MEDIA_SURAHS_ID -> getSurahsItems()
                    parentId.startsWith(MEDIA_RECITER_PREFIX) -> {
                        val reciterId = parentId.removePrefix(MEDIA_RECITER_PREFIX)
                        getReciterSurahsItems(reciterId)
                    }
                    else -> emptyList()
                }
                result.sendResult(mediaItems.toMutableList())
            } catch (e: Exception) {
                Timber.e(e, "Error loading children for $parentId")
                result.sendResult(mutableListOf())
            }
        }
    }

    private fun getRootItems(): List<MediaBrowserCompat.MediaItem> {
        return listOf(
            MediaBrowserCompat.MediaItem(
                MediaDescriptionCompat.Builder()
                    .setMediaId(MEDIA_RECITERS_ID)
                    .setTitle("Browse by Reciter")
                    .setSubtitle("Select a reciter")
                    .build(),
                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
            ),
            MediaBrowserCompat.MediaItem(
                MediaDescriptionCompat.Builder()
                    .setMediaId(MEDIA_SURAHS_ID)
                    .setTitle("Browse by Surah")
                    .setSubtitle("Select a surah")
                    .build(),
                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
            ),
            MediaBrowserCompat.MediaItem(
                MediaDescriptionCompat.Builder()
                    .setMediaId(MEDIA_BOOKMARKS_ID)
                    .setTitle("Bookmarks")
                    .setSubtitle("Your saved positions")
                    .build(),
                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
            )
        )
    }

    private suspend fun getRecitersItems(): List<MediaBrowserCompat.MediaItem> {
        val reciters = quranRepository.getAllReciters().first()
        return reciters.map { reciter ->
            MediaBrowserCompat.MediaItem(
                MediaDescriptionCompat.Builder()
                    .setMediaId("$MEDIA_RECITER_PREFIX${reciter.id}")
                    .setTitle(reciter.name)
                    .setSubtitle(reciter.nameArabic ?: "")
                    .build(),
                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
            )
        }
    }

    private suspend fun getSurahsItems(): List<MediaBrowserCompat.MediaItem> {
        val surahs = quranRepository.getAllSurahs().first()
        return surahs.map { surah ->
            MediaBrowserCompat.MediaItem(
                MediaDescriptionCompat.Builder()
                    .setMediaId("$MEDIA_SURAH_PREFIX${surah.number}")
                    .setTitle("${surah.number}. ${surah.nameEnglish}")
                    .setSubtitle("${surah.nameArabic} - ${surah.ayahCount} ayahs")
                    .build(),
                MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
            )
        }
    }

    private suspend fun getReciterSurahsItems(reciterId: String): List<MediaBrowserCompat.MediaItem> {
        val surahs = quranRepository.getAllSurahs().first()
        val reciter = quranRepository.getReciterById(reciterId)

        return surahs.map { surah ->
            MediaBrowserCompat.MediaItem(
                MediaDescriptionCompat.Builder()
                    .setMediaId("$MEDIA_RECITER_PREFIX$reciterId:$MEDIA_SURAH_PREFIX${surah.number}")
                    .setTitle(surah.nameEnglish)
                    .setSubtitle("${surah.nameArabic} - ${reciter?.name ?: ""}")
                    .build(),
                MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("QuranMediaBrowserService destroyed")
    }
}
