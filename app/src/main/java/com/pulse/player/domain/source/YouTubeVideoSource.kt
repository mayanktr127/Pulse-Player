package com.pulse.player.domain.source

import com.pulse.player.PulseApp
import com.pulse.player.data.model.VideoItem
import com.pulse.player.data.model.VideoSourceType

class YouTubeVideoSource : VideoSource {
    override val sourceType: VideoSourceType = VideoSourceType.YOUTUBE
    override val supportsBackgroundPlayback: Boolean = false // Prohibited by YouTube API Terms
    override val supportsDownloads: Boolean = false // Unauthorized stream ripping prohibited
    override val supportsPictureInPicture: Boolean = true
    override val supportsQualitySelection: Boolean = true // Handled inside official IFrame Player
    override val supportsCustomPlaybackSpeed: Boolean = true

    override suspend fun getVideoDetails(id: String): Result<VideoItem> {
        val repo = try { PulseApp.instance.youTubeRepository } catch (e: Throwable) { null }
        val item = repo?.getVideoById(id) ?: VideoItem(
            id = id,
            title = "YouTube Video ($id)",
            description = "Streamed via official YouTube IFrame Player API in compliance with YouTube Developer Terms.",
            thumbnailUrl = "https://img.youtube.com/vi/$id/hqdefault.jpg",
            channelTitle = "YouTube Creator",
            channelId = "UC_$id",
            durationSeconds = 0L,
            viewCount = 0L,
            publishedAt = "YouTube",
            streamUrl = id,
            sourceType = VideoSourceType.YOUTUBE,
            isLive = false
        )
        return Result.success(item)
    }

    override suspend fun getPlaybackUri(id: String): Result<String> {
        // For YouTube, the video ID is passed directly to the YouTube IFrame Player container
        return Result.success(id)
    }
}
