package com.pulse.player

import com.pulse.player.data.repository.YouTubeRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaRepositoryTest {

    private val youTubeRepository = YouTubeRepository()

    @Test
    fun testYouTubeRepository_getTrending_returnsNonEmptyList() = runBlocking {
        val trending = youTubeRepository.getTrendingVideos()
        assertTrue("Trending videos should not be empty", trending.isNotEmpty())
    }

    @Test
    fun testYouTubeRepository_search_matchesDirectIdOrUrl() = runBlocking {
        val searchResults = youTubeRepository.searchVideos("dQw4w9WgXcQ")
        assertTrue("Search for 'dQw4w9WgXcQ' should yield results", searchResults.isNotEmpty())
        assertEquals("dQw4w9WgXcQ", searchResults[0].id)
    }

    @Test
    fun testYouTubeRepository_getVideoById_returnsTargetVideo() = runBlocking {
        val video = youTubeRepository.getVideoById("dQw4w9WgXcQ")
        assertNotNull(video)
        assertEquals("dQw4w9WgXcQ", video?.id)
        assertTrue(video?.title?.contains("Rick Astley") ?: false)
    }
}
