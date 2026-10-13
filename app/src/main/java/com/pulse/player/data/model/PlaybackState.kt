package com.pulse.player.data.model

enum class PlaybackState {
    IDLE,
    BUFFERING,
    READY,
    ENDED
}

data class PlayerState(
    val isPlaying: Boolean = false,
    val playbackState: PlaybackState = PlaybackState.IDLE,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val isFullscreen: Boolean = false,
    val availableQualities: List<StreamQuality> = emptyList(),
    val selectedQuality: StreamQuality = StreamQuality.AUTO,
    val errorMessage: String? = null
)
