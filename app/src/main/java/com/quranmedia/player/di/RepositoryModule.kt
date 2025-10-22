package com.quranmedia.player.di

import com.quranmedia.player.data.repository.QuranRepositoryImpl
import com.quranmedia.player.data.repository.SearchRepositoryImpl
import com.quranmedia.player.domain.repository.QuranRepository
import com.quranmedia.player.domain.repository.SearchRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindQuranRepository(
        quranRepositoryImpl: QuranRepositoryImpl
    ): QuranRepository

    @Binds
    @Singleton
    abstract fun bindSearchRepository(
        searchRepositoryImpl: SearchRepositoryImpl
    ): SearchRepository
}
