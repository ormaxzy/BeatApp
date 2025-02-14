package com.example.beatapp.util

import android.content.Context
import android.content.Intent
import com.example.beatapp.domain.model.Track
import com.example.beatapp.service.MusicPlayerService

object PlayerIntentBuilder {

    fun buildPlayIntent(context: Context, track: Track? = null): Intent {
        return Intent(context, MusicPlayerService::class.java).apply {
            action = MusicPlayerService.ACTION_PLAY
            track?.let {
                putExtra(MusicPlayerService.EXTRA_TRACK_URL, it.preview)
                putExtra("EXTRA_TRACK_TITLE", it.title)
                putExtra("EXTRA_TRACK_ARTIST", it.artistName)
                putExtra("EXTRA_TRACK_COVER_URL", it.coverUrl)
                putExtra("EXTRA_TRACK_DURATION", it.duration)
            }
        }
    }

    fun buildPauseIntent(context: Context): Intent {
        return Intent(context, MusicPlayerService::class.java).apply {
            action = MusicPlayerService.ACTION_PAUSE
        }
    }

    fun buildSeekToIntent(context: Context, position: Long): Intent {
        return Intent(context, MusicPlayerService::class.java).apply {
            action = MusicPlayerService.ACTION_SEEK_TO
            putExtra("EXTRA_SEEK_POSITION", position)
        }
    }
}
