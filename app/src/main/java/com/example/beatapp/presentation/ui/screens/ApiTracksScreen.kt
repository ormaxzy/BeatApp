package com.example.beatapp.presentation.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.beatapp.presentation.ui.components.TrackList
import com.example.beatapp.presentation.ui.components.TrackSearchBar
import com.example.beatapp.presentation.viewmodel.ApiTracksViewModel
import com.example.beatapp.presentation.viewmodel.PlayerViewModel

@Composable
fun ApiTracksScreen(
    navController: NavController,
    playerViewModel: PlayerViewModel,
    viewModel: ApiTracksViewModel = hiltViewModel()
) {
    val tracks by viewModel.tracks.collectAsState()
    val query by viewModel.query.collectAsState()
    val listState = rememberLazyListState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        TrackSearchBar(
            query = query,
            onQueryChange = { viewModel.onQueryChanged(it) },
            onSearch = { viewModel.performSearch() }
        )
        Spacer(modifier = Modifier.height(16.dp))
        TrackList(
            tracks = tracks,
            onTrackClick = { track ->
                val index = tracks.indexOf(track)
                playerViewModel.setQueue(tracks, index)
                navController.navigate("player/${track.id}")
                if (viewModel.hasMoreTracks()) {
                    viewModel.loadAllTracksInBackground { updatedTracks ->
                        playerViewModel.setQueue(updatedTracks, index)
                    }
                }
            },
            listState = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )
    }
}
