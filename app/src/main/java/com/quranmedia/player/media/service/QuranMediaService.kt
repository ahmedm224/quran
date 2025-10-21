package com.quranmedia.player.media.service

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.quranmedia.player.media.player.QuranPlayer
import com.quranmedia.player.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class QuranMediaService : MediaSessionService() {

    @Inject
    lateinit var quranPlayer: QuranPlayer

    private var mediaSession: MediaSession? = null

    companion object {
        const val CUSTOM_COMMAND_NEXT_AYAH = "NEXT_AYAH"
        const val CUSTOM_COMMAND_PREVIOUS_AYAH = "PREVIOUS_AYAH"
        const val CUSTOM_COMMAND_TOGGLE_LOOP = "TOGGLE_LOOP"
        const val CUSTOM_COMMAND_NUDGE_FORWARD = "NUDGE_FORWARD"
        const val CUSTOM_COMMAND_NUDGE_BACKWARD = "NUDGE_BACKWARD"
    }

    override fun onCreate() {
        super.onCreate()

        val sessionActivityPendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        mediaSession = MediaSession.Builder(this, quranPlayer.player)
            .setCallback(MediaSessionCallback())
            .setSessionActivity(sessionActivityPendingIntent)
            .build()

        Timber.d("QuranMediaService created with notification support")
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
        Timber.d("QuranMediaService destroyed")
    }

    private inner class MediaSessionCallback : MediaSession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            val sessionCommands = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
                .add(SessionCommand(CUSTOM_COMMAND_NEXT_AYAH, Bundle.EMPTY))
                .add(SessionCommand(CUSTOM_COMMAND_PREVIOUS_AYAH, Bundle.EMPTY))
                .add(SessionCommand(CUSTOM_COMMAND_TOGGLE_LOOP, Bundle.EMPTY))
                .add(SessionCommand(CUSTOM_COMMAND_NUDGE_FORWARD, Bundle.EMPTY))
                .add(SessionCommand(CUSTOM_COMMAND_NUDGE_BACKWARD, Bundle.EMPTY))
                .build()

            val playerCommands = MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS.buildUpon()
                .build()

            return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                .setAvailableSessionCommands(sessionCommands)
                .setAvailablePlayerCommands(playerCommands)
                .build()
        }

        override fun onPlaybackResumption(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo
        ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
            return Futures.immediateFuture(
                MediaSession.MediaItemsWithStartPosition(emptyList(), 0, 0)
            )
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            when (customCommand.customAction) {
                CUSTOM_COMMAND_NEXT_AYAH -> {
                    Timber.d("Next ayah command")
                }
                CUSTOM_COMMAND_PREVIOUS_AYAH -> {
                    Timber.d("Previous ayah command")
                }
                CUSTOM_COMMAND_TOGGLE_LOOP -> {
                    Timber.d("Toggle loop command")
                }
                CUSTOM_COMMAND_NUDGE_FORWARD -> {
                    val incrementMs = args.getLong("incrementMs", 250)
                    quranPlayer.nudgeForward(incrementMs)
                }
                CUSTOM_COMMAND_NUDGE_BACKWARD -> {
                    val incrementMs = args.getLong("incrementMs", 250)
                    quranPlayer.nudgeBackward(incrementMs)
                }
            }
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }

        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>
        ): ListenableFuture<MutableList<MediaItem>> {
            val updatedMediaItems = mediaItems.map { mediaItem ->
                if (mediaItem.localConfiguration?.uri != null) {
                    mediaItem
                } else {
                    mediaItem.buildUpon()
                        .setUri(mediaItem.requestMetadata.mediaUri)
                        .build()
                }
            }.toMutableList()

            return Futures.immediateFuture(updatedMediaItems)
        }
    }
}
