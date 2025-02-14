package com.example.beatapp.domain.model

data class SearchResult(
    val tracks: List<Track>,
    val nextIndex: Int?
)
