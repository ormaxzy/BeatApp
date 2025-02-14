package com.example.beatapp.domain.usecase

import com.example.beatapp.domain.model.SearchResult
import com.example.beatapp.domain.repository.DeezerRepository
import javax.inject.Inject

class SearchTracksUseCase @Inject constructor(
    private val repository: DeezerRepository
) {
    suspend operator fun invoke(query: String, index: Int = 0): SearchResult {
        return repository.searchTracks(query, index)
    }
}
