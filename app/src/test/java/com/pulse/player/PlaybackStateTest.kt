package com.pulse.player

import com.pulse.player.data.model.PlaybackState
import com.pulse.player.data.model.PlayerState
import com.pulse.player.data.model.StreamQuality
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackStateTest {

    @Test
    fun testPlayerState_initialValues() {
        val state = PlayerState()
        assertFalse(state.isPlaying)
        assertEquals(PlaybackState.IDLE, state.playbackState)
        assertEquals(0L, state.currentPositionMs)
        assertEquals(1.0f, state.playbackSpeed)
        assertEquals(StreamQuality.AUTO, state.selectedQuality)
    }

    @Test
    fun testPlayerState_stateTransitions() {
        val state = PlayerState(
            isPlaying = true,
            playbackState = PlaybackState.READY,
            currentPositionMs = 15000L,
            durationMs = 60000L,
            playbackSpeed = 1.25f
        )
        assertTrue(state.isPlaying)
        assertEquals(PlaybackState.READY, state.playbackState)
        assertEquals(15000L, state.currentPositionMs)
        assertEquals(60000L, state.durationMs)
        assertEquals(1.25f, state.playbackSpeed)
    }

    @Test
    fun testStreamQuality_sortingAndFormatting() {
        val q1 = StreamQuality("1080p", 1920, 1080, 5000000L, "video/mp4", 0)
        val q2 = StreamQuality("720p", 1280, 720, 2500000L, "video/mp4", 1)
        val q3 = StreamQuality.AUTO

        val list = listOf(q2, q1, q3)
        val sorted = list.sortedByDescending { it.height }

        assertEquals("1080p", sorted[0].label)
        assertEquals("720p", sorted[1].label)
        assertEquals("Auto", sorted[2].label)
    }
}
