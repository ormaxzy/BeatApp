package com.example.beatapp.domain.usecase

import com.example.beatapp.domain.model.Track
import com.example.beatapp.domain.repository.DeezerRepository
import javax.inject.Inject

class GetChartTracksUseCase @Inject constructor(
    private val repository: DeezerRepository
) {
    suspend operator fun invoke(): List<Track> {
        return repository.getChartTracks()
    }
}
