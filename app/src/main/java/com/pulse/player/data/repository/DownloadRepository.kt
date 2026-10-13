package com.pulse.player.data.repository

import android.content.Context
import com.pulse.player.data.local.AppDatabase
import com.pulse.player.data.local.entity.DownloadEntity
import com.pulse.player.data.local.entity.DownloadStatus
import com.pulse.player.data.model.VideoItem
import com.pulse.player.data.model.VideoSourceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class DownloadRepository(
    private val context: Context,
    private val database: AppDatabase,
    private val okHttpClient: OkHttpClient = OkHttpClient()
) {
    fun getAllDownloads(): Flow<List<DownloadEntity>> = database.downloadDao().getAllDownloads()

    suspend fun startDownload(video: VideoItem): Result<String> = withContext(Dispatchers.IO) {
        // Enforce YouTube terms compliance
        if (video.sourceType == VideoSourceType.YOUTUBE) {
            return@withContext Result.failure(
                IllegalStateException("Downloading YouTube audiovisual content is strictly restricted under YouTube Developer Terms of Service.")
            )
        }

        if (video.streamUrl.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Invalid stream URL for download."))
        }

        val downloadId = UUID.randomUUID().toString()
        val downloadDir = File(context.getExternalFilesDir(null), "downloads").apply { mkdirs() }
        val extension = if (video.streamUrl.contains(".mp4")) "mp4" else "ts"
        val cleanTitle = video.title.replace(Regex("[^a-zA-Z0-9.-]"), "_").take(40)
        val targetFile = File(downloadDir, "${cleanTitle}_$downloadId.$extension")

        val entity = DownloadEntity(
            id = downloadId,
            videoId = video.id,
            title = video.title,
            localFilePath = targetFile.absolutePath,
            fileSizeBytes = 0L,
            bytesDownloaded = 0L,
            status = DownloadStatus.DOWNLOADING
        )
        database.downloadDao().insertOrUpdate(entity)

        try {
            val request = Request.Builder().url(video.streamUrl).build()
            val response = okHttpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                database.downloadDao().updateStatus(downloadId, DownloadStatus.FAILED, "HTTP ${response.code}")
                return@withContext Result.failure(Exception("Failed download: HTTP ${response.code}"))
            }

            val body = response.body ?: throw Exception("Empty response body")
            val contentLength = body.contentLength()
            var downloaded = 0L

            body.byteStream().use { input ->
                FileOutputStream(targetFile).use { output ->
                    val buffer = ByteArray(8 * 1024)
                    var read: Int
                    var lastUpdate = System.currentTimeMillis()

                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        downloaded += read

                        val now = System.currentTimeMillis()
                        if (now - lastUpdate > 500) {
                            database.downloadDao().updateProgress(downloadId, downloaded, DownloadStatus.DOWNLOADING)
                            lastUpdate = now
                        }
                    }
                }
            }

            database.downloadDao().updateProgress(downloadId, downloaded, DownloadStatus.COMPLETED)
            Result.success(downloadId)
        } catch (e: Exception) {
            database.downloadDao().updateStatus(downloadId, DownloadStatus.FAILED, e.message)
            Result.failure(e)
        }
    }

    suspend fun deleteDownload(id: String) = withContext(Dispatchers.IO) {
        val download = database.downloadDao().getDownloadById(id)
        if (download != null) {
            val file = File(download.localFilePath)
            if (file.exists()) {
                file.delete()
            }
            database.downloadDao().delete(id)
        }
    }
}
