package com.quranmedia.player

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.quranmedia.player.data.worker.QuranDataPopulatorWorker
import com.quranmedia.player.data.worker.ReciterDataPopulatorWorker
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class QuranMediaApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Create notification channels
        createNotificationChannels()

        // Start Quran data population (runs once, automatically)
        startQuranDataPopulation()

        // Start reciter data population (API-based streaming)
        startReciterDataPopulation()

        Timber.d("QuranMediaApplication initialized - Workers enqueued")
    }

    private fun startQuranDataPopulation() {
        val workRequest = OneTimeWorkRequestBuilder<QuranDataPopulatorWorker>()
            .build()

        WorkManager.getInstance(this).enqueueUniqueWork(
            QuranDataPopulatorWorker.WORK_NAME,
            ExistingWorkPolicy.KEEP, // Only run if not already queued/running
            workRequest
        )

        Timber.d("Quran data population work enqueued")
    }

    private fun startReciterDataPopulation() {
        val workRequest = OneTimeWorkRequestBuilder<ReciterDataPopulatorWorker>()
            .build()

        WorkManager.getInstance(this).enqueueUniqueWork(
            ReciterDataPopulatorWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE, // Replace existing work to ensure fresh data
            workRequest
        )

        Timber.d("Reciter data population work enqueued")
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // Playback notification channel
            val playbackChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_PLAYBACK,
                getString(R.string.notification_channel_playback),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Media playback controls"
                setShowBadge(false)
            }

            // Download notification channel
            val downloadChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_DOWNLOADS,
                getString(R.string.notification_channel_downloads),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Download progress notifications"
                setShowBadge(false)
            }

            notificationManager?.createNotificationChannel(playbackChannel)
            notificationManager?.createNotificationChannel(downloadChannel)
        }
    }

    companion object {
        const val NOTIFICATION_CHANNEL_PLAYBACK = "playback_channel"
        const val NOTIFICATION_CHANNEL_DOWNLOADS = "downloads_channel"
    }
}
