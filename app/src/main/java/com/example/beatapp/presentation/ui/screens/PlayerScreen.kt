package com.example.beatapp.presentation.ui.screens

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.beatapp.R
import com.example.beatapp.presentation.ui.theme.AppColors
import com.example.beatapp.presentation.viewmodel.PlayerViewModel
import com.example.beatapp.service.MusicPlayerService
import com.example.beatapp.util.PlayerIntentBuilder
import com.example.beatapp.util.formatTime

@Composable
fun PlayerScreen(
    playerViewModel: PlayerViewModel
) {
    val context = LocalContext.current
    val track by playerViewModel.track.collectAsState()
    val isPlaying by playerViewModel.isPlaying.collectAsState()

    var sliderPosition by remember { mutableLongStateOf(0L) }
    var trackDuration by remember { mutableLongStateOf(0L) }

    LaunchedEffect(track) {
        track?.let {
            sliderPosition = 0L
            trackDuration = it.duration * 1000L
            val serviceIntent = PlayerIntentBuilder.buildPlayIntent(context, it)
            context.startForegroundService(serviceIntent)
        }
    }

    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                if (intent.action == MusicPlayerService.ACTION_POSITION_CHANGED) {
                    sliderPosition = intent.getLongExtra(MusicPlayerService.EXTRA_POSITION, 0L)
                    trackDuration = intent.getLongExtra(MusicPlayerService.EXTRA_DURATION, 0L)
                }
            }
        }
        val filter = IntentFilter(MusicPlayerService.ACTION_POSITION_CHANGED)
        context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        onDispose { context.unregisterReceiver(receiver) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {
        if (track == null) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 46.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = track?.coverUrl.takeUnless { it.isNullOrBlank() },
                        placeholder = painterResource(id = R.drawable.ic_track_cover_placeholder),
                        error = painterResource(id = R.drawable.ic_track_cover_placeholder),
                        fallback = painterResource(id = R.drawable.ic_track_cover_placeholder),
                        contentDescription = track?.title.takeUnless { it.isNullOrBlank() }
                            ?: stringResource(R.string.unknown_title),
                        modifier = Modifier
                            .size(256.dp)
                            .clip(MaterialTheme.shapes.medium)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = track?.title.takeUnless { it.isNullOrBlank() }
                            ?: stringResource(R.string.unknown_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(
                            R.string.artist_label,
                            track?.artistName.takeUnless { it.isNullOrBlank() }
                                ?: stringResource(R.string.unknown_artist)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    if (!track?.albumTitle.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.album_label, track?.albumTitle ?: ""),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Column {
                    Slider(
                        value = sliderPosition.toFloat(),
                        onValueChange = { sliderPosition = it.toLong() },
                        onValueChangeFinished = { playerViewModel.seekTo(sliderPosition, context) },
                        valueRange = 0f..trackDuration.toFloat(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.secondary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = formatTime(sliderPosition))
                        Text(text = formatTime(trackDuration))
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(onClick = {
                        if (playerViewModel.isFirstTrack) {
                            playerViewModel.seekTo(0, context)
                        } else {
                            playerViewModel.previousTrack()
                            playerViewModel.track.value?.let {
                                val serviceIntent = PlayerIntentBuilder.buildPlayIntent(context, it)
                                context.startForegroundService(serviceIntent)
                            }
                        }
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_previous),
                            contentDescription = stringResource(R.string.previous),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Surface(
                        onClick = {
                            playerViewModel.togglePlayPause()
                            if (isPlaying) {
                                val pauseIntent = PlayerIntentBuilder.buildPauseIntent(context)
                                context.startForegroundService(pauseIntent)
                            } else {
                                track?.let {
                                    val playIntent =
                                        PlayerIntentBuilder.buildPlayIntent(context, it)
                                    context.startForegroundService(playIntent)
                                }
                            }
                        },
                        shape = RectangleShape,
                        color = Color.Transparent,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                painter = if (isPlaying)
                                    painterResource(R.drawable.ic_pause)
                                else painterResource(R.drawable.ic_play),
                                contentDescription = stringResource(R.string.play_pause),
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            playerViewModel.nextTrack()
                            playerViewModel.track.value?.let {
                                val serviceIntent = PlayerIntentBuilder.buildPlayIntent(context, it)
                                context.startForegroundService(serviceIntent)
                            }
                        },
                        enabled = !playerViewModel.isLastTrack
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_next),
                            contentDescription = stringResource(R.string.next),
                            tint = if (playerViewModel.isLastTrack) AppColors.Disabled else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}
