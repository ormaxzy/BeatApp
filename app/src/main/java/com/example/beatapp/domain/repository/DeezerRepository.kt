package com.example.beatapp.domain.repository

import com.example.beatapp.domain.model.SearchResult
import com.example.beatapp.domain.model.Track

interface DeezerRepository {
    suspend fun getChartTracks(): List<Track>
    suspend fun searchTracks(query: String, index: Int = 0): SearchResult
    suspend fun getTrackDetails(id: Long): Track
}
