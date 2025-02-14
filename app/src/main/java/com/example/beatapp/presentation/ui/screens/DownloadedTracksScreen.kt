package com.example.beatapp.presentation.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.beatapp.R
import com.example.beatapp.presentation.ui.components.TrackList
import com.example.beatapp.presentation.ui.components.TrackSearchBar
import com.example.beatapp.presentation.viewmodel.DownloadedTracksViewModel
import com.example.beatapp.presentation.viewmodel.PlayerViewModel
import kotlinx.coroutines.delay

@Composable
fun DownloadedTracksScreen(
    navController: NavController,
    playerViewModel: PlayerViewModel,
    viewModel: DownloadedTracksViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val tracks by viewModel.tracks.collectAsState()
    val query by viewModel.query.collectAsState()

    val neededPermission = Manifest.permission.READ_MEDIA_AUDIO

    var permissionState by remember { mutableStateOf<Boolean?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionState = isGranted
        if (isGranted) viewModel.loadLocalTracks(context)
    }

    LaunchedEffect(Unit) {
        val permissionStatus = ContextCompat.checkSelfPermission(context, neededPermission)
        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
            permissionState = true
            viewModel.loadLocalTracks(context)
        } else {
            permissionLauncher.launch(neededPermission)
        }
    }

    LaunchedEffect(neededPermission) {
        while (permissionState != true) {
            val permissionStatus = ContextCompat.checkSelfPermission(context, neededPermission)
            if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                permissionState = true
                viewModel.loadLocalTracks(context)
                break
            }
            delay(100)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        TrackSearchBar(
            query = query,
            onQueryChange = { viewModel.onQueryChanged(it) },
            onSearch = { viewModel.onQueryChanged(query) }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            when (permissionState) {
                null -> CircularProgressIndicator()
                false -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = stringResource(R.string.info),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.no_tracks_or_permission),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                true -> {
                    if (tracks.isEmpty()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = stringResource(R.string.info),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                stringResource(R.string.no_tracks),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    } else {
                        TrackList(
                            tracks = tracks,
                            onTrackClick = { track ->
                                val index = tracks.indexOf(track)
                                playerViewModel.setQueue(tracks, index)
                                navController.navigate("player/${track.id}")
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}
