package com.pulse.player.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pulse.player.data.model.VideoSourceType

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey
    val videoId: String,
    val title: String,
    val channelTitle: String,
    val thumbnailUrl: String,
    val sourceType: VideoSourceType,
    val durationSeconds: Long,
    val bookmarkedTimestamp: Long = System.currentTimeMillis()
)
