package com.example.beatapp.data.repository

import com.example.beatapp.data.mapper.toDomain
import com.example.beatapp.domain.model.Track
import com.example.beatapp.domain.repository.DeezerRepository
import com.example.beatapp.data.remote.DeezerApiService
import com.example.beatapp.domain.model.SearchResult
import javax.inject.Inject

class DeezerRepositoryImpl @Inject constructor(
    private val apiService: DeezerApiService
) : DeezerRepository {
    override suspend fun getChartTracks(): List<Track> {
        val response = apiService.getChartTracks()
        return response.tracks.data.map { it.toDomain() }
    }

    override suspend fun searchTracks(query: String, index: Int): SearchResult {
        val response = apiService.searchTracks(query, index)
        val tracks = response.data.map { it.toDomain() }
        val nextIndex = if (response.next != null) index + response.data.size else null
        return SearchResult(tracks, nextIndex)
    }


    override suspend fun getTrackDetails(id: Long): Track {
        val response = apiService.getTrack(id)
        return Track(
            id = response.id,
            title = response.title,
            preview = response.preview,
            duration = response.duration,
            artistName = response.artist.name,
            albumTitle = response.album.title,
            coverUrl = response.album.cover
        )
    }
}
