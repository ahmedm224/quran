package com.quranmedia.player.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.quranmedia.player.data.api.AlQuranCloudApi
import com.quranmedia.player.data.database.dao.AudioVariantDao
import com.quranmedia.player.data.database.dao.ReciterDao
import com.quranmedia.player.data.database.entity.AudioVariantEntity
import com.quranmedia.player.data.database.entity.ReciterEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import timber.log.Timber

/**
 * Worker to download all audio reciters from Al-Quran Cloud API
 * and create audio variants for streaming playback
 */
@HiltWorker
class ReciterDataPopulatorWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val api: AlQuranCloudApi,
    private val reciterDao: ReciterDao,
    private val audioVariantDao: AudioVariantDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            Timber.d("Starting reciter data population from API")

            // List of reciters to exclude
            val excludedReciters = setOf(
                "ar.abdurrahmaansudais",
                "ar.ahmedajamy",
                "ar.alafasy",
                "zh.chinese",
                "fa.hedayatfarfooladvand",
                "ru.kuliev",
                "en.walk",
                "ur.khan"
            )

            // Delete excluded reciters if they exist
            for (excludedId in excludedReciters) {
                try {
                    reciterDao.deleteReciterById(excludedId)
                    Timber.d("Deleted excluded reciter: $excludedId")
                } catch (e: Exception) {
                    Timber.w(e, "Could not delete reciter: $excludedId")
                }
            }

            // Check if reciters already exist
            val existingReciterCount = reciterDao.getReciterCount()
            if (existingReciterCount > 0) {
                Timber.d("Reciter data already populated ($existingReciterCount reciters)")
                return Result.success()
            }

            // Fetch all audio editions (reciters) from API
            val response = api.getAudioEditions()

            if (response.code != 200 || response.status != "OK") {
                Timber.e("Failed to fetch audio editions: ${response.status}")
                return Result.retry()
            }

            Timber.d("Fetched ${response.data.size} audio editions from API")

            var reciterCount = 0
            var audioVariantCount = 0

            // Process each audio edition
            for (edition in response.data) {
                try {
                    // Skip excluded reciters
                    if (excludedReciters.any { edition.identifier.contains(it, ignoreCase = true) }) {
                        Timber.d("Skipping excluded reciter: ${edition.identifier}")
                        continue
                    }
                    // Insert reciter
                    val reciterEntity = ReciterEntity(
                        id = edition.identifier,
                        name = edition.englishName,
                        nameArabic = if (edition.language == "ar") edition.name else null,
                        style = edition.type ?: "Murattal",
                        version = "2024",
                        imageUrl = null
                    )
                    reciterDao.insertReciter(reciterEntity)
                    reciterCount++

                    // Create audio variants for all 114 Surahs using CDN URLs
                    for (surahNumber in 1..114) {
                        val audioUrl = buildCdnAudioUrl(
                            edition = edition.identifier,
                            surahNumber = surahNumber,
                            bitrate = 128 // Default bitrate
                        )

                        val audioVariant = AudioVariantEntity(
                            reciterId = edition.identifier,
                            surahNumber = surahNumber,
                            bitrate = 128,
                            format = "MP3",
                            url = audioUrl,
                            localPath = null,
                            durationMs = 0, // Duration not known from API
                            fileSizeBytes = null,
                            hash = null
                        )
                        audioVariantDao.insertAudioVariant(audioVariant)
                        audioVariantCount++
                    }

                    Timber.d("Added reciter: ${edition.englishName} (${edition.identifier})")

                    // Small delay to avoid overwhelming the database
                    delay(10)
                } catch (e: Exception) {
                    Timber.e(e, "Error adding reciter: ${edition.identifier}")
                    // Continue with next reciter
                }
            }

            Timber.d("Reciter data population complete: $reciterCount reciters, $audioVariantCount audio variants")
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Failed to populate reciter data")
            Result.retry()
        }
    }

    /**
     * Build CDN audio URL for streaming
     * Format: https://cdn.islamic.network/quran/audio-surah/{bitrate}/{edition}/{number}.mp3
     */
    private fun buildCdnAudioUrl(edition: String, surahNumber: Int, bitrate: Int = 128): String {
        return "https://cdn.islamic.network/quran/audio-surah/$bitrate/$edition/$surahNumber.mp3"
    }

    companion object {
        const val WORK_NAME = "reciter_data_populator"
    }
}
