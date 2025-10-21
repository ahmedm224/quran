package com.quranmedia.player.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.quranmedia.player.data.database.dao.AyahDao
import com.quranmedia.player.data.database.dao.AyahIndexDao
import com.quranmedia.player.data.database.dao.AudioVariantDao
import com.quranmedia.player.data.database.dao.BookmarkDao
import com.quranmedia.player.data.database.dao.DownloadTaskDao
import com.quranmedia.player.data.database.dao.ReciterDao
import com.quranmedia.player.data.database.dao.SurahDao
import com.quranmedia.player.data.database.entity.AyahEntity
import com.quranmedia.player.data.database.entity.AyahIndexEntity
import com.quranmedia.player.data.database.entity.AudioVariantEntity
import com.quranmedia.player.data.database.entity.BookmarkEntity
import com.quranmedia.player.data.database.entity.DownloadTaskEntity
import com.quranmedia.player.data.database.entity.ReciterEntity
import com.quranmedia.player.data.database.entity.SurahEntity

@Database(
    entities = [
        ReciterEntity::class,
        SurahEntity::class,
        AudioVariantEntity::class,
        AyahEntity::class,  // Quran Ayah data
        AyahIndexEntity::class,
        BookmarkEntity::class,
        DownloadTaskEntity::class
    ],
    version = 2,  // Incremented for AyahEntity
    exportSchema = true
)
abstract class QuranDatabase : RoomDatabase() {
    abstract fun reciterDao(): ReciterDao
    abstract fun surahDao(): SurahDao
    abstract fun audioVariantDao(): AudioVariantDao
    abstract fun ayahDao(): AyahDao
    abstract fun ayahIndexDao(): AyahIndexDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun downloadTaskDao(): DownloadTaskDao
}
