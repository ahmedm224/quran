package com.quranmedia.player.data.api

import com.quranmedia.player.data.api.model.AllSurahsResponse
import com.quranmedia.player.data.api.model.EditionsResponse
import com.quranmedia.player.data.api.model.SurahResponse
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Al-Quran Cloud API Service
 * Base URL: http://api.alquran.cloud/v1/
 * Documentation: https://alquran.cloud/api
 */
interface AlQuranCloudApi {

    /**
     * Get metadata for all 114 Surahs
     */
    @GET("surah")
    suspend fun getAllSurahs(): AllSurahsResponse

    /**
     * Get a specific Surah with all its Ayahs
     * @param surahNumber Surah number (1-114)
     * @param edition Quran edition identifier (default: quran-uthmani)
     */
    @GET("surah/{number}/{edition}")
    suspend fun getSurah(
        @Path("number") surahNumber: Int,
        @Path("edition") edition: String = "quran-uthmani"
    ): SurahResponse

    /**
     * Get a specific Surah with default edition (quran-uthmani)
     * @param surahNumber Surah number (1-114)
     */
    @GET("surah/{number}")
    suspend fun getSurahDefault(
        @Path("number") surahNumber: Int
    ): SurahResponse

    /**
     * Get all available audio editions (reciters)
     */
    @GET("edition/format/audio")
    suspend fun getAudioEditions(): EditionsResponse

    companion object {
        const val BASE_URL = "https://api.alquran.cloud/v1/"

        // Available editions
        const val EDITION_UTHMANI = "quran-uthmani"
        const val EDITION_UTHMANI_QURAN_ACADEMY = "quran-uthmani-quran-academy"
        const val EDITION_SIMPLE = "quran-simple"
        const val EDITION_SIMPLE_CLEAN = "quran-simple-clean"
    }
}
