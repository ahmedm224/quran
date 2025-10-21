package com.quranmedia.player.presentation.screens.surahs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quranmedia.player.domain.model.Surah
import com.quranmedia.player.domain.repository.QuranRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SurahsViewModel @Inject constructor(
    private val quranRepository: QuranRepository
) : ViewModel() {

    val surahs: StateFlow<List<Surah>> = quranRepository.getAllSurahs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
