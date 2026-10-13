package com.pulse.player.data.repository

import android.content.Context
import android.util.Log
import com.pulse.player.data.model.VideoItem
import com.pulse.player.data.model.VideoSourceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

enum class YouTubeErrorCode {
    MISSING_API_KEY,
    INVALID_API_KEY,
    QUOTA_EXCEEDED,
    API_NOT_ENABLED,
    NETWORK_ERROR,
    PARSING_ERROR,
    UNKNOWN
}

class YouTubeApiException(
    message: String,
    val errorCode: YouTubeErrorCode,
    val httpStatusCode: Int = 0,
    cause: Throwable? = null
) : Exception(message, cause)

class YouTubeRepository(
    private val context: Context? = null,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()
) {

    companion object {
        private const val TAG = "PulseYouTube"
        private const val PREFS_NAME = "pulse_youtube_prefs"
        private const val KEY_API_KEY = "youtube_api_key"

        private val YOUTUBE_URL_REGEX = Pattern.compile(
            "(?:https?://)?(?:www\\.)?(?:youtube\\.com/(?:watch\\?v=|embed/|v/|shorts/)|youtu\\.be/)([a-zA-Z0-9_-]{11})",
            Pattern.CASE_INSENSITIVE
        )
        private val VIDEO_ID_REGEX = Pattern.compile("^[a-zA-Z0-9_-]{11}$")

        val SAMPLE_YOUTUBE_VIDEOS = listOf(
            VideoItem(
                id = "dQw4w9WgXcQ",
                title = "Rick Astley - Never Gonna Give You Up (Official Music Video)",
                description = "The official video for “Never Gonna Give You Up” by Rick Astley. Remastered in 4K.",
                thumbnailUrl = "https://img.youtube.com/vi/dQw4w9WgXcQ/hqdefault.jpg",
                channelTitle = "Rick Astley",
                channelId = "rickastley",
                durationSeconds = 212L,
                viewCount = 1600000000L,
                publishedAt = "Oct 25, 2009",
                streamUrl = "dQw4w9WgXcQ",
                sourceType = VideoSourceType.YOUTUBE
            ),
            VideoItem(
                id = "L_LUpnjgPso",
                title = "Android 15 Developer Overview: What's New",
                description = "Deep dive into new Android 15 features, Private Space, Edge-to-Edge by default, and camera improvements.",
                thumbnailUrl = "https://img.youtube.com/vi/L_LUpnjgPso/hqdefault.jpg",
                channelTitle = "Android Developers",
                channelId = "androiddev",
                durationSeconds = 945L,
                viewCount = 420000L,
                publishedAt = "2024",
                streamUrl = "L_LUpnjgPso",
                sourceType = VideoSourceType.YOUTUBE
            ),
            VideoItem(
                id = "jfKfPfyJRdk",
                title = "lofi hip hop radio 📚 - beats to relax/study to",
                description = "Lofi Girl 24/7 lofi hip hop radio stream for studying, sleeping, and chilling.",
                thumbnailUrl = "https://img.youtube.com/vi/jfKfPfyJRdk/hqdefault.jpg",
                channelTitle = "Lofi Girl",
                channelId = "lofigirl",
                durationSeconds = 0L,
                viewCount = 89000000L,
                publishedAt = "Live",
                streamUrl = "jfKfPfyJRdk",
                sourceType = VideoSourceType.YOUTUBE,
                isLive = true
            ),
            VideoItem(
                id = "kXYiU_JCYtU",
                title = "Linkin Park - Numb (Official Music Video) [4K Upgrade]",
                description = "Official Music Video for 'Numb' by Linkin Park from the album Meteora.",
                thumbnailUrl = "https://img.youtube.com/vi/kXYiU_JCYtU/hqdefault.jpg",
                channelTitle = "Linkin Park",
                channelId = "linkinpark",
                durationSeconds = 187L,
                viewCount = 2300000000L,
                publishedAt = "Mar 5, 2007",
                streamUrl = "kXYiU_JCYtU",
                sourceType = VideoSourceType.YOUTUBE
            )
        )
    }

    private fun logD(tag: String, msg: String) {
        try { Log.d(tag, msg) } catch (e: Throwable) {}
    }
    private fun logI(tag: String, msg: String) {
        try { Log.i(tag, msg) } catch (e: Throwable) {}
    }
    private fun logW(tag: String, msg: String) {
        try { Log.w(tag, msg) } catch (e: Throwable) {}
    }
    private fun logE(tag: String, msg: String) {
        try { Log.e(tag, msg) } catch (e: Throwable) {}
    }

    /**
     * Retrieves active YouTube Data API Key.
     * Priority:
     * 1. App SharedPreferences (configured dynamically in UI)
     * 2. BuildConfig.YOUTUBE_API_KEY (configured in local.properties)
     */
    fun getApiKey(): String {
        val prefsKey = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            ?.getString(KEY_API_KEY, null)
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
        if (prefsKey != null) return prefsKey

        val buildConfigKey = try {
            com.pulse.player.BuildConfig.YOUTUBE_API_KEY.trim()
        } catch (e: Throwable) {
            ""
        }
        return buildConfigKey
    }

    fun setApiKey(key: String) {
        val sanitized = key.trim()
        context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            ?.edit()
            ?.putString(KEY_API_KEY, sanitized)
            ?.apply()
        logI(TAG, "YouTube Data API Key updated (length: ${sanitized.length})")
    }

    fun hasApiKey(): Boolean = getApiKey().isNotBlank()

    /**
     * Checks if input contains an explicit YouTube Video ID or URL.
     */
    fun extractVideoId(input: String): String? {
        val trimmed = input.trim()
        if (VIDEO_ID_REGEX.matcher(trimmed).matches()) {
            return trimmed
        }
        val matcher = YOUTUBE_URL_REGEX.matcher(trimmed)
        if (matcher.find()) {
            return matcher.group(1)
        }
        return null
    }

    /**
     * Searches YouTube videos using YouTube Data API v3.
     */
    suspend fun searchVideos(query: String): List<VideoItem> = withContext(Dispatchers.IO) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isBlank()) {
            return@withContext getTrendingVideos()
        }

        // Direct Video ID or URL detection
        val directVideoId = extractVideoId(trimmedQuery)
        if (directVideoId != null) {
            logI(TAG, "Direct YouTube link/ID recognized: $directVideoId")
            val directVideo = getVideoById(directVideoId) ?: VideoItem(
                id = directVideoId,
                title = "YouTube Video ($directVideoId)",
                description = "Direct YouTube video link.",
                thumbnailUrl = "https://img.youtube.com/vi/$directVideoId/hqdefault.jpg",
                channelTitle = "YouTube Creator",
                channelId = "youtube",
                durationSeconds = 0L,
                viewCount = 0L,
                publishedAt = "YouTube",
                streamUrl = directVideoId,
                sourceType = VideoSourceType.YOUTUBE
            )
            return@withContext listOf(directVideo)
        }

        // Verify API key configuration
        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            logW(TAG, "Search query '$trimmedQuery' blocked: YouTube Data API v3 key is missing.")
            throw YouTubeApiException(
                message = "YouTube Data API v3 Key is not configured.\n\n" +
                        "The official YouTube IFrame Player handles playback, but searching YouTube requires a Google Cloud YouTube Data API v3 key.\n\n" +
                        "Please configure your API key in Settings or local.properties.",
                errorCode = YouTubeErrorCode.MISSING_API_KEY
            )
        }

        val encodedQuery = URLEncoder.encode(trimmedQuery, "UTF-8")
        val url = "https://www.googleapis.com/youtube/v3/search?part=snippet&type=video&maxResults=25&q=$encodedQuery&key=$apiKey"
        logD(TAG, "Executing YouTube Data API search for '$trimmedQuery' [URL: ...&key=***]")

        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "PulsePlayer-Android/1.0")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val code = response.code
                val bodyString = response.body?.string().orEmpty()

                if (response.isSuccessful) {
                    val items = parseSearchResponse(bodyString)
                    logI(TAG, "YouTube search succeeded: retrieved ${items.size} videos for '$trimmedQuery'")
                    return@withContext items
                } else {
                    val (errorMessage, errorCode) = parseErrorResponse(code, bodyString)
                    logE(TAG, "YouTube search failed with HTTP $code: $errorMessage")
                    throw YouTubeApiException(errorMessage, errorCode, code)
                }
            }
        } catch (e: YouTubeApiException) {
            throw e
        } catch (e: IOException) {
            logE(TAG, "Network failure during YouTube search: ${e.message}")
            throw YouTubeApiException(
                message = "Network error: Unable to connect to YouTube. Please check your internet connection.",
                errorCode = YouTubeErrorCode.NETWORK_ERROR,
                cause = e
            )
        } catch (e: Exception) {
            logE(TAG, "Unexpected error during YouTube search: ${e.message}")
            throw YouTubeApiException(
                message = "Unexpected error searching YouTube: ${e.message}",
                errorCode = YouTubeErrorCode.UNKNOWN,
                cause = e
            )
        }
    }

    /**
     * Retrieves Trending videos from YouTube Data API v3 or sample fallback.
     */
    suspend fun getTrendingVideos(): List<VideoItem> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            logI(TAG, "No API key configured for live trending; serving curated starter videos.")
            return@withContext SAMPLE_YOUTUBE_VIDEOS
        }

        val url = "https://www.googleapis.com/youtube/v3/videos?part=snippet,statistics,contentDetails&chart=mostPopular&regionCode=US&maxResults=20&key=$apiKey"
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "PulsePlayer-Android/1.0")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    val items = parseVideosResponse(body)
                    if (items.isNotEmpty()) {
                        logI(TAG, "Retrieved ${items.size} live trending videos from YouTube.")
                        return@withContext items
                    }
                } else {
                    val (errorMsg, _) = parseErrorResponse(response.code, response.body?.string().orEmpty())
                    logW(TAG, "Trending fetch returned HTTP ${response.code}: $errorMsg. Falling back to starter videos.")
                }
            }
        } catch (e: Exception) {
            logW(TAG, "Failed to load live trending: ${e.message}. Using starter videos.")
        }
        return@withContext SAMPLE_YOUTUBE_VIDEOS
    }

    /**
     * Retrieves details for a specific video by ID.
     */
    suspend fun getVideoById(id: String): VideoItem? = withContext(Dispatchers.IO) {
        val localMatch = SAMPLE_YOUTUBE_VIDEOS.find { it.id == id }
        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            return@withContext localMatch ?: createFallbackItem(id)
        }

        val url = "https://www.googleapis.com/youtube/v3/videos?part=snippet,statistics,contentDetails&id=$id&key=$apiKey"
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "PulsePlayer-Android/1.0")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    val items = parseVideosResponse(body)
                    if (items.isNotEmpty()) {
                        return@withContext items.first()
                    }
                }
            }
        } catch (e: Exception) {
            logW(TAG, "Failed to fetch video details for $id: ${e.message}")
        }
        return@withContext localMatch ?: createFallbackItem(id)
    }

    private fun createFallbackItem(id: String): VideoItem {
        return VideoItem(
            id = id,
            title = "YouTube Video ($id)",
            description = "Playing via official YouTube IFrame Player API. Subject to YouTube Terms of Service.",
            thumbnailUrl = "https://img.youtube.com/vi/$id/hqdefault.jpg",
            channelTitle = "YouTube Creator",
            channelId = "UC_$id",
            durationSeconds = 0L,
            viewCount = 0L,
            publishedAt = "YouTube",
            streamUrl = id,
            sourceType = VideoSourceType.YOUTUBE
        )
    }

    fun parseSearchResponse(jsonString: String): List<VideoItem> {
        val results = mutableListOf<VideoItem>()
        try {
            val root = JSONObject(jsonString)
            val items = root.optJSONArray("items") ?: return emptyList()

            for (i in 0 until items.length()) {
                val item = items.optJSONObject(i) ?: continue
                val idObj = item.optJSONObject("id") ?: continue
                val videoId = idObj.optString("videoId").takeIf { it.isNotBlank() } ?: continue

                val snippet = item.optJSONObject("snippet") ?: continue
                val title = unescapeHtml(snippet.optString("title", "Untitled Video"))
                val description = unescapeHtml(snippet.optString("description", ""))
                val channelTitle = unescapeHtml(snippet.optString("channelTitle", "Unknown Channel"))
                val channelId = snippet.optString("channelId", "")
                val publishedAt = snippet.optString("publishedAt", "")

                val thumbnails = snippet.optJSONObject("thumbnails")
                val thumbUrl = thumbnails?.optJSONObject("high")?.optString("url")
                    ?: thumbnails?.optJSONObject("medium")?.optString("url")
                    ?: thumbnails?.optJSONObject("default")?.optString("url")
                    ?: "https://img.youtube.com/vi/$videoId/hqdefault.jpg"

                results.add(
                    VideoItem(
                        id = videoId,
                        title = title,
                        description = description,
                        thumbnailUrl = thumbUrl,
                        channelTitle = channelTitle,
                        channelId = channelId,
                        durationSeconds = 0L,
                        viewCount = 0L,
                        publishedAt = formatPublishedDate(publishedAt),
                        streamUrl = videoId,
                        sourceType = VideoSourceType.YOUTUBE
                    )
                )
            }
        } catch (e: Exception) {
            logE(TAG, "Failed to parse YouTube search JSON: ${e.message}")
            throw YouTubeApiException("Failed to parse YouTube response.", YouTubeErrorCode.PARSING_ERROR, cause = e)
        }
        return results
    }

    fun parseVideosResponse(jsonString: String): List<VideoItem> {
        val results = mutableListOf<VideoItem>()
        try {
            val root = JSONObject(jsonString)
            val items = root.optJSONArray("items") ?: return emptyList()

            for (i in 0 until items.length()) {
                val item = items.optJSONObject(i) ?: continue
                val videoId = item.optString("id").takeIf { it.isNotBlank() } ?: continue
                val snippet = item.optJSONObject("snippet") ?: continue

                val title = unescapeHtml(snippet.optString("title", "Untitled Video"))
                val description = unescapeHtml(snippet.optString("description", ""))
                val channelTitle = unescapeHtml(snippet.optString("channelTitle", "Unknown Channel"))
                val channelId = snippet.optString("channelId", "")
                val publishedAt = snippet.optString("publishedAt", "")

                val thumbnails = snippet.optJSONObject("thumbnails")
                val thumbUrl = thumbnails?.optJSONObject("maxres")?.optString("url")
                    ?: thumbnails?.optJSONObject("high")?.optString("url")
                    ?: thumbnails?.optJSONObject("medium")?.optString("url")
                    ?: "https://img.youtube.com/vi/$videoId/hqdefault.jpg"

                val statistics = item.optJSONObject("statistics")
                val viewCount = statistics?.optLong("viewCount", 0L) ?: 0L

                val contentDetails = item.optJSONObject("contentDetails")
                val durationIso = contentDetails?.optString("duration", "") ?: ""
                val durationSeconds = parseIsoDuration(durationIso)

                results.add(
                    VideoItem(
                        id = videoId,
                        title = title,
                        description = description,
                        thumbnailUrl = thumbUrl,
                        channelTitle = channelTitle,
                        channelId = channelId,
                        durationSeconds = durationSeconds,
                        viewCount = viewCount,
                        publishedAt = formatPublishedDate(publishedAt),
                        streamUrl = videoId,
                        sourceType = VideoSourceType.YOUTUBE
                    )
                )
            }
        } catch (e: Exception) {
            logE(TAG, "Failed to parse YouTube videos JSON: ${e.message}")
        }
        return results
    }

    fun parseErrorResponse(code: Int, body: String): Pair<String, YouTubeErrorCode> {
        var reason = ""
        var message = ""
        try {
            val root = JSONObject(body)
            val error = root.optJSONObject("error")
            if (error != null) {
                message = error.optString("message", "")
                val errorsArr = error.optJSONArray("errors")
                if (errorsArr != null && errorsArr.length() > 0) {
                    val first = errorsArr.optJSONObject(0)
                    reason = first?.optString("reason", "") ?: ""
                }
            }
        } catch (e: Exception) {
            // Non-JSON error body
        }

        return when {
            code == 400 || reason == "keyInvalid" || reason == "badRequest" -> {
                Pair(
                    "Invalid YouTube Data API Key. Please verify your Google Cloud API key in Settings.",
                    YouTubeErrorCode.INVALID_API_KEY
                )
            }
            code == 403 && (reason == "quotaExceeded" || reason == "dailyLimitExceeded") -> {
                Pair(
                    "YouTube Data API daily quota exceeded (10,000 units/day limit). Quota resets at midnight Pacific Time.",
                    YouTubeErrorCode.QUOTA_EXCEEDED
                )
            }
            code == 403 && (reason == "accessNotConfigured" || message.contains("blocked", ignoreCase = true) || message.contains("not been used", ignoreCase = true)) -> {
                Pair(
                    "YouTube Data API v3 is not enabled or requests are blocked for this key. Please enable 'YouTube Data API v3' in your Google Cloud Console project.",
                    YouTubeErrorCode.API_NOT_ENABLED
                )
            }
            code == 403 -> {
                val detail = if (message.isNotBlank()) message else "Check API key restrictions in Google Cloud Console."
                Pair("YouTube API access forbidden (HTTP 403): $detail", YouTubeErrorCode.INVALID_API_KEY)
            }
            else -> {
                val detail = if (message.isNotBlank()) message else "Request failed"
                Pair("YouTube API error (HTTP $code): $detail", YouTubeErrorCode.UNKNOWN)
            }
        }
    }

    /**
     * Parses ISO-8601 duration string (e.g. PT4M13S) into seconds.
     */
    fun parseIsoDuration(durationStr: String): Long {
        if (durationStr.isBlank()) return 0L
        try {
            var seconds = 0L
            val matcher = Pattern.compile("PT(?:(\\d+)H)?(?:(\\d+)M)?(?:(\\d+)S)?").matcher(durationStr)
            if (matcher.matches()) {
                val hours = matcher.group(1)?.toLongOrNull() ?: 0L
                val minutes = matcher.group(2)?.toLongOrNull() ?: 0L
                val secs = matcher.group(3)?.toLongOrNull() ?: 0L
                seconds = hours * 3600 + minutes * 60 + secs
            }
            return seconds
        } catch (e: Exception) {
            return 0L
        }
    }

    private fun formatPublishedDate(rawDate: String): String {
        return if (rawDate.length >= 10) rawDate.substring(0, 10) else rawDate
    }

    private fun unescapeHtml(text: String): String {
        return text
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&apos;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
    }
}
