package org.openmedia.sdk.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class QueueTest {

    private lateinit var queue: Queue
    private lateinit var trackA: MediaItem
    private lateinit var trackB: MediaItem
    private lateinit var trackC: MediaItem

    @Before
    fun setUp() {
        trackA = MediaItem(
            id = "A",
            title = "Track A",
            type = MediaType.AUDIO,
            source = MediaSource("https://example.com/a.mp3")
        )
        trackB = MediaItem(
            id = "B",
            title = "Track B",
            type = MediaType.AUDIO,
            source = MediaSource("https://example.com/b.mp3")
        )
        trackC = MediaItem(
            id = "C",
            title = "Track C",
            type = MediaType.AUDIO,
            source = MediaSource("https://example.com/c.mp3")
        )
        queue = Queue()
    }

    @Test
    fun testAddItem() {
        assertTrue(queue.isEmpty)
        queue.add(trackA)
        assertEquals(1, queue.size)
        assertFalse(queue.isEmpty)
        assertEquals(trackA, queue.current())
        assertEquals(0, queue.currentIndex)
    }

    @Test
    fun testAddAllItems() {
        queue.addAll(listOf(trackA, trackB, trackC))
        assertEquals(3, queue.size)
        assertEquals(trackA, queue.current())
        assertEquals(0, queue.currentIndex)
    }

    @Test
    fun testNextAndPreviousNavigation() {
        queue.addAll(listOf(trackA, trackB, trackC))

        assertTrue(queue.hasNext)
        assertFalse(queue.hasPrevious)

        val second = queue.next()
        assertEquals(trackB, second)
        assertEquals(trackB, queue.current())
        assertEquals(1, queue.currentIndex)
        assertTrue(queue.hasNext)
        assertTrue(queue.hasPrevious)

        val third = queue.next()
        assertEquals(trackC, third)
        assertEquals(2, queue.currentIndex)
        assertFalse(queue.hasNext)
        assertTrue(queue.hasPrevious)

        // Beyond end returns null
        val beyond = queue.next()
        assertNull(beyond)
        assertEquals(trackC, queue.current())

        // Navigate backwards
        val backToSecond = queue.previous()
        assertEquals(trackB, backToSecond)
        assertEquals(1, queue.currentIndex)

        val backToFirst = queue.previous()
        assertEquals(trackA, backToFirst)
        assertEquals(0, queue.currentIndex)

        // Before start returns null
        val beforeStart = queue.previous()
        assertNull(beforeStart)
        assertEquals(trackA, queue.current())
    }

    @Test
    fun testRemoveById() {
        queue.addAll(listOf(trackA, trackB, trackC))
        val removed = queue.remove("B")
        assertTrue(removed)
        assertEquals(2, queue.size)
        assertEquals(listOf(trackA, trackC), queue.items)

        val notFound = queue.remove("NonExistent")
        assertFalse(notFound)
    }

    @Test
    fun testRemoveCurrentItemMaintainsValidIndex() {
        queue.addAll(listOf(trackA, trackB, trackC))
        queue.next() // active is B (index 1)

        val removed = queue.removeAt(1)
        assertEquals(trackB, removed)
        assertEquals(2, queue.size)
        // Now current should be trackC at index 1
        assertEquals(trackC, queue.current())
        assertEquals(1, queue.currentIndex)
    }

    @Test
    fun testMoveItem() {
        queue.addAll(listOf(trackA, trackB, trackC)) // [A, B, C]
        // Move track C from index 2 to index 0 -> [C, A, B]
        val moved = queue.move(2, 0)
        assertTrue(moved)
        assertEquals(listOf("C", "A", "B"), queue.items.map { it.id })

        // Check active pointer still points to original active item (track A, now at index 1)
        assertEquals(trackA, queue.current())
        assertEquals(1, queue.currentIndex)
    }

    @Test
    fun testClear() {
        queue.addAll(listOf(trackA, trackB, trackC))
        assertEquals(3, queue.size)

        queue.clear()
        assertEquals(0, queue.size)
        assertTrue(queue.isEmpty)
        assertNull(queue.current())
        assertEquals(-1, queue.currentIndex)
    }

    @Test
    fun testJumpTo() {
        queue.addAll(listOf(trackA, trackB, trackC))
        val jumped = queue.jumpTo(2)
        assertEquals(trackC, jumped)
        assertEquals(trackC, queue.current())
        assertEquals(2, queue.currentIndex)

        val invalidJump = queue.jumpTo(99)
        assertNull(invalidJump)
        assertEquals(trackC, queue.current())
    }
}
