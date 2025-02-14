package com.example.beatapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChartResponse(
    val tracks: TracksContainer
)

@Serializable
data class TracksContainer(
    val data: List<TrackDto>
)

@Serializable
data class TrackDto(
    val id: Long,
    val title: String,
    @SerialName("title_short")
    val titleShort: String = "",
    val preview: String,
    val duration: Int,
    val artist: ArtistDto,
    val album: AlbumDto
)

@Serializable
data class ArtistDto(
    val id: Int,
    val name: String
)

@Serializable
data class AlbumDto(
    val id: Int,
    val title: String,
    val cover: String,
    @SerialName("cover_big")
    val coverBig: String,
    @SerialName("cover_xl")
    val coverXl: String
)
