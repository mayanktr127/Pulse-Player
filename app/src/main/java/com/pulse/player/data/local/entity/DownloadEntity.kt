package com.pulse.player.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pulse.player.data.model.VideoSourceType

enum class DownloadStatus {
    PENDING,
    DOWNLOADING,
    COMPLETED,
    PAUSED,
    FAILED
}

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey
    val id: String,
    val videoId: String,
    val title: String,
    val localFilePath: String,
    val fileSizeBytes: Long = 0L,
    val bytesDownloaded: Long = 0L,
    val status: DownloadStatus = DownloadStatus.PENDING,
    val errorMessage: String? = null,
    val createdTimestamp: Long = System.currentTimeMillis()
)
