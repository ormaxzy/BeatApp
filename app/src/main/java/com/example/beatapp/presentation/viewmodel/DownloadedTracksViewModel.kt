package com.example.beatapp.presentation.viewmodel

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.beatapp.domain.model.Track
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class DownloadedTracksViewModel @Inject constructor() : ViewModel() {

    private val _allLocalTracks = mutableListOf<Track>()
    private val _tracks = MutableStateFlow<List<Track>>(emptyList())
    val tracks: StateFlow<List<Track>> = _tracks

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    fun onQueryChanged(newQuery: String) {
        _query.value = newQuery
        filterTracks()
    }

    private fun filterTracks() {
        val q = _query.value.trim().lowercase()
        _tracks.value = if (q.isBlank()) {
            _allLocalTracks
        } else {
            _allLocalTracks.filter { track ->
                track.title.lowercase().contains(q) || track.artistName.lowercase().contains(q)
            }
        }
    }

    fun loadLocalTracks(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val localTracks = mutableListOf<Track>()
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ALBUM_ID
            )
            val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"
            val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

            context.contentResolver.query(uri, projection, null, null, sortOrder)?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val title = cursor.getString(titleCol)
                    val artist = cursor.getString(artistCol)
                    val durationMs = cursor.getLong(durationCol)
                    val albumId = cursor.getLong(albumIdCol)

                    val audioUri = ContentUris.withAppendedId(uri, id).toString()
                    val coverUri = "content://media/external/audio/albumart/$albumId"

                    localTracks.add(
                        Track(
                            id = id,
                            title = title,
                            preview = audioUri,
                            duration = (durationMs / 1000).toInt(),
                            artistName = artist,
                            albumTitle = "",
                            coverUrl = coverUri
                        )
                    )
                }
            }
            withContext(Dispatchers.Main) {
                _allLocalTracks.clear()
                _allLocalTracks.addAll(localTracks)
                filterTracks()
            }
        }
    }
}
