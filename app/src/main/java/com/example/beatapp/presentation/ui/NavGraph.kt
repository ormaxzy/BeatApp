package com.example.beatapp.presentation.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.beatapp.R
import com.example.beatapp.presentation.ui.screens.ApiTracksScreen
import com.example.beatapp.presentation.ui.screens.DownloadedTracksScreen
import com.example.beatapp.presentation.ui.screens.PlayerScreen
import com.example.beatapp.presentation.viewmodel.PlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeatAppNavGraph(playerViewModel: PlayerViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "downloaded_tracks"

    Scaffold(
        topBar = {
            when {
                currentRoute.startsWith("downloaded_tracks") -> {
                    CenterAlignedTopAppBar(
                        title = { Text(stringResource(R.string.downloaded_tracks_title)) },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
                currentRoute.startsWith("api_tracks") -> {
                    CenterAlignedTopAppBar(
                        title = { Text(stringResource(R.string.api_tracks_title)) },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
                currentRoute.startsWith("player") -> {
                    CenterAlignedTopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_logo),
                                    contentDescription = stringResource(R.string.app_logo),
                                    modifier = Modifier
                                        .width(32.dp)
                                        .padding(end = 8.dp),
                                    tint = Color.Unspecified
                                )
                                Text(
                                    text = buildAnnotatedString {
                                        withStyle(style = SpanStyle(color = Color(0xFFC4A5E8))) {
                                            append("Beat")
                                        }
                                        withStyle(style = SpanStyle(color = Color(0xFF9D31FE))) {
                                            append("App")
                                        }
                                    },
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_local_music),
                            contentDescription = null,
                            tint = if (currentRoute == "downloaded_tracks")
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    },
                    label = {
                        Text(
                            stringResource(R.string.downloaded_tracks),
                            color = if (currentRoute == "downloaded_tracks")
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    },
                    selected = currentRoute == "downloaded_tracks",
                    onClick = { navController.navigate("downloaded_tracks") },
                    colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent,
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                )
                NavigationBarItem(
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_search_music),
                            contentDescription = null,
                            tint = if (currentRoute == "api_tracks")
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    },
                    label = {
                        Text(
                            stringResource(R.string.api_tracks),
                            color = if (currentRoute == "api_tracks")
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    },
                    selected = currentRoute == "api_tracks",
                    onClick = { navController.navigate("api_tracks") },
                    colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent,
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "downloaded_tracks",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("downloaded_tracks") {
                DownloadedTracksScreen(navController, playerViewModel)
            }
            composable("api_tracks") {
                ApiTracksScreen(navController, playerViewModel)
            }
            composable(
                route = "player/{trackId}",
                arguments = listOf(navArgument("trackId") { type = NavType.LongType })
            ) {
                PlayerScreen(playerViewModel = playerViewModel)
            }
        }
    }
}
