package com.quranmedia.player.di

import com.quranmedia.player.data.repository.QuranRepositoryImpl
import com.quranmedia.player.domain.repository.QuranRepository
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
}
