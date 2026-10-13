package com.pulse.player

import com.pulse.player.data.model.VideoSourceType
import com.pulse.player.domain.source.LocalFileVideoSource
import com.pulse.player.domain.source.RemoteStreamVideoSource
import com.pulse.player.domain.source.YouTubeVideoSource
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VideoSourceTest {

    @Test
    fun testYouTubeSource_enforcesComplianceRestrictions() = runBlocking {
        val ytSource = YouTubeVideoSource()

        assertEquals(VideoSourceType.YOUTUBE, ytSource.sourceType)
        assertFalse("YouTube source must prohibit background stream extraction", ytSource.supportsBackgroundPlayback)
        assertFalse("YouTube source must prohibit unauthorized video downloads", ytSource.supportsDownloads)
        assertTrue("YouTube source supports Picture-in-Picture window", ytSource.supportsPictureInPicture)

        val details = ytSource.getVideoDetails("dQw4w9WgXcQ")
        assertTrue(details.isSuccess)
        val videoItem = details.getOrNull()
        assertNotNull(videoItem)
        assertEquals("dQw4w9WgXcQ", videoItem?.id)
        assertFalse(videoItem?.isDownloadable ?: true)
    }

    @Test
    fun testRemoteStreamSource_permitsAuthorizedPlaybackAndDownload() = runBlocking {
        val remoteSource = RemoteStreamVideoSource()

        assertEquals(VideoSourceType.REMOTE_STREAM, remoteSource.sourceType)
        assertTrue(remoteSource.supportsBackgroundPlayback)
        assertTrue(remoteSource.supportsDownloads)
        assertTrue(remoteSource.supportsQualitySelection)

        val details = remoteSource.getVideoDetails("big_buck_bunny")
        assertTrue(details.isSuccess)
        val videoItem = details.getOrNull()
        assertNotNull(videoItem)
        assertTrue(videoItem?.isDownloadable ?: false)
        assertTrue(videoItem?.streamUrl?.contains("m3u8") ?: false)
    }

    @Test
    fun testLocalFileSource_capabilities() = runBlocking {
        val localSource = LocalFileVideoSource()

        assertEquals(VideoSourceType.LOCAL_FILE, localSource.sourceType)
        assertTrue(localSource.supportsBackgroundPlayback)
        assertFalse("Local files do not need re-downloading", localSource.supportsDownloads)
    }
}
