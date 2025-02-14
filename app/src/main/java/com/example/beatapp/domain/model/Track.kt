package com.example.beatapp.domain.model

data class Track(
    val id: Long,
    val title: String,
    val preview: String,
    val duration: Int,
    val artistName: String,
    val albumTitle: String,
    val coverUrl: String
)
