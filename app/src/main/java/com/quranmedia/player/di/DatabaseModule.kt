package com.quranmedia.player.di

import android.content.Context
import androidx.room.Room
import com.quranmedia.player.data.database.QuranDatabase
import com.quranmedia.player.data.database.dao.AyahDao
import com.quranmedia.player.data.database.dao.AyahIndexDao
import com.quranmedia.player.data.database.dao.AudioVariantDao
import com.quranmedia.player.data.database.dao.BookmarkDao
import com.quranmedia.player.data.database.dao.DownloadTaskDao
import com.quranmedia.player.data.database.dao.ReciterDao
import com.quranmedia.player.data.database.dao.SurahDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideQuranDatabase(
        @ApplicationContext context: Context
    ): QuranDatabase {
        return Room.databaseBuilder(
            context,
            QuranDatabase::class.java,
            "quran_media_db"
        )
            .fallbackToDestructiveMigration() // TODO: Add proper migrations
            .build()
    }

    @Provides
    fun provideReciterDao(database: QuranDatabase): ReciterDao {
        return database.reciterDao()
    }

    @Provides
    fun provideSurahDao(database: QuranDatabase): SurahDao {
        return database.surahDao()
    }

    @Provides
    fun provideAudioVariantDao(database: QuranDatabase): AudioVariantDao {
        return database.audioVariantDao()
    }

    @Provides
    fun provideAyahIndexDao(database: QuranDatabase): AyahIndexDao {
        return database.ayahIndexDao()
    }

    @Provides
    fun provideBookmarkDao(database: QuranDatabase): BookmarkDao {
        return database.bookmarkDao()
    }

    @Provides
    fun provideDownloadTaskDao(database: QuranDatabase): DownloadTaskDao {
        return database.downloadTaskDao()
    }

    @Provides
    fun provideAyahDao(database: QuranDatabase): AyahDao {
        return database.ayahDao()
    }
}
