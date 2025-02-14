package com.example.beatapp

import com.example.beatapp.domain.model.Track
import com.example.beatapp.domain.repository.DeezerRepository
import com.example.beatapp.domain.usecase.GetChartTracksUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

class GetChartTracksUseCaseTest {

    private lateinit var repository: DeezerRepository
    private lateinit var getChartTracksUseCase: GetChartTracksUseCase

    @Before
    fun setUp() {
        repository = mockk()
        getChartTracksUseCase = GetChartTracksUseCase(repository)
    }

    @Test
    fun `invoke returns list of tracks from repository`() = runBlocking {
        val dummyTracks = listOf(
            Track(1, "Track 1", "url1", 120, "Artist 1", "Album 1", "cover1"),
            Track(2, "Track 2", "url2", 180, "Artist 2", "Album 2", "cover2")
        )
        coEvery { repository.getChartTracks() } returns dummyTracks

        val result = getChartTracksUseCase()

        assertEquals(2, result.size)
        assertEquals("Track 1", result[0].title)
        assertEquals("Track 2", result[1].title)
    }

    @Test
    fun `invoke throws exception if repository fails`(): Unit = runBlocking {
        coEvery { repository.getChartTracks() } throws RuntimeException("Some error")

        assertThrows(RuntimeException::class.java) {
            runBlocking {
                getChartTracksUseCase()
            }
        }
    }
}