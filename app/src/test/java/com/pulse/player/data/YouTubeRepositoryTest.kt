package com.pulse.player.data

import com.pulse.player.data.model.VideoSourceType
import com.pulse.player.data.repository.YouTubeApiException
import com.pulse.player.data.repository.YouTubeErrorCode
import com.pulse.player.data.repository.YouTubeRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class YouTubeRepositoryTest {

    private val repository = YouTubeRepository()

    @Test
    fun testExtractVideoId_validUrlsAndIds() {
        assertEquals("dQw4w9WgXcQ", repository.extractVideoId("dQw4w9WgXcQ"))
        assertEquals("dQw4w9WgXcQ", repository.extractVideoId("https://www.youtube.com/watch?v=dQw4w9WgXcQ"))
        assertEquals("dQw4w9WgXcQ", repository.extractVideoId("https://youtu.be/dQw4w9WgXcQ"))
        assertEquals("dQw4w9WgXcQ", repository.extractVideoId("http://youtube.com/embed/dQw4w9WgXcQ"))
        assertEquals("dQw4w9WgXcQ", repository.extractVideoId("https://www.youtube.com/shorts/dQw4w9WgXcQ"))
        assertNull(repository.extractVideoId("music"))
        assertNull(repository.extractVideoId("android developer tutorial"))
    }

    @Test
    fun testSearchVideos_directUrlOrId_resolvesDirectlyWithoutApiKey() = runBlocking {
        val resultsFromId = repository.searchVideos("dQw4w9WgXcQ")
        assertEquals(1, resultsFromId.size)
        assertEquals("dQw4w9WgXcQ", resultsFromId[0].id)
        assertEquals(VideoSourceType.YOUTUBE, resultsFromId[0].sourceType)

        val resultsFromUrl = repository.searchVideos("https://youtu.be/dQw4w9WgXcQ")
        assertEquals(1, resultsFromUrl.size)
        assertEquals("dQw4w9WgXcQ", resultsFromUrl[0].id)
    }

    @Test
    fun testSearchVideos_textQueryWithoutApiKey_throwsMissingApiKeyException() = runBlocking {
        try {
            repository.searchVideos("music")
            fail("Expected YouTubeApiException when searching text without API key")
        } catch (e: YouTubeApiException) {
            assertEquals(YouTubeErrorCode.MISSING_API_KEY, e.errorCode)
            assertTrue(e.message?.contains("YouTube Data API v3 Key is not configured") == true)
        }
    }

    @Test
    fun testParseSearchResponse_parsesRealYouTubeDataApiJsonCorrectly() {
        val json = """
        {
          "kind": "youtube#searchListResponse",
          "etag": "etag123",
          "nextPageToken": "CAUQAA",
          "pageInfo": { "totalResults": 1000, "resultsPerPage": 2 },
          "items": [
            {
              "kind": "youtube#searchResult",
              "id": { "kind": "youtube#video", "videoId": "9bZkp7q19f0" },
              "snippet": {
                "publishedAt": "2012-07-15T07:46:32Z",
                "channelId": "UCb--channelId123",
                "title": "PSY - GANGNAM STYLE (&quot;강남스타일&quot;) M/V",
                "description": "PSY - GANGNAM STYLE 10th Anniversary &amp; Remastered",
                "thumbnails": {
                  "default": { "url": "https://i.ytimg.com/vi/9bZkp7q19f0/default.jpg" },
                  "medium": { "url": "https://i.ytimg.com/vi/9bZkp7q19f0/mqdefault.jpg" },
                  "high": { "url": "https://i.ytimg.com/vi/9bZkp7q19f0/hqdefault.jpg" }
                },
                "channelTitle": "officialpsy"
              }
            }
          ]
        }
        """.trimIndent()

        val videos = repository.parseSearchResponse(json)
        assertEquals(1, videos.size)

        val video = videos[0]
        assertEquals("9bZkp7q19f0", video.id)
        assertEquals("PSY - GANGNAM STYLE (\"강남스타일\") M/V", video.title)
        assertEquals("PSY - GANGNAM STYLE 10th Anniversary & Remastered", video.description)
        assertEquals("officialpsy", video.channelTitle)
        assertEquals("https://i.ytimg.com/vi/9bZkp7q19f0/hqdefault.jpg", video.thumbnailUrl)
        assertEquals("2012-07-15", video.publishedAt)
        assertEquals(VideoSourceType.YOUTUBE, video.sourceType)
    }

    @Test
    fun testParseVideosResponse_parsesDurationAndStatistics() {
        val json = """
        {
          "items": [
            {
              "id": "dQw4w9WgXcQ",
              "snippet": {
                "publishedAt": "2009-10-25T06:57:33Z",
                "channelId": "UCuAXFkgsw1L7xaCfnd5JJOw",
                "title": "Rick Astley - Never Gonna Give You Up",
                "description": "Official Video",
                "thumbnails": {
                  "high": { "url": "https://img.youtube.com/vi/dQw4w9WgXcQ/hqdefault.jpg" }
                },
                "channelTitle": "Rick Astley"
              },
              "contentDetails": {
                "duration": "PT3M33S"
              },
              "statistics": {
                "viewCount": "1600000000"
              }
            }
          ]
        }
        """.trimIndent()

        val videos = repository.parseVideosResponse(json)
        assertEquals(1, videos.size)

        val video = videos[0]
        assertEquals("dQw4w9WgXcQ", video.id)
        assertEquals(213L, video.durationSeconds)
        assertEquals(1600000000L, video.viewCount)
    }

    @Test
    fun testParseErrorResponse_identifiesQuotaAndKeyErrors() {
        val quotaErrorJson = """
        {
          "error": {
            "code": 403,
            "message": "Quota exceeded",
            "errors": [
              {
                "message": "Quota exceeded",
                "domain": "youtube.quota",
                "reason": "quotaExceeded"
              }
            ]
          }
        }
        """.trimIndent()

        val (quotaMsg, quotaCode) = repository.parseErrorResponse(403, quotaErrorJson)
        assertEquals(YouTubeErrorCode.QUOTA_EXCEEDED, quotaCode)
        assertTrue(quotaMsg.contains("quota exceeded"))

        val invalidKeyJson = """
        {
          "error": {
            "code": 400,
            "message": "API key not valid",
            "errors": [
              {
                "message": "API key not valid",
                "domain": "global",
                "reason": "keyInvalid"
              }
            ]
          }
        }
        """.trimIndent()

        val (invalidKeyMsg, invalidKeyCode) = repository.parseErrorResponse(400, invalidKeyJson)
        assertEquals(YouTubeErrorCode.INVALID_API_KEY, invalidKeyCode)
        assertTrue(invalidKeyMsg.contains("Invalid YouTube Data API Key"))

        val blockedJson = """
        {
          "error": {
            "code": 403,
            "message": "Requests to this API youtube method youtube.api.v3.V3DataSearchService.List are blocked.",
            "errors": [
              {
                "message": "Requests to this API youtube method youtube.api.v3.V3DataSearchService.List are blocked.",
                "domain": "global",
                "reason": "forbidden"
              }
            ]
          }
        }
        """.trimIndent()

        val (blockedMsg, blockedCode) = repository.parseErrorResponse(403, blockedJson)
        assertEquals(YouTubeErrorCode.API_NOT_ENABLED, blockedCode)
        assertTrue(blockedMsg.contains("YouTube Data API v3 is not enabled or requests are blocked"))
    }

    @Test
    fun testParseIsoDuration_handlesVariousFormats() {
        assertEquals(213L, repository.parseIsoDuration("PT3M33S"))
        assertEquals(3725L, repository.parseIsoDuration("PT1H2M5S"))
        assertEquals(45L, repository.parseIsoDuration("PT45S"))
        assertEquals(3600L, repository.parseIsoDuration("PT1H"))
        assertEquals(0L, repository.parseIsoDuration(""))
        assertEquals(0L, repository.parseIsoDuration("invalid"))
    }
}
