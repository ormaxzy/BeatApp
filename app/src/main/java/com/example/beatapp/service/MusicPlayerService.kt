package com.example.beatapp.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.beatapp.R
import com.example.beatapp.domain.model.Track
import com.example.beatapp.util.NotificationHelper
import kotlinx.coroutines.*

class MusicPlayerService : Service() {

    companion object {
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "music_player_channel"

        const val ACTION_PLAY = "ACTION_PLAY"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_SKIP_FORWARD = "ACTION_SKIP_FORWARD"
        const val ACTION_SKIP_BACKWARD = "ACTION_SKIP_BACKWARD"
        const val ACTION_SEEK_TO = "ACTION_SEEK_TO"

        const val EXTRA_TRACK_URL = "TRACK_URL"

        const val ACTION_POSITION_CHANGED = "com.example.beatapp.POSITION_CHANGED"
        const val EXTRA_POSITION = "EXTRA_POSITION"
        const val EXTRA_DURATION = "EXTRA_DURATION"

        const val ACTION_TRACK_STATE_CHANGED = "com.example.beatapp.TRACK_STATE_CHANGED"
        const val EXTRA_IS_PLAYING = "EXTRA_IS_PLAYING"
        const val EXTRA_TITLE = "EXTRA_TITLE"
        const val EXTRA_ARTIST = "EXTRA_ARTIST"
        const val EXTRA_COVER_URL = "EXTRA_COVER_URL"
        const val EXTRA_ALBUM = "EXTRA_ALBUM"
        const val EXTRA_DURATION_SEC = "EXTRA_DURATION_SEC"
    }

    private lateinit var exoPlayer: ExoPlayer
    private lateinit var mediaSession: MediaSessionCompat
    private var currentTrack: Track? = null
    private var updateJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        exoPlayer = ExoPlayer.Builder(this).build()
        mediaSession = MediaSessionCompat(this, "MusicPlayerService").apply {
            isActive = true
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() {
                    if (!exoPlayer.isPlaying) {
                        exoPlayer.play()
                        broadcastTrackState()
                        updateSessionAndNotification()
                    }
                }
                override fun onPause() {
                    if (exoPlayer.isPlaying) {
                        exoPlayer.pause()
                        broadcastTrackState()
                        updateSessionAndNotification()
                    }
                }
                override fun onSkipToNext() {
                    sendBroadcast(Intent("com.example.beatapp.ACTION_NEXT"))
                }
                override fun onSkipToPrevious() {
                    sendBroadcast(Intent("com.example.beatapp.ACTION_PREVIOUS"))
                }
                override fun onSeekTo(pos: Long) {
                    exoPlayer.seekTo(pos)
                    updateSessionAndNotification()
                }
                override fun onFastForward() {
                    val newPos = (exoPlayer.currentPosition + 5000L).coerceAtMost(exoPlayer.duration)
                    exoPlayer.seekTo(newPos)
                    updateSessionAndNotification()
                }
                override fun onRewind() {
                    val newPos = (exoPlayer.currentPosition - 5000L).coerceAtLeast(0L)
                    exoPlayer.seekTo(newPos)
                    updateSessionAndNotification()
                }
            })
        }

        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateSessionAndNotification()
                if (isPlaying) startUpdatingNotification() else stopUpdatingNotification()
                broadcastTrackState()
            }
            override fun onPlaybackStateChanged(playbackState: Int) {
                updateSessionAndNotification()
                if (playbackState == Player.STATE_ENDED) {
                    sendBroadcast(Intent("com.example.beatapp.ACTION_NEXT"))
                }
            }
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(
            NOTIFICATION_ID,
            NotificationHelper.createNotification(
                context = this,
                exoPlayer = exoPlayer,
                track = currentTrack,
                sessionToken = mediaSession.sessionToken
            )
        )

        when (intent?.action) {
            ACTION_PLAY -> handlePlay(intent)
            ACTION_PAUSE -> mediaSession.controller.transportControls.pause()
            ACTION_SKIP_FORWARD -> mediaSession.controller.transportControls.fastForward()
            ACTION_SKIP_BACKWARD -> mediaSession.controller.transportControls.rewind()
            ACTION_SEEK_TO -> {
                val pos = intent.getLongExtra("EXTRA_SEEK_POSITION", 0L)
                mediaSession.controller.transportControls.seekTo(pos)
            }
        }
        return START_STICKY
    }

    private fun handlePlay(intent: Intent) {
        val newTrackUrl = intent.getStringExtra(EXTRA_TRACK_URL)
        if (newTrackUrl.isNullOrEmpty()) {
            mediaSession.controller.transportControls.play()
            return
        }
        if (currentTrack?.preview == newTrackUrl) {
            mediaSession.controller.transportControls.play()
            return
        }
        val title = intent.getStringExtra("EXTRA_TRACK_TITLE") ?: getString(R.string.unknown_title)
        val artist = intent.getStringExtra("EXTRA_TRACK_ARTIST") ?: getString(R.string.unknown_artist)
        val coverUrl = intent.getStringExtra("EXTRA_TRACK_COVER_URL") ?: ""
        val durationSec = intent.getIntExtra("EXTRA_TRACK_DURATION", 0)

        val newTrack = Track(
            id = 0L,
            title = title,
            preview = newTrackUrl,
            duration = durationSec,
            artistName = artist,
            albumTitle = "",
            coverUrl = coverUrl
        )
        currentTrack = newTrack

        exoPlayer.setMediaItem(MediaItem.fromUri(newTrack.preview))
        exoPlayer.prepare()
        exoPlayer.play()

        updateSessionAndNotification()
        broadcastTrackState()
    }


    private fun startUpdatingNotification() {
        updateJob?.cancel()
        updateJob = serviceScope.launch {
            while (exoPlayer.isPlaying) {
                updateSessionAndNotification()
                sendPositionBroadcast()
                delay(500)
            }
        }
    }

    private fun stopUpdatingNotification() {
        updateJob?.cancel()
        sendPositionBroadcast()
    }

    private fun updateSessionAndNotification() {
        updateMediaSessionMetadata()
        updateMediaSessionState()
        NotificationHelper.updateNotification(
            context = this,
            exoPlayer = exoPlayer,
            track = currentTrack,
            sessionToken = mediaSession.sessionToken
        )
    }

    private fun updateMediaSessionMetadata() {
        val builder = MediaMetadataCompat.Builder()
        currentTrack?.let { track ->
            builder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, track.title)
            builder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, track.artistName)
            val durMs = if (exoPlayer.duration > 0) exoPlayer.duration else track.duration * 1000L
            builder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, durMs)
        }
        mediaSession.setMetadata(builder.build())
    }

    private fun updateMediaSessionState() {
        val state = if (exoPlayer.isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
        val playbackState = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackStateCompat.ACTION_FAST_FORWARD or
                        PlaybackStateCompat.ACTION_REWIND or
                        PlaybackStateCompat.ACTION_SEEK_TO
            )
            .setState(state, exoPlayer.currentPosition, exoPlayer.playbackParameters.speed)
            .build()
        mediaSession.setPlaybackState(playbackState)
    }

    private fun sendPositionBroadcast() {
        Intent(ACTION_POSITION_CHANGED).apply {
            putExtra(EXTRA_POSITION, exoPlayer.currentPosition)
            putExtra(EXTRA_DURATION, exoPlayer.duration.coerceAtLeast(0))
        }.also { sendBroadcast(it) }
    }

    private fun broadcastTrackState() {
        Intent(ACTION_TRACK_STATE_CHANGED).apply {
            putExtra(EXTRA_IS_PLAYING, exoPlayer.isPlaying)
            currentTrack?.let { track ->
                putExtra(EXTRA_TITLE, track.title)
                putExtra(EXTRA_ARTIST, track.artistName)
                putExtra(EXTRA_ALBUM, track.albumTitle)
                putExtra(EXTRA_COVER_URL, track.coverUrl)
                putExtra(EXTRA_DURATION_SEC, track.duration)
            }
        }.also { sendBroadcast(it) }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        updateJob?.cancel()
        serviceScope.cancel()
        exoPlayer.release()
        mediaSession.release()
        super.onDestroy()
    }
}
