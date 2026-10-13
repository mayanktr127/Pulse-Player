package com.pulse.player.data.model

data class VideoItem(
    val id: String,
    val title: String,
    val description: String = "",
    val thumbnailUrl: String = "",
    val channelTitle: String = "",
    val channelId: String = "",
    val durationSeconds: Long = 0L,
    val viewCount: Long = 0L,
    val publishedAt: String = "",
    val streamUrl: String = "",
    val sourceType: VideoSourceType = VideoSourceType.YOUTUBE,
    val isLive: Boolean = false,
    val isDownloadable: Boolean = (sourceType != VideoSourceType.YOUTUBE)
)
