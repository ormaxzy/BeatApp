package com.example.beatapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SearchResponse(
    val data: List<TrackDto>,
    val total: Int,
    val next: String? = null
)
