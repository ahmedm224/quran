package com.quranmedia.player.download

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.quranmedia.player.data.database.dao.AudioVariantDao
import com.quranmedia.player.data.database.dao.DownloadTaskDao
import com.quranmedia.player.data.database.entity.DownloadStatus
import com.quranmedia.player.data.database.entity.DownloadTaskEntity
import com.quranmedia.player.data.database.entity.toDomainModel
import com.quranmedia.player.data.repository.SettingsRepository
import com.quranmedia.player.domain.model.AudioVariant
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val workManager: WorkManager,
    private val downloadTaskDao: DownloadTaskDao,
    private val audioVariantDao: AudioVariantDao,
    private val settingsRepository: SettingsRepository
) {

    fun getAllDownloads(): Flow<List<DownloadTaskEntity>> {
        return downloadTaskDao.getAllDownloadTasks()
    }

    fun getDownloadsByStatus(status: DownloadStatus): Flow<List<DownloadTaskEntity>> {
        return downloadTaskDao.getDownloadTasksByStatus(status.name)
    }

    suspend fun downloadAudio(
        reciterId: String,
        surahNumber: Int,
        audioVariant: AudioVariant
    ): String {
        // Check if already downloaded
        val existingTask = downloadTaskDao.getDownloadTaskForSurah(reciterId, surahNumber)
        if (existingTask?.status == DownloadStatus.COMPLETED.name) {
            Timber.d("Audio already downloaded for surah $surahNumber")
            return existingTask.id
        }

        // Create download directory
        val downloadDir = File(context.getExternalFilesDir(null), "quran/audio/$reciterId")
        downloadDir.mkdirs()

        val destinationPath = File(downloadDir, "surah_${surahNumber}.${audioVariant.format.name.lowercase()}").absolutePath

        // Create download task
        val taskId = java.util.UUID.randomUUID().toString()
        val task = DownloadTaskEntity(
            id = taskId,
            audioVariantId = audioVariant.id,
            reciterId = reciterId,
            surahNumber = surahNumber,
            status = DownloadStatus.PENDING.name,
            bytesTotal = audioVariant.fileSizeBytes ?: 0L
        )

        downloadTaskDao.insertDownloadTask(task)

        // Get settings for WiFi-only option
        val settings = settingsRepository.settings.first()
        val wifiOnly = settings.wifiOnlyDownloads

        // Create work request
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED)
            .setRequiresStorageNotLow(true)
            .build()

        val workData = workDataOf(
            DownloadWorker.KEY_DOWNLOAD_TASK_ID to taskId,
            DownloadWorker.KEY_AUDIO_VARIANT_ID to audioVariant.id,
            DownloadWorker.KEY_URL to audioVariant.url,
            DownloadWorker.KEY_DESTINATION_PATH to destinationPath
        )

        val downloadWork = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setConstraints(constraints)
            .setInputData(workData)
            .addTag("download_$taskId")
            .build()

        workManager.enqueue(downloadWork)

        Timber.d("Download queued for surah $surahNumber, task: $taskId")
        return taskId
    }

    suspend fun pauseDownload(taskId: String) {
        workManager.cancelAllWorkByTag("download_$taskId")
        val task = downloadTaskDao.getDownloadTaskById(taskId)
        task?.let {
            downloadTaskDao.updateDownloadTask(
                it.copy(
                    status = DownloadStatus.PAUSED.name,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
        Timber.d("Download paused: $taskId")
    }

    suspend fun resumeDownload(taskId: String) {
        val task = downloadTaskDao.getDownloadTaskById(taskId) ?: return
        val audioVariant = audioVariantDao.getAudioVariantById(task.audioVariantId) ?: return

        downloadAudio(task.reciterId, task.surahNumber, audioVariant.toDomainModel())
    }

    suspend fun cancelDownload(taskId: String) {
        workManager.cancelAllWorkByTag("download_$taskId")
        downloadTaskDao.deleteDownloadTask(taskId)
        Timber.d("Download cancelled: $taskId")
    }

    suspend fun deleteDownloadedAudio(reciterId: String, surahNumber: Int) {
        val task = downloadTaskDao.getDownloadTaskForSurah(reciterId, surahNumber)
        task?.let {
            val audioVariant = audioVariantDao.getAudioVariantById(it.audioVariantId)
            audioVariant?.localPath?.let { path ->
                val file = File(path)
                if (file.exists()) {
                    file.delete()
                    Timber.d("Deleted audio file: $path")
                }

                // Update audio variant
                audioVariantDao.insertAudioVariant(audioVariant.copy(localPath = null))
            }

            downloadTaskDao.deleteDownloadTask(it.id)
        }
    }
}
