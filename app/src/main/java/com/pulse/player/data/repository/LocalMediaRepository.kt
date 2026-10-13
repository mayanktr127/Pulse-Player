package com.pulse.player.data.repository

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.pulse.player.data.model.VideoItem
import com.pulse.player.data.model.VideoSourceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalMediaRepository(private val context: Context) {

    suspend fun getLocalVideos(): List<VideoItem> = withContext(Dispatchers.IO) {
        val videoList = mutableListOf<VideoItem>()
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATE_ADDED
        )
        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        try {
            context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn) ?: "Video $id"
                    val durationMs = cursor.getLong(durationColumn)
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        id
                    ).toString()

                    videoList.add(
                        VideoItem(
                            id = "local_vid_$id",
                            title = title,
                            description = "Local media video file stored on device.",
                            thumbnailUrl = "",
                            channelTitle = "Device Video",
                            channelId = "local_storage",
                            durationSeconds = durationMs / 1000L,
                            streamUrl = contentUri,
                            sourceType = VideoSourceType.LOCAL_FILE
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Handled when permissions are not granted or no media files
        }
        videoList
    }

    suspend fun getLocalAudio(): List<VideoItem> = withContext(Dispatchers.IO) {
        val audioList = mutableListOf<VideoItem>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        try {
            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                null
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn) ?: "Audio $id"
                    val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                    val durationMs = cursor.getLong(durationColumn)
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    ).toString()

                    audioList.add(
                        VideoItem(
                            id = "local_aud_$id",
                            title = title,
                            description = "Local audio track on device.",
                            thumbnailUrl = "",
                            channelTitle = artist,
                            channelId = "local_music",
                            durationSeconds = durationMs / 1000L,
                            streamUrl = contentUri,
                            sourceType = VideoSourceType.LOCAL_FILE
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Handled safely
        }
        audioList
    }
}
