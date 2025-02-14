package com.example.beatapp

import com.example.beatapp.domain.model.SearchResult
import com.example.beatapp.domain.model.Track
import com.example.beatapp.domain.repository.DeezerRepository
import com.example.beatapp.domain.usecase.SearchTracksUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SearchTracksUseCaseTest {

    private lateinit var repository: DeezerRepository
    private lateinit var searchTracksUseCase: SearchTracksUseCase

    @Before
    fun setUp() {
        repository = mockk()
        searchTracksUseCase = SearchTracksUseCase(repository)
    }

    @Test
    fun `invoke returns search result`() = runBlocking {
        val dummyTracks = listOf(
            Track(10, "SearchTrack", "http://preview", 99, "Test artist", "Test album", "cover")
        )
        val searchResult = SearchResult(dummyTracks, nextIndex = 10)

        coEvery { repository.searchTracks("test", 0) } returns searchResult

        val result = searchTracksUseCase("test", 0)

        assertEquals(1, result.tracks.size)
        assertEquals("SearchTrack", result.tracks[0].title)
        assertEquals(10, result.nextIndex)
    }

    @Test
    fun `invoke throws exception if repository fails`(): Unit = runBlocking {
        coEvery { repository.searchTracks("test", 0) } throws RuntimeException("Error")

        assertThrows(RuntimeException::class.java) {
            runBlocking {
                searchTracksUseCase("test", 0)
            }
        }
    }
}
