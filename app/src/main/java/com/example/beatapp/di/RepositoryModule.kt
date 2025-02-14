package com.example.beatapp.di

import com.example.beatapp.data.repository.DeezerRepositoryImpl
import com.example.beatapp.domain.repository.DeezerRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideDeezerRepository(
        apiService: com.example.beatapp.data.remote.DeezerApiService
    ): DeezerRepository {
        return DeezerRepositoryImpl(apiService)
    }
}
