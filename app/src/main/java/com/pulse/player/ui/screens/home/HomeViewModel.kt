package com.pulse.player.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulse.player.data.local.entity.HistoryEntity
import com.pulse.player.data.model.VideoItem
import com.pulse.player.data.repository.DownloadRepository
import com.pulse.player.data.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val trendingVideos: List<VideoItem> = emptyList(),
    val openStreams: List<VideoItem> = emptyList(),
    val errorMessage: String? = null
)

class HomeViewModel(
    private val mediaRepository: MediaRepository,
    private val downloadRepository: DownloadRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val recentHistory: StateFlow<List<HistoryEntity>> = mediaRepository.getRecentHistory(5)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadHomeFeeds()
    }

    fun loadHomeFeeds() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val trending = mediaRepository.getTrendingVideos()
                val open = mediaRepository.getOpenStreams()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        trendingVideos = trending,
                        openStreams = open
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to load video feeds"
                    )
                }
            }
        }
    }

    fun bookmarkVideo(video: VideoItem) {
        viewModelScope.launch {
            mediaRepository.toggleBookmark(video, false)
        }
    }

    fun downloadVideo(video: VideoItem, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val result = downloadRepository.startDownload(video)
            result.onSuccess { onResult("Download started") }
                .onFailure { onResult(it.message ?: "Download failed") }
        }
    }
}
