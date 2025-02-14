package com.example.beatapp.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.example.beatapp.service.MusicPlayerService

class BroadcastManager(
    private val context: Context,
    private val onNext: () -> Unit,
    private val onPrevious: () -> Unit,
    private val onTrackStateChanged: (Boolean) -> Unit
) {
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            if (ctx == null || intent == null) return
            when (intent.action) {
                "com.example.beatapp.ACTION_NEXT" -> onNext()
                "com.example.beatapp.ACTION_PREVIOUS" -> onPrevious()
                MusicPlayerService.ACTION_TRACK_STATE_CHANGED -> {
                    val playing = intent.getBooleanExtra(MusicPlayerService.EXTRA_IS_PLAYING, false)
                    onTrackStateChanged(playing)
                }
            }
        }
    }

    fun register() {
        val filter = IntentFilter().apply {
            addAction("com.example.beatapp.ACTION_NEXT")
            addAction("com.example.beatapp.ACTION_PREVIOUS")
            addAction(MusicPlayerService.ACTION_TRACK_STATE_CHANGED)
        }
        context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
    }

    fun unregister() {
        context.unregisterReceiver(receiver)
    }
}
