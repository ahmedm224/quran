package com.quranmedia.player.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quranmedia.player.domain.model.Reciter
import com.quranmedia.player.domain.model.Surah
import com.quranmedia.player.domain.repository.QuranRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class SearchState(
    val surahs: List<Surah> = emptyList(),
    val reciters: List<Reciter> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val quranRepository: QuranRepository
) : ViewModel() {

    private val _searchState = MutableStateFlow(SearchState())
    val searchState: StateFlow<SearchState> = _searchState.asStateFlow()

    fun search(query: String) {
        if (query.isBlank()) {
            _searchState.value = SearchState()
            return
        }

        viewModelScope.launch {
            _searchState.value = _searchState.value.copy(isLoading = true)

            try {
                val surahs = quranRepository.searchSurahs(query)
                val reciters = quranRepository.searchReciters(query)

                _searchState.value = SearchState(
                    surahs = surahs,
                    reciters = reciters,
                    isLoading = false
                )

                Timber.d("Search results: ${surahs.size} surahs, ${reciters.size} reciters")
            } catch (e: Exception) {
                Timber.e(e, "Error searching")
                _searchState.value = _searchState.value.copy(isLoading = false)
            }
        }
    }
}
