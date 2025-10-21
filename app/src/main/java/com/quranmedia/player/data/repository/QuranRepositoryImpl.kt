package com.quranmedia.player.data.repository

import com.quranmedia.player.data.api.AlQuranCloudApi
import com.quranmedia.player.data.api.model.SurahData
import com.quranmedia.player.data.database.dao.AyahIndexDao
import com.quranmedia.player.data.database.dao.AudioVariantDao
import com.quranmedia.player.data.database.dao.BookmarkDao
import com.quranmedia.player.data.database.dao.ReciterDao
import com.quranmedia.player.data.database.dao.SurahDao
import com.quranmedia.player.data.database.entity.toDomainModel
import com.quranmedia.player.data.database.entity.toEntity
import com.quranmedia.player.domain.model.AyahIndex
import com.quranmedia.player.domain.model.AudioVariant
import com.quranmedia.player.domain.model.Bookmark
import com.quranmedia.player.domain.model.Reciter
import com.quranmedia.player.domain.model.Surah
import com.quranmedia.player.domain.repository.QuranRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuranRepositoryImpl @Inject constructor(
    private val reciterDao: ReciterDao,
    private val surahDao: SurahDao,
    private val audioVariantDao: AudioVariantDao,
    private val ayahIndexDao: AyahIndexDao,
    private val bookmarkDao: BookmarkDao,
    private val api: AlQuranCloudApi
) : QuranRepository {

    override fun getAllReciters(): Flow<List<Reciter>> {
        return reciterDao.getAllReciters().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun getReciterById(reciterId: String): Reciter? {
        return reciterDao.getReciterById(reciterId)?.toDomainModel()
    }

    override suspend fun insertReciters(reciters: List<Reciter>) {
        reciterDao.insertReciters(reciters.map { it.toEntity() })
    }

    override suspend fun getSurahWithAudio(surahNumber: Int, reciterId: String): SurahData? {
        return try {
            Timber.d("Fetching surah $surahNumber with audio from reciter $reciterId")
            val response = api.getSurah(surahNumber, reciterId)
            if (response.code == 200 && response.status == "OK") {
                response.data
            } else {
                Timber.e("API returned error: ${response.status}")
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch surah with audio from API")
            null
        }
    }

    override fun getAllSurahs(): Flow<List<Surah>> {
        return surahDao.getAllSurahs().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun getSurahByNumber(surahNumber: Int): Surah? {
        return surahDao.getSurahByNumber(surahNumber)?.toDomainModel()
    }

    override suspend fun insertSurahs(surahs: List<Surah>) {
        surahDao.insertSurahs(surahs.map { it.toEntity() })
    }

    override fun getAudioVariants(reciterId: String, surahNumber: Int): Flow<List<AudioVariant>> {
        return audioVariantDao.getAudioVariants(reciterId, surahNumber).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun getAudioVariant(reciterId: String, surahNumber: Int): AudioVariant? {
        return audioVariantDao.getAudioVariant(reciterId, surahNumber)?.toDomainModel()
    }

    override suspend fun insertAudioVariant(audioVariant: AudioVariant) {
        audioVariantDao.insertAudioVariant(audioVariant.toEntity())
    }

    override fun getAyahIndices(reciterId: String, surahNumber: Int): Flow<List<AyahIndex>> {
        return ayahIndexDao.getAyahIndices(reciterId, surahNumber).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun getAyahIndexAt(reciterId: String, surahNumber: Int, positionMs: Long): AyahIndex? {
        return ayahIndexDao.getAyahIndexAt(reciterId, surahNumber, positionMs)?.toDomainModel()
    }

    override suspend fun insertAyahIndices(indices: List<AyahIndex>) {
        ayahIndexDao.insertAyahIndices(indices.map { it.toEntity() })
    }

    override fun getAllBookmarks(): Flow<List<Bookmark>> {
        return bookmarkDao.getAllBookmarks().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun insertBookmark(bookmark: Bookmark) {
        bookmarkDao.insertBookmark(bookmark.toEntity())
    }

    override suspend fun deleteBookmark(bookmarkId: String) {
        bookmarkDao.deleteBookmark(bookmarkId)
    }

    override suspend fun searchSurahs(query: String): List<Surah> {
        return surahDao.searchSurahs(query).map { it.toDomainModel() }
    }

    override suspend fun searchReciters(query: String): List<Reciter> {
        return reciterDao.searchReciters(query).map { it.toDomainModel() }
    }
}
