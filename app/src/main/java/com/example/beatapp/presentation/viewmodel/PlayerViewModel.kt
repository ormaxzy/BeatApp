package com.example.beatapp.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.beatapp.domain.model.Track
import com.example.beatapp.util.PlayerIntentBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor() : ViewModel() {

    private val _track = MutableStateFlow<Track?>(null)
    val track: StateFlow<Track?> = _track

    private val _trackQueue = MutableStateFlow<List<Track>>(emptyList())
    val trackQueue: StateFlow<List<Track>> = _trackQueue

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    val isFirstTrack: Boolean
        get() = _currentIndex.value == 0

    val isLastTrack: Boolean
        get() = _currentIndex.value >= _trackQueue.value.size - 1

    fun setQueue(queue: List<Track>, startIndex: Int) {
        _trackQueue.value = queue
        _currentIndex.value = startIndex
        loadCurrentTrack()
    }

    private fun loadCurrentTrack() {
        _track.value = _trackQueue.value.getOrNull(_currentIndex.value)
        _isPlaying.value = _track.value != null
    }

    fun togglePlayPause() {
        _isPlaying.value = !_isPlaying.value
    }

    fun nextTrack() {
        if (_currentIndex.value < _trackQueue.value.size - 1) {
            _currentIndex.value++
            loadCurrentTrack()
        }
    }

    fun previousTrack() {
        if (_currentIndex.value > 0) {
            _currentIndex.value--
            loadCurrentTrack()
        }
    }

    fun setPlaying(playing: Boolean) {
        _isPlaying.value = playing
    }

    fun seekTo(position: Long, context: Context) {
        val serviceIntent = PlayerIntentBuilder.buildSeekToIntent(context, position)
        context.startForegroundService(serviceIntent)
    }
}
