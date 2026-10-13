package com.pulse.player.domain.source

import com.pulse.player.data.model.VideoItem
import com.pulse.player.data.model.VideoSourceType

interface VideoSource {
    val sourceType: VideoSourceType
    val supportsBackgroundPlayback: Boolean
    val supportsDownloads: Boolean
    val supportsPictureInPicture: Boolean
    val supportsQualitySelection: Boolean
    val supportsCustomPlaybackSpeed: Boolean

    suspend fun getVideoDetails(id: String): Result<VideoItem>
    suspend fun getPlaybackUri(id: String): Result<String>
}
