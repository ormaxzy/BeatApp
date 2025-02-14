package com.example.beatapp.data.mapper

import com.example.beatapp.data.model.TrackDto
import com.example.beatapp.domain.model.Track

fun TrackDto.toDomain(): Track {
    return Track(
        id = id,
        title = title,
        preview = preview,
        duration = duration,
        artistName = artist.name,
        albumTitle = album.title,
        coverUrl = album.coverXl
    )
}
