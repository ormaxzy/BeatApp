package com.example.beatapp

import app.cash.turbine.test
import com.example.beatapp.domain.model.SearchResult
import com.example.beatapp.domain.model.Track
import com.example.beatapp.domain.usecase.GetChartTracksUseCase
import com.example.beatapp.domain.usecase.SearchTracksUseCase
import com.example.beatapp.presentation.viewmodel.ApiTracksViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ApiTracksViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var getChartTracksUseCase: GetChartTracksUseCase
    private lateinit var searchTracksUseCase: SearchTracksUseCase
    private lateinit var viewModel: ApiTracksViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        getChartTracksUseCase = mockk()
        searchTracksUseCase = mockk()

        runBlocking {
            coEvery { getChartTracksUseCase() } returns emptyList()
        }

        viewModel = ApiTracksViewModel(getChartTracksUseCase, searchTracksUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads chart tracks`() = runTest {
        val dummyTracks = listOf(
            Track(1, "Chart 1", "url1", 120, "Artist 1", "Album 1", "cover1"),
            Track(2, "Chart 2", "url2", 180, "Artist 2", "Album 2", "cover2")
        )
        coEvery { getChartTracksUseCase() } returns dummyTracks

        val newViewModel = ApiTracksViewModel(getChartTracksUseCase, searchTracksUseCase)

        newViewModel.tracks.test {
            val firstEmission = awaitItem()
            testScheduler.advanceUntilIdle()
            val secondEmission = awaitItem()

            assertEquals(2, secondEmission.size)
            assertEquals("Chart 1", secondEmission[0].title)
            assertEquals("Chart 2", secondEmission[1].title)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `performSearch updates tracks`() = runTest {
        val dummyTrack = Track(10, "SearchTrack", "url", 100, "Artist", "Album", "cover")
        val searchResult = SearchResult(listOf(dummyTrack), nextIndex = null)

        coEvery { searchTracksUseCase("rock", 0) } returns searchResult

        viewModel.tracks.test {
            val initial = awaitItem()
            viewModel.onQueryChanged("rock")
            viewModel.performSearch()

            testScheduler.advanceUntilIdle()

            val updated = awaitItem()
            assertEquals(1, updated.size)
            assertEquals("SearchTrack", updated[0].title)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
