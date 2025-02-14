package com.example.beatapp.domain.usecase

import com.example.beatapp.domain.model.Track
import com.example.beatapp.domain.repository.DeezerRepository
import javax.inject.Inject

class GetTrackDetailsUseCase @Inject constructor(
    private val repository: DeezerRepository
) {
    suspend operator fun invoke(trackId: Long): Track {
        return repository.getTrackDetails(trackId)
    }
}
