package com.quranmedia.player.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.quranmedia.player.data.api.AlQuranCloudApi
import com.quranmedia.player.data.api.model.AyahData
import com.quranmedia.player.data.database.dao.AyahDao
import com.quranmedia.player.data.database.dao.SurahDao
import com.quranmedia.player.data.database.entity.AyahEntity
import com.quranmedia.player.data.database.entity.SurahEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import timber.log.Timber

/**
 * Worker to download all 114 Surahs with Ayah data from Al-Quran Cloud API
 * and populate the local database
 */
@HiltWorker
class QuranDataPopulatorWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val api: AlQuranCloudApi,
    private val surahDao: SurahDao,
    private val ayahDao: AyahDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            Timber.d("Starting Quran data population")

            // Check if data already exists
            val existingAyahCount = ayahDao.getAyahCount()
            if (existingAyahCount >= 6236) {
                Timber.d("Quran data already populated (${existingAyahCount} ayahs)")
                return Result.success()
            }

            // Download all 114 Surahs
            for (surahNumber in 1..114) {
                try {
                    Timber.d("Downloading Surah $surahNumber/114")

                    val response = api.getSurahDefault(surahNumber)

                    if (response.code == 200 && response.status == "OK") {
                        val surahData = response.data

                        // Save Surah metadata
                        val surahEntity = SurahEntity(
                            number = surahData.number,
                            nameArabic = surahData.name,
                            nameEnglish = surahData.englishName,
                            nameTransliteration = surahData.englishNameTranslation,
                            ayahCount = surahData.numberOfAyahs,
                            revelationType = surahData.revelationType.uppercase()
                        )
                        surahDao.insertSurah(surahEntity)

                        // Save all Ayahs for this Surah
                        val ayahEntities = surahData.ayahs.map { ayahData ->
                            ayahData.toEntity(surahNumber)
                        }
                        ayahDao.insertAyahs(ayahEntities)

                        Timber.d("Saved Surah $surahNumber with ${ayahEntities.size} ayahs")

                        // Small delay to avoid overwhelming the API
                        delay(100)
                    } else {
                        Timber.e("Failed to download Surah $surahNumber: ${response.status}")
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error downloading Surah $surahNumber")
                    // Continue with next surah instead of failing completely
                }
            }

            val finalCount = ayahDao.getAyahCount()
            Timber.d("Quran data population complete: $finalCount ayahs in database")

            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Failed to populate Quran data")
            Result.retry()
        }
    }

    private fun AyahData.toEntity(surahNumber: Int): AyahEntity {
        return AyahEntity(
            surahNumber = surahNumber,
            ayahNumber = numberInSurah,
            globalAyahNumber = number,
            textArabic = text,
            juz = juz,
            manzil = manzil,
            page = page,
            ruku = ruku,
            hizbQuarter = hizbQuarter,
            sajda = when (sajda) {
                is Boolean -> sajda
                else -> false  // Handle sajda object case
            }
        )
    }

    companion object {
        const val WORK_NAME = "quran_data_populator"
    }
}
