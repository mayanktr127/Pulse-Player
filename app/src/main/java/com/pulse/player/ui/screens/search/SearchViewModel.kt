package com.pulse.player.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulse.player.data.model.VideoItem
import com.pulse.player.data.model.VideoSourceType
import com.pulse.player.data.repository.MediaRepository
import com.pulse.player.data.repository.YouTubeApiException
import com.pulse.player.data.repository.YouTubeErrorCode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val isSearching: Boolean = false,
    val searchResults: List<VideoItem> = emptyList(),
    val recentSearches: List<String> = listOf("Android 15", "Blender Open Movie", "Lofi beats", "ExoPlayer 1080p"),
    val selectedFilter: VideoSourceType? = null,
    val errorMessage: String? = null,
    val isApiKeyMissing: Boolean = false
)

class SearchViewModel(
    private val mediaRepository: MediaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    fun onQueryChange(newQuery: String) {
        _uiState.update { it.copy(query = newQuery) }
        if (newQuery.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList(), errorMessage = null, isApiKeyMissing = false) }
        }
    }

    fun onFilterSelect(filter: VideoSourceType?) {
        _uiState.update { it.copy(selectedFilter = filter) }
        executeSearch(_uiState.value.query)
    }

    fun executeSearch(query: String) {
        if (query.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true, errorMessage = null, isApiKeyMissing = false) }
            try {
                val results = mediaRepository.searchVideos(query)
                val filtered = if (_uiState.value.selectedFilter != null) {
                    results.filter { it.sourceType == _uiState.value.selectedFilter }
                } else {
                    results
                }
                val recents = (_uiState.value.recentSearches.toMutableList().apply {
                    remove(query)
                    add(0, query)
                }).take(6)

                _uiState.update {
                    it.copy(
                        isSearching = false,
                        searchResults = filtered,
                        errorMessage = null,
                        isApiKeyMissing = false,
                        recentSearches = recents
                    )
                }
            } catch (e: YouTubeApiException) {
                val isKeyIssue = e.errorCode == YouTubeErrorCode.MISSING_API_KEY || e.errorCode == YouTubeErrorCode.INVALID_API_KEY
                _uiState.update {
                    it.copy(
                        isSearching = false,
                        searchResults = emptyList(),
                        errorMessage = e.message,
                        isApiKeyMissing = isKeyIssue
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSearching = false,
                        searchResults = emptyList(),
                        errorMessage = e.message ?: "Failed to find videos",
                        isApiKeyMissing = false
                    )
                }
            }
        }
    }

    fun updateApiKeyAndSearch(apiKey: String) {
        mediaRepository.setYouTubeApiKey(apiKey)
        executeSearch(_uiState.value.query)
    }

    fun removeRecentSearch(term: String) {
        _uiState.update {
            it.copy(recentSearches = it.recentSearches.filterNot { it == term })
        }
    }
}
