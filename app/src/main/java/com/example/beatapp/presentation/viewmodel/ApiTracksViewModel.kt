package com.example.beatapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.beatapp.domain.model.Track
import com.example.beatapp.domain.usecase.GetChartTracksUseCase
import com.example.beatapp.domain.usecase.SearchTracksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ApiTracksViewModel @Inject constructor(
    private val getChartTracksUseCase: GetChartTracksUseCase,
    private val searchTracksUseCase: SearchTracksUseCase
) : ViewModel() {

    private val _tracks = MutableStateFlow<List<Track>>(emptyList())
    val tracks: StateFlow<List<Track>> = _tracks

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private var nextSearchIndex: Int? = null

    init {
        viewModelScope.launch {
            _tracks.value = getChartTracksUseCase()
        }
    }

    fun onQueryChanged(newQuery: String) {
        _query.value = newQuery
        nextSearchIndex = null
    }

    fun performSearch() {
        viewModelScope.launch {
            if (_query.value.isNotBlank()) {
                val result = searchTracksUseCase(_query.value, index = 0)
                _tracks.value = result.tracks
                nextSearchIndex = result.nextIndex
            } else {
                _tracks.value = getChartTracksUseCase()
            }
        }
    }

    fun hasMoreTracks(): Boolean = nextSearchIndex != null

    private suspend fun loadAllTracks() {
        while (hasMoreTracks()) {
            val result = searchTracksUseCase(_query.value, nextSearchIndex!!)
            _tracks.value += result.tracks
            nextSearchIndex = result.nextIndex
        }
    }

    fun loadAllTracksInBackground(onComplete: (List<Track>) -> Unit) {
        viewModelScope.launch {
            loadAllTracks()
            onComplete(_tracks.value)
        }
    }
}
