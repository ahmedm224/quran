package com.quranmedia.player.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quranmedia.player.data.repository.SettingsRepository
import com.quranmedia.player.domain.model.Reciter
import com.quranmedia.player.domain.model.Surah
import com.quranmedia.player.domain.repository.QuranRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class LastPlaybackInfo(
    val reciter: Reciter? = null,
    val surah: Surah? = null,
    val positionMs: Long = 0L
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val quranRepository: QuranRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val settings = settingsRepository.settings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _lastPlaybackInfo = MutableStateFlow<LastPlaybackInfo?>(null)
    val lastPlaybackInfo: StateFlow<LastPlaybackInfo?> = _lastPlaybackInfo.asStateFlow()

    init {
        loadLastPlaybackInfo()
    }

    fun refreshLastPlaybackInfo() {
        loadLastPlaybackInfo()
    }

    private fun loadLastPlaybackInfo() {
        viewModelScope.launch {
            try {
                val userSettings = settingsRepository.settings.first()
                val lastReciterId = userSettings.lastReciterId
                val lastSurahNumber = userSettings.lastSurahNumber
                val lastPositionMs = userSettings.lastPositionMs

                Timber.d("Loading last playback: reciter=$lastReciterId, surah=$lastSurahNumber, position=$lastPositionMs")

                if (lastReciterId.isNotBlank() && lastSurahNumber > 0) {
                    val reciter = quranRepository.getReciterById(lastReciterId)
                    val surah = quranRepository.getSurahByNumber(lastSurahNumber)

                    Timber.d("Found reciter: ${reciter?.name}, surah: ${surah?.nameEnglish}")

                    _lastPlaybackInfo.value = LastPlaybackInfo(
                        reciter = reciter,
                        surah = surah,
                        positionMs = lastPositionMs
                    )

                    Timber.d("Last playback info set: ${_lastPlaybackInfo.value}")
                } else {
                    Timber.d("No valid last playback (reciter blank or surah <= 0)")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading last playback info")
            }
        }
    }
}
