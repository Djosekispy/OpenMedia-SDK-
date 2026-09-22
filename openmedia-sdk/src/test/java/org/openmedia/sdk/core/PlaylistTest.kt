package org.openmedia.sdk.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PlaylistTest {

    private lateinit var playlist: Playlist
    private lateinit var item1: MediaItem
    private lateinit var item2: MediaItem
    private lateinit var item3: MediaItem

    @Before
    fun setUp() {
        playlist = Playlist(id = "pl_1", name = "My Rock Playlist", description = "Classic Rock Collection")
        item1 = MediaItem("1", "Song 1", MediaType.AUDIO, MediaSource("https://example.com/1.mp3"))
        item2 = MediaItem("2", "Song 2", MediaType.AUDIO, MediaSource("https://example.com/2.mp3"))
        item3 = MediaItem("3", "Song 3", MediaType.AUDIO, MediaSource("https://example.com/3.mp3"))
    }

    @Test
    fun testPlaylistCreation() {
        assertEquals("pl_1", playlist.id)
        assertEquals("My Rock Playlist", playlist.name)
        assertEquals("Classic Rock Collection", playlist.description)
        assertTrue(playlist.isEmpty)
        assertEquals(0, playlist.size)
    }

    @Test
    fun testAddAndGetItems() {
        playlist.add(item1)
        playlist.addAll(listOf(item2, item3))

        assertEquals(3, playlist.size)
        assertEquals(item1, playlist.get(0))
        assertEquals(item2, playlist.get(1))
        assertEquals(item3, playlist.get(2))
        assertNull(playlist.get(10))
    }

    @Test
    fun testRemoveById() {
        playlist.addAll(listOf(item1, item2, item3))
        val removed = playlist.remove("2")
        assertTrue(removed)
        assertEquals(2, playlist.size)
        assertEquals(listOf("1", "3"), playlist.items.map { it.id })

        assertFalse(playlist.remove("non_existing"))
    }

    @Test
    fun testRemoveAt() {
        playlist.addAll(listOf(item1, item2, item3))
        val removed = playlist.removeAt(0)
        assertEquals(item1, removed)
        assertEquals(2, playlist.size)
        assertEquals("2", playlist.items[0].id)
    }

    @Test
    fun testMoveItem() {
        playlist.addAll(listOf(item1, item2, item3)) // [1, 2, 3]
        val moved = playlist.move(0, 2) // move 1 to the end -> [2, 3, 1]
        assertTrue(moved)
        assertEquals(listOf("2", "3", "1"), playlist.items.map { it.id })
    }

    @Test
    fun testClear() {
        playlist.addAll(listOf(item1, item2, item3))
        assertEquals(3, playlist.size)
        playlist.clear()
        assertEquals(0, playlist.size)
        assertTrue(playlist.isEmpty)
    }
}
