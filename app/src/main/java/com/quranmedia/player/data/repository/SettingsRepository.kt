package com.quranmedia.player.data.repository

// TEMPORARY STUB: This is a stub implementation without protobuf DataStore
// TODO: Re-enable full DataStore implementation after fixing protobuf/KSP configuration
// See QURAN_DATA_INTEGRATION.md for fix options

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

// Temporary data class to replace protobuf UserSettings
data class UserSettings(
    val playbackSpeed: Float = 1.0f,
    val pitchLockEnabled: Boolean = true,
    val smallSeekIncrementMs: Int = 250,
    val largeSeekIncrementMs: Int = 30000,
    val snapToAyahEnabled: Boolean = true,
    val gaplessPlayback: Boolean = true,
    val volumeLevel: Int = 100,
    val normalizeAudio: Boolean = false,
    val autoLoopEnabled: Boolean = false,
    val loopCount: Int = 1,
    val waveformEnabled: Boolean = true,
    val showTranslation: Boolean = false,
    val translationLanguage: String = "en",
    val darkMode: Boolean = false,
    val dynamicColors: Boolean = true,
    val wifiOnlyDownloads: Boolean = false,
    val autoDeleteAfterPlayback: Boolean = false,
    val preferredBitrate: Int = 128,
    val preferredAudioFormat: String = "mp3",
    val lastReciterId: String = "",
    val lastSurahNumber: Int = 1,
    val lastPositionMs: Long = 0L,
    val resumeOnStartup: Boolean = false,
    val largeText: Boolean = false,
    val highContrast: Boolean = false,
    val hapticFeedbackIntensity: Int = 50,
    val continuousPlaybackEnabled: Boolean = true
)

@Singleton
class SettingsRepository @Inject constructor() {
    private val _settings = MutableStateFlow(UserSettings())

    val settings: Flow<UserSettings> = _settings

    // Playback settings
    suspend fun setPlaybackSpeed(speed: Float) {
        _settings.value = _settings.value.copy(playbackSpeed = speed)
    }

    suspend fun setPitchLockEnabled(enabled: Boolean) {
        _settings.value = _settings.value.copy(pitchLockEnabled = enabled)
    }

    // Seeking settings
    suspend fun setSmallSeekIncrement(incrementMs: Int) {
        _settings.value = _settings.value.copy(smallSeekIncrementMs = incrementMs)
    }

    suspend fun setLargeSeekIncrement(incrementMs: Int) {
        _settings.value = _settings.value.copy(largeSeekIncrementMs = incrementMs)
    }

    suspend fun setSnapToAyahEnabled(enabled: Boolean) {
        _settings.value = _settings.value.copy(snapToAyahEnabled = enabled)
    }

    // Audio settings
    suspend fun setGaplessPlayback(enabled: Boolean) {
        _settings.value = _settings.value.copy(gaplessPlayback = enabled)
    }

    suspend fun setVolumeLevel(level: Int) {
        _settings.value = _settings.value.copy(volumeLevel = level)
    }

    suspend fun setNormalizeAudio(enabled: Boolean) {
        _settings.value = _settings.value.copy(normalizeAudio = enabled)
    }

    // Loop settings
    suspend fun setAutoLoopEnabled(enabled: Boolean) {
        _settings.value = _settings.value.copy(autoLoopEnabled = enabled)
    }

    suspend fun setLoopCount(count: Int) {
        _settings.value = _settings.value.copy(loopCount = count)
    }

    // UI settings
    suspend fun setWaveformEnabled(enabled: Boolean) {
        _settings.value = _settings.value.copy(waveformEnabled = enabled)
    }

    suspend fun setShowTranslation(show: Boolean) {
        _settings.value = _settings.value.copy(showTranslation = show)
    }

    suspend fun setTranslationLanguage(language: String) {
        _settings.value = _settings.value.copy(translationLanguage = language)
    }

    suspend fun setDarkMode(enabled: Boolean) {
        _settings.value = _settings.value.copy(darkMode = enabled)
    }

    suspend fun setDynamicColors(enabled: Boolean) {
        _settings.value = _settings.value.copy(dynamicColors = enabled)
    }

    // Download settings
    suspend fun setWifiOnlyDownloads(enabled: Boolean) {
        _settings.value = _settings.value.copy(wifiOnlyDownloads = enabled)
    }

    suspend fun setAutoDeleteAfterPlayback(enabled: Boolean) {
        _settings.value = _settings.value.copy(autoDeleteAfterPlayback = enabled)
    }

    suspend fun setPreferredBitrate(bitrate: Int) {
        _settings.value = _settings.value.copy(preferredBitrate = bitrate)
    }

    suspend fun setPreferredAudioFormat(format: String) {
        _settings.value = _settings.value.copy(preferredAudioFormat = format)
    }

    // Last playback state
    suspend fun updateLastPlaybackState(reciterId: String, surahNumber: Int, positionMs: Long) {
        _settings.value = _settings.value.copy(
            lastReciterId = reciterId,
            lastSurahNumber = surahNumber,
            lastPositionMs = positionMs
        )
    }

    suspend fun setResumeOnStartup(enabled: Boolean) {
        _settings.value = _settings.value.copy(resumeOnStartup = enabled)
    }

    // Accessibility
    suspend fun setLargeText(enabled: Boolean) {
        _settings.value = _settings.value.copy(largeText = enabled)
    }

    suspend fun setHighContrast(enabled: Boolean) {
        _settings.value = _settings.value.copy(highContrast = enabled)
    }

    suspend fun setHapticFeedbackIntensity(intensity: Int) {
        _settings.value = _settings.value.copy(hapticFeedbackIntensity = intensity)
    }

    // Continuous playback
    suspend fun setContinuousPlaybackEnabled(enabled: Boolean) {
        _settings.value = _settings.value.copy(continuousPlaybackEnabled = enabled)
    }
}
