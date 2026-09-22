package org.openmedia.sdk.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaItemTest {

    @Test
    fun testAudioMediaItemCreation() {
        val source = MediaSource(
            uri = "https://example.com/audio/song.mp3",
            mimeType = "audio/mpeg",
            quality = "320kbps",
            headers = mapOf("Authorization" to "Bearer token")
        )

        val item = MediaItem(
            id = "track_101",
            title = "Bohemian Rhapsody",
            type = MediaType.AUDIO,
            source = source,
            duration = 354000L,
            metadata = mapOf(
                MediaItem.METADATA_ARTIST to "Queen",
                MediaItem.METADATA_ALBUM to "A Night at the Opera"
            )
        )

        assertEquals("track_101", item.id)
        assertEquals("Bohemian Rhapsody", item.title)
        assertEquals(MediaType.AUDIO, item.type)
        assertEquals(354000L, item.duration)
        assertEquals("Queen", item.artist)
        assertEquals("A Night at the Opera", item.album)
        assertEquals("320kbps", item.source.quality)
        assertEquals("audio/mpeg", item.source.mimeType)
    }

    @Test
    fun testVideoMediaItemCreation() {
        val source = MediaSource(
            uri = "https://example.com/video/trailer.mp4",
            mimeType = "video/mp4",
            quality = "1080p"
        )

        val item = MediaItem(
            id = "vid_202",
            title = "OpenMedia Trailer",
            type = MediaType.VIDEO,
            source = source,
            duration = 120000L
        )

        assertEquals("vid_202", item.id)
        assertEquals("OpenMedia Trailer", item.title)
        assertEquals(MediaType.VIDEO, item.type)
        assertEquals(120000L, item.duration)
        assertNull(item.artist)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testBlankIdThrowsException() {
        MediaItem(
            id = "   ",
            title = "Title",
            type = MediaType.AUDIO,
            source = MediaSource("https://example.com/song.mp3")
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun testBlankTitleThrowsException() {
        MediaItem(
            id = "valid_id",
            title = "",
            type = MediaType.AUDIO,
            source = MediaSource("https://example.com/song.mp3")
        )
    }
}
