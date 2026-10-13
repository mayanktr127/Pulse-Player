package com.pulse.player.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulse.player.data.model.VideoItem
import com.pulse.player.data.model.VideoSourceType
import com.pulse.player.data.repository.DownloadRepository
import com.pulse.player.data.repository.MediaRepository
import com.pulse.player.player.Media3PlayerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VideoDetailUiState(
    val isLoading: Boolean = true,
    val video: VideoItem? = null,
    val relatedVideos: List<VideoItem> = emptyList(),
    val isBookmarked: Boolean = false,
    val errorMessage: String? = null
)

class VideoDetailViewModel(
    private val videoId: String,
    private val sourceType: VideoSourceType,
    private val mediaRepository: MediaRepository,
    private val downloadRepository: DownloadRepository,
    val playerManager: Media3PlayerManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(VideoDetailUiState())
    val uiState: StateFlow<VideoDetailUiState> = _uiState.asStateFlow()

    init {
        loadVideoDetails()
        observeBookmarkStatus()
    }

    private fun loadVideoDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val item = mediaRepository.getVideoDetails(videoId, sourceType)
                val related = mediaRepository.getTrendingVideos().filterNot { it.id == videoId }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        video = item,
                        relatedVideos = related
                    )
                }

                if (item != null) {
                    mediaRepository.recordWatchHistory(item)

                    // For non-YouTube sources, prepare Media3 ExoPlayer
                    if (item.sourceType != VideoSourceType.YOUTUBE && item.streamUrl.isNotBlank()) {
                        playerManager.play(item, item.streamUrl)
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to load video details"
                    )
                }
            }
        }
    }

    private fun observeBookmarkStatus() {
        viewModelScope.launch {
            mediaRepository.isBookmarked(videoId).collect { bookmarked ->
                _uiState.update { it.copy(isBookmarked = bookmarked) }
            }
        }
    }

    fun toggleBookmark() {
        val currentVideo = _uiState.value.video ?: return
        viewModelScope.launch {
            mediaRepository.toggleBookmark(currentVideo, _uiState.value.isBookmarked)
        }
    }

    fun downloadVideo(onResult: (String) -> Unit) {
        val currentVideo = _uiState.value.video ?: return
        viewModelScope.launch {
            val result = downloadRepository.startDownload(currentVideo)
            result.onSuccess { onResult("Download initiated") }
                .onFailure { onResult(it.message ?: "Download failed") }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Release or pause local player
        if (sourceType != VideoSourceType.YOUTUBE) {
            playerManager.exoPlayer.pause()
        }
    }
}
