package com.pulse.player.domain.source

import com.pulse.player.data.model.VideoItem
import com.pulse.player.data.model.VideoSourceType

class RemoteStreamVideoSource : VideoSource {
    override val sourceType: VideoSourceType = VideoSourceType.REMOTE_STREAM
    override val supportsBackgroundPlayback: Boolean = true
    override val supportsDownloads: Boolean = true
    override val supportsPictureInPicture: Boolean = true
    override val supportsQualitySelection: Boolean = true
    override val supportsCustomPlaybackSpeed: Boolean = true

    override suspend fun getVideoDetails(id: String): Result<VideoItem> {
        val sample = SAMPLE_OPEN_STREAMS.find { it.id == id }
            ?: VideoItem(
                id = id,
                title = "Authorized Remote Stream",
                description = "High definition remote stream playable via AndroidX Media3 ExoPlayer.",
                thumbnailUrl = "https://images.unsplash.com/photo-1536240478700-b869070f9279?w=800&q=80",
                channelTitle = "Open Media Project",
                channelId = "open_media",
                durationSeconds = 734L,
                viewCount = 98400L,
                publishedAt = "2024",
                streamUrl = id,
                sourceType = VideoSourceType.REMOTE_STREAM
            )
        return Result.success(sample)
    }

    override suspend fun getPlaybackUri(id: String): Result<String> {
        val stream = SAMPLE_OPEN_STREAMS.find { it.id == id }
        val uri = stream?.streamUrl ?: id
        return Result.success(uri)
    }

    companion object {
        val SAMPLE_OPEN_STREAMS = listOf(
            VideoItem(
                id = "big_buck_bunny",
                title = "Big Buck Bunny (HLS 1080p)",
                description = "An open-source animated film by the Blender Institute. Licensed under Creative Commons Attribution 3.0.",
                thumbnailUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=800&q=80",
                channelTitle = "Blender Open Projects",
                channelId = "blender_foundation",
                durationSeconds = 596L,
                viewCount = 1420000L,
                publishedAt = "2023",
                streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
                sourceType = VideoSourceType.REMOTE_STREAM
            ),
            VideoItem(
                id = "tears_of_steel",
                title = "Tears of Steel (4K Sci-Fi Short)",
                description = "Open source VFX short film by the Blender Foundation exploring futuristic Amsterdam.",
                thumbnailUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800&q=80",
                channelTitle = "Blender Studio",
                channelId = "blender_studio",
                durationSeconds = 734L,
                viewCount = 890000L,
                publishedAt = "2023",
                streamUrl = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8",
                sourceType = VideoSourceType.REMOTE_STREAM
            ),
            VideoItem(
                id = "sintel_animation",
                title = "Sintel (DASH / HLS Open Movie)",
                description = "The third Blender Open Movie, independently produced by the Blender Foundation.",
                thumbnailUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=800&q=80",
                channelTitle = "Blender Foundation",
                channelId = "blender_foundation",
                durationSeconds = 888L,
                viewCount = 2100000L,
                publishedAt = "2024",
                streamUrl = "https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8",
                sourceType = VideoSourceType.REMOTE_STREAM
            ),
            VideoItem(
                id = "elephants_dream",
                title = "Elephants Dream (Classic 1080p)",
                description = "The world's first open-source computer-animated film.",
                thumbnailUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=800&q=80",
                channelTitle = "Orange Open Movie Project",
                channelId = "orange_project",
                durationSeconds = 653L,
                viewCount = 540000L,
                publishedAt = "2022",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                sourceType = VideoSourceType.REMOTE_STREAM
            )
        )
    }
}
