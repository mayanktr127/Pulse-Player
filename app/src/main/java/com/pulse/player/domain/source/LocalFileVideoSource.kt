package com.pulse.player.domain.source

import com.pulse.player.data.model.VideoItem
import com.pulse.player.data.model.VideoSourceType

class LocalFileVideoSource : VideoSource {
    override val sourceType: VideoSourceType = VideoSourceType.LOCAL_FILE
    override val supportsBackgroundPlayback: Boolean = true
    override val supportsDownloads: Boolean = false // Already stored locally
    override val supportsPictureInPicture: Boolean = true
    override val supportsQualitySelection: Boolean = false // Single fixed file bitrate
    override val supportsCustomPlaybackSpeed: Boolean = true

    override suspend fun getVideoDetails(id: String): Result<VideoItem> {
        return Result.success(
            VideoItem(
                id = id,
                title = "Local Media File",
                description = "Offline media file located on your device storage.",
                thumbnailUrl = "",
                channelTitle = "Local Storage",
                channelId = "device",
                streamUrl = id,
                sourceType = VideoSourceType.LOCAL_FILE
            )
        )
    }

    override suspend fun getPlaybackUri(id: String): Result<String> {
        return Result.success(id)
    }
}
