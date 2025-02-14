package com.example.beatapp.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media.session.MediaButtonReceiver
import androidx.media3.exoplayer.ExoPlayer
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.example.beatapp.R
import com.example.beatapp.domain.model.Track
import com.example.beatapp.presentation.MainActivity
import com.example.beatapp.service.MusicPlayerService

object NotificationHelper {


    fun createNotification(
        context: Context,
        exoPlayer: ExoPlayer,
        track: Track? = null,
        sessionToken: MediaSessionCompat.Token? = null
    ): Notification {
        val channel = NotificationChannel(
            MusicPlayerService.CHANNEL_ID,
            "Music Player",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        val contentIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        var largeIcon: Bitmap? = null
        track?.coverUrl?.let { coverUrl ->
            try {
                largeIcon = Glide.with(context)
                    .asBitmap()
                    .load(coverUrl)
                    .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .get()
            } catch (_: Exception) { }
        }

        val contentTitle = track?.title ?: context.getString(R.string.unknown_title)
        val contentText = track?.artistName ?: context.getString(R.string.unknown_artist)

        val skipPrevAction = NotificationCompat.Action(
            R.drawable.ic_previous,
            context.getString(R.string.previous),
            MediaButtonReceiver.buildMediaButtonPendingIntent(
                context,
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
            )
        )

        val playPauseAction = NotificationCompat.Action(
            if (exoPlayer.isPlaying) R.drawable.ic_pause else R.drawable.ic_play,
            context.getString(R.string.play_pause),
            MediaButtonReceiver.buildMediaButtonPendingIntent(
                context,
                if (exoPlayer.isPlaying) PlaybackStateCompat.ACTION_PAUSE else PlaybackStateCompat.ACTION_PLAY
            )
        )

        val skipNextAction = NotificationCompat.Action(
            R.drawable.ic_next,
            context.getString(R.string.next),
            MediaButtonReceiver.buildMediaButtonPendingIntent(
                context,
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT
            )
        )

        return NotificationCompat.Builder(context, MusicPlayerService.CHANNEL_ID)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_logo)
            .setLargeIcon(largeIcon)
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(skipPrevAction)
            .addAction(playPauseAction)
            .addAction(skipNextAction)
            .setStyle(
                MediaStyle()
                    .setMediaSession(sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
                    .setShowCancelButton(true)
                    .setCancelButtonIntent(
                        PendingIntent.getService(
                            context, 0,
                            Intent(context, MusicPlayerService::class.java),
                            PendingIntent.FLAG_IMMUTABLE
                        )
                    )
            )
            .build()
    }

    fun updateNotification(
        context: Context,
        exoPlayer: ExoPlayer,
        track: Track? = null,
        sessionToken: MediaSessionCompat.Token? = null
    ) {
        val notification = createNotification(context, exoPlayer, track, sessionToken)
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(MusicPlayerService.NOTIFICATION_ID, notification)
    }
}
