package com.quranmedia.player.presentation.screens.downloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quranmedia.player.data.database.entity.DownloadTaskEntity
import com.quranmedia.player.download.DownloadManager
import com.quranmedia.player.domain.model.AudioVariant
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class DownloadsState(
    val downloads: List<DownloadTaskEntity> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val downloadManager: DownloadManager
) : ViewModel() {

    private val _state = MutableStateFlow(DownloadsState())
    val state: StateFlow<DownloadsState> = _state.asStateFlow()

    init {
        loadDownloads()
    }

    private fun loadDownloads() {
        viewModelScope.launch {
            downloadManager.getAllDownloads().collect { downloads ->
                _state.value = _state.value.copy(
                    downloads = downloads,
                    isLoading = false
                )
            }
        }
    }

    fun downloadSurah(reciterId: String, surahNumber: Int, audioVariant: AudioVariant) {
        viewModelScope.launch {
            try {
                downloadManager.downloadAudio(reciterId, surahNumber, audioVariant)
                Timber.d("Download started for surah $surahNumber")
            } catch (e: Exception) {
                Timber.e(e, "Error starting download")
            }
        }
    }

    fun pauseDownload(taskId: String) {
        viewModelScope.launch {
            try {
                downloadManager.pauseDownload(taskId)
            } catch (e: Exception) {
                Timber.e(e, "Error pausing download")
            }
        }
    }

    fun resumeDownload(taskId: String) {
        viewModelScope.launch {
            try {
                downloadManager.resumeDownload(taskId)
            } catch (e: Exception) {
                Timber.e(e, "Error resuming download")
            }
        }
    }

    fun cancelDownload(taskId: String) {
        viewModelScope.launch {
            try {
                downloadManager.cancelDownload(taskId)
            } catch (e: Exception) {
                Timber.e(e, "Error cancelling download")
            }
        }
    }

    fun deleteDownload(reciterId: String, surahNumber: Int) {
        viewModelScope.launch {
            try {
                downloadManager.deleteDownloadedAudio(reciterId, surahNumber)
            } catch (e: Exception) {
                Timber.e(e, "Error deleting download")
            }
        }
    }
}
