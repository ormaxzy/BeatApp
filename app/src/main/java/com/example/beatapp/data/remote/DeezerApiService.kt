package com.example.beatapp.data.remote


import com.example.beatapp.data.model.ChartResponse
import com.example.beatapp.data.model.SearchResponse
import com.example.beatapp.data.model.TrackDetailResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface DeezerApiService {
    @GET("chart")
    suspend fun getChartTracks(): ChartResponse

    @GET("search")
    suspend fun searchTracks(
        @Query("q") query: String,
        @Query("index") index: Int = 0
    ): SearchResponse


    @GET("track/{id}")
    suspend fun getTrack(@Path("id") id: Long): TrackDetailResponse
}
