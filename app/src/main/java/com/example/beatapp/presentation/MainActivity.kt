package com.example.beatapp.presentation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import com.example.beatapp.presentation.ui.BeatAppNavGraph
import com.example.beatapp.presentation.ui.theme.BeatAppTheme
import com.example.beatapp.presentation.viewmodel.PlayerViewModel
import com.example.beatapp.util.BroadcastManager
import com.example.beatapp.util.PlayerIntentBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class  MainActivity : ComponentActivity() {

    private lateinit var broadcastManager: BroadcastManager
    private lateinit var playerViewModel: PlayerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        playerViewModel = ViewModelProvider(this)[PlayerViewModel::class.java]

        broadcastManager = BroadcastManager(
            context = this,
            onNext = {
                playerViewModel.nextTrack()
                notifyService(this)
            },
            onPrevious = {
                playerViewModel.previousTrack()
                notifyService(this)
            },
            onTrackStateChanged = { playing ->
                playerViewModel.setPlaying(playing)
            }
        )
        broadcastManager.register()

        setContent {
            BeatAppTheme {
                BeatAppNavGraph(playerViewModel = playerViewModel)
            }
        }

        val permissionsToRequest = mutableListOf<String>().apply {
            if (ContextCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.POST_NOTIFICATIONS
                )
                != PackageManager.PERMISSION_GRANTED
            ) add(Manifest.permission.POST_NOTIFICATIONS)
            if (ContextCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.READ_MEDIA_AUDIO
                )
                != PackageManager.PERMISSION_GRANTED
            ) add(Manifest.permission.READ_MEDIA_AUDIO)
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), 100)
        }
    }

    override fun onDestroy() {
        broadcastManager.unregister()
        super.onDestroy()
    }

    private fun notifyService(context: Context) {
        playerViewModel.track.value?.let { track ->
            val serviceIntent = PlayerIntentBuilder.buildPlayIntent(context, track)
            context.startForegroundService(serviceIntent)
        }
    }
}
