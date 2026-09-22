package org.openmedia.sdk.player

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.openmedia.sdk.core.PlaybackState

class PlaybackStateTest {

    @Test
    fun testAllRequiredPlaybackStatesAreDefined() {
        val states = PlaybackState.values().map { it.name }.toSet()
        val expectedStates = setOf(
            "IDLE",
            "LOADING",
            "PLAYING",
            "PAUSED",
            "BUFFERING",
            "COMPLETED",
            "ERROR"
        )

        assertEquals(expectedStates, states)
    }

    @Test
    fun testPlaybackStateValues() {
        assertEquals(PlaybackState.IDLE, PlaybackState.valueOf("IDLE"))
        assertEquals(PlaybackState.LOADING, PlaybackState.valueOf("LOADING"))
        assertEquals(PlaybackState.PLAYING, PlaybackState.valueOf("PLAYING"))
        assertEquals(PlaybackState.PAUSED, PlaybackState.valueOf("PAUSED"))
        assertEquals(PlaybackState.BUFFERING, PlaybackState.valueOf("BUFFERING"))
        assertEquals(PlaybackState.COMPLETED, PlaybackState.valueOf("COMPLETED"))
        assertEquals(PlaybackState.ERROR, PlaybackState.valueOf("ERROR"))
    }
}
