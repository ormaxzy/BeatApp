package com.example.beatapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TrackDetailResponse(
    val id: Long,
    val title: String,
    val duration: Int,
    val preview: String,
    val artist: ArtistDto,
    val album: AlbumDto,
    @SerialName("explicit_lyrics")
    val explicitLyrics: Boolean = false,
    val releaseDate: String? = null
)
