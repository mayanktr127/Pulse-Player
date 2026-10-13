package com.pulse.player.data.repository

import com.pulse.player.data.local.AppDatabase
import com.pulse.player.data.local.entity.BookmarkEntity
import com.pulse.player.data.local.entity.HistoryEntity
import com.pulse.player.data.model.VideoItem
import com.pulse.player.data.model.VideoSourceType
import com.pulse.player.domain.source.RemoteStreamVideoSource
import kotlinx.coroutines.flow.Flow

class MediaRepository(
    private val database: AppDatabase,
    private val youTubeRepository: YouTubeRepository,
    private val localMediaRepository: LocalMediaRepository
) {
    fun getYouTubeApiKey(): String = youTubeRepository.getApiKey()
    fun setYouTubeApiKey(key: String) = youTubeRepository.setApiKey(key)
    fun hasYouTubeApiKey(): Boolean = youTubeRepository.hasApiKey()

    suspend fun getTrendingVideos(): List<VideoItem> {
        val yt = youTubeRepository.getTrendingVideos()
        val open = RemoteStreamVideoSource.SAMPLE_OPEN_STREAMS
        return yt + open
    }

    suspend fun getOpenStreams(): List<VideoItem> {
        return RemoteStreamVideoSource.SAMPLE_OPEN_STREAMS
    }

    suspend fun getLocalVideos(): List<VideoItem> {
        return localMediaRepository.getLocalVideos()
    }

    suspend fun getLocalAudio(): List<VideoItem> {
        return localMediaRepository.getLocalAudio()
    }

    suspend fun searchVideos(query: String): List<VideoItem> {
        val ytResults = youTubeRepository.searchVideos(query)
        val openResults = RemoteStreamVideoSource.SAMPLE_OPEN_STREAMS.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.channelTitle.contains(query, ignoreCase = true) ||
            it.description.contains(query, ignoreCase = true)
        }
        return ytResults + openResults
    }

    suspend fun getVideoDetails(id: String, sourceType: VideoSourceType): VideoItem? {
        return when (sourceType) {
            VideoSourceType.YOUTUBE -> youTubeRepository.getVideoById(id)
            VideoSourceType.REMOTE_STREAM -> RemoteStreamVideoSource.SAMPLE_OPEN_STREAMS.find { it.id == id }
            VideoSourceType.LOCAL_FILE -> localMediaRepository.getLocalVideos().find { it.id == id }
                ?: localMediaRepository.getLocalAudio().find { it.id == id }
        }
    }

    // History
    fun getWatchHistory(): Flow<List<HistoryEntity>> = database.historyDao().getAllHistory()

    fun getRecentHistory(limit: Int = 10): Flow<List<HistoryEntity>> = database.historyDao().getRecentHistory(limit)

    suspend fun recordWatchHistory(video: VideoItem, positionMs: Long = 0L) {
        val entity = HistoryEntity(
            videoId = video.id,
            title = video.title,
            channelTitle = video.channelTitle,
            thumbnailUrl = video.thumbnailUrl,
            sourceType = video.sourceType,
            durationSeconds = video.durationSeconds,
            playbackPositionMs = positionMs,
            lastWatchedTimestamp = System.currentTimeMillis()
        )
        database.historyDao().upsert(entity)
    }

    suspend fun updatePlaybackPosition(videoId: String, positionMs: Long) {
        database.historyDao().updatePlaybackPosition(videoId, positionMs)
    }

    suspend fun clearHistory() {
        database.historyDao().clearAll()
    }

    // Bookmarks
    fun getAllBookmarks(): Flow<List<BookmarkEntity>> = database.bookmarkDao().getAllBookmarks()

    fun isBookmarked(videoId: String): Flow<Boolean> = database.bookmarkDao().isBookmarked(videoId)

    suspend fun toggleBookmark(video: VideoItem, isCurrentlyBookmarked: Boolean) {
        if (isCurrentlyBookmarked) {
            database.bookmarkDao().delete(video.id)
        } else {
            val entity = BookmarkEntity(
                videoId = video.id,
                title = video.title,
                channelTitle = video.channelTitle,
                thumbnailUrl = video.thumbnailUrl,
                sourceType = video.sourceType,
                durationSeconds = video.durationSeconds
            )
            database.bookmarkDao().insert(entity)
        }
    }
}
