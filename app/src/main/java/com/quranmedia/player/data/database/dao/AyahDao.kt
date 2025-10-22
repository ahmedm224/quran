package com.quranmedia.player.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.quranmedia.player.data.database.entity.AyahEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AyahDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAyah(ayah: AyahEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAyahs(ayahs: List<AyahEntity>)

    @Query("SELECT * FROM ayahs WHERE surahNumber = :surahNumber ORDER BY ayahNumber ASC")
    fun getAyahsBySurah(surahNumber: Int): Flow<List<AyahEntity>>

    @Query("SELECT * FROM ayahs WHERE surahNumber = :surahNumber AND ayahNumber = :ayahNumber")
    suspend fun getAyah(surahNumber: Int, ayahNumber: Int): AyahEntity?

    @Query("SELECT * FROM ayahs WHERE globalAyahNumber = :globalNumber")
    suspend fun getAyahByGlobalNumber(globalNumber: Int): AyahEntity?

    @Query("SELECT * FROM ayahs WHERE page = :pageNumber ORDER BY surahNumber, ayahNumber")
    fun getAyahsByPage(pageNumber: Int): Flow<List<AyahEntity>>

    @Query("SELECT * FROM ayahs WHERE juz = :juzNumber ORDER BY surahNumber, ayahNumber")
    fun getAyahsByJuz(juzNumber: Int): Flow<List<AyahEntity>>

    @Query("SELECT * FROM ayahs WHERE sajda = 1 ORDER BY surahNumber, ayahNumber")
    fun getSajdaAyahs(): Flow<List<AyahEntity>>

    @Query("SELECT COUNT(*) FROM ayahs")
    suspend fun getAyahCount(): Int

    @Query("SELECT COUNT(*) FROM ayahs WHERE surahNumber = :surahNumber")
    suspend fun getAyahCountForSurah(surahNumber: Int): Int

    @Query("DELETE FROM ayahs")
    suspend fun deleteAllAyahs()

    @Query("DELETE FROM ayahs WHERE surahNumber = :surahNumber")
    suspend fun deleteAyahsForSurah(surahNumber: Int)

    @Query("SELECT * FROM ayahs ORDER BY globalAyahNumber ASC")
    fun getAllAyahs(): Flow<List<AyahEntity>>

    @Query("SELECT * FROM ayahs WHERE textArabic LIKE '%' || :query || '%' ORDER BY globalAyahNumber ASC")
    suspend fun searchAyahs(query: String): List<AyahEntity>
}
