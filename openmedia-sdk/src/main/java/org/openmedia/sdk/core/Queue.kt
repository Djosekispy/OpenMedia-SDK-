package org.openmedia.sdk.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages an ordered sequential playback queue with pointer navigation.
 *
 * Supports adding, removing, reordering, and navigating back and forth across tracks.
 * Emits reactive state updates via [itemsFlow] and [currentItemFlow].
 */
class Queue(initialItems: List<MediaItem> = emptyList()) {

    private val lock = Any()
    private val _items = ArrayList<MediaItem>(initialItems)
    private var _currentIndex: Int = if (initialItems.isNotEmpty()) 0 else -1

    private val _itemsFlow = MutableStateFlow<List<MediaItem>>(initialItems.toList())
    val itemsFlow: StateFlow<List<MediaItem>> = _itemsFlow.asStateFlow()

    private val _currentItemFlow = MutableStateFlow<MediaItem?>(
        if (_currentIndex in initialItems.indices) initialItems[_currentIndex] else null
    )
    val currentItemFlow: StateFlow<MediaItem?> = _currentItemFlow.asStateFlow()

    /**
     * Snapshot list of items currently in the playback queue.
     */
    val items: List<MediaItem>
        get() = synchronized(lock) { ArrayList(_items) }

    /**
     * Zero-based index of the currently active media item, or -1 if empty.
     */
    val currentIndex: Int
        get() = synchronized(lock) { _currentIndex }

    /**
     * Total number of items in the queue.
     */
    val size: Int
        get() = synchronized(lock) { _items.size }

    /**
     * Returns true if the queue contains no items.
     */
    val isEmpty: Boolean
        get() = synchronized(lock) { _items.isEmpty() }

    /**
     * Returns true if there is a next track available after current position.
     */
    val hasNext: Boolean
        get() = synchronized(lock) { _currentIndex in 0 until (_items.size - 1) }

    /**
     * Returns true if there is a previous track available before current position.
     */
    val hasPrevious: Boolean
        get() = synchronized(lock) { _currentIndex > 0 && _items.isNotEmpty() }

    /**
     * Appends a new [MediaItem] to the end of the queue.
     */
    fun add(item: MediaItem) = synchronized(lock) {
        _items.add(item)
        if (_currentIndex == -1) {
            _currentIndex = 0
        }
        notifyChanges()
    }

    /**
     * Appends a collection of [MediaItem]s to the end of the queue.
     */
    fun addAll(newItems: Collection<MediaItem>) = synchronized(lock) {
        if (newItems.isEmpty()) return
        _items.addAll(newItems)
        if (_currentIndex == -1 && _items.isNotEmpty()) {
            _currentIndex = 0
        }
        notifyChanges()
    }

    /**
     * Inserts an item at a specific [index].
     */
    fun insert(index: Int, item: MediaItem) = synchronized(lock) {
        val clampedIndex = index.coerceIn(0, _items.size)
        _items.add(clampedIndex, item)
        if (_currentIndex == -1) {
            _currentIndex = 0
        } else if (clampedIndex <= _currentIndex) {
            _currentIndex++
        }
        notifyChanges()
    }

    /**
     * Removes the item matching the provided [id].
     *
     * @return true if the item was found and removed, false otherwise.
     */
    fun remove(id: String): Boolean = synchronized(lock) {
        val index = _items.indexOfFirst { it.id == id }
        if (index == -1) return false
        removeAt(index) != null
    }

    /**
     * Removes and returns the item located at [index].
     */
    fun removeAt(index: Int): MediaItem? = synchronized(lock) {
        if (index !in _items.indices) return null
        val removed = _items.removeAt(index)
        when {
            _items.isEmpty() -> {
                _currentIndex = -1
            }
            index < _currentIndex -> {
                _currentIndex--
            }
            index == _currentIndex -> {
                if (_currentIndex >= _items.size) {
                    _currentIndex = _items.size - 1
                }
            }
        }
        notifyChanges()
        removed
    }

    /**
     * Clears all items from the queue and resets the index.
     */
    fun clear() = synchronized(lock) {
        _items.clear()
        _currentIndex = -1
        notifyChanges()
    }

    /**
     * Moves an item from [fromIndex] to [toIndex].
     *
     * @return true if the move was successfully performed.
     */
    fun move(fromIndex: Int, toIndex: Int): Boolean = synchronized(lock) {
        if (fromIndex !in _items.indices || toIndex !in _items.indices || fromIndex == toIndex) {
            return false
        }
        val currentActiveItem = current()
        val item = _items.removeAt(fromIndex)
        _items.add(toIndex, item)

        // Restore currentIndex to keep track of the currently active item
        _currentIndex = if (currentActiveItem != null) {
            _items.indexOfFirst { it.id == currentActiveItem.id }
        } else {
            -1
        }
        notifyChanges()
        true
    }

    /**
     * Returns the currently active item, or null if the queue is empty.
     */
    fun current(): MediaItem? = synchronized(lock) {
        if (_currentIndex in _items.indices) _items[_currentIndex] else null
    }

    /**
     * Advances the active pointer to the next track and returns it, or null if at the end.
     */
    fun next(): MediaItem? = synchronized(lock) {
        if (hasNext) {
            _currentIndex++
            notifyChanges()
            _items[_currentIndex]
        } else {
            null
        }
    }

    /**
     * Moves the active pointer to the previous track and returns it, or null if at the beginning.
     */
    fun previous(): MediaItem? = synchronized(lock) {
        if (hasPrevious) {
            _currentIndex--
            notifyChanges()
            _items[_currentIndex]
        } else {
            null
        }
    }

    /**
     * Jumps pointer directly to [index] and returns the item at that position.
     */
    fun jumpTo(index: Int): MediaItem? = synchronized(lock) {
        if (index in _items.indices) {
            _currentIndex = index
            notifyChanges()
            _items[_currentIndex]
        } else {
            null
        }
    }

    private fun notifyChanges() {
        val snapshot = ArrayList(_items)
        val active = if (_currentIndex in snapshot.indices) snapshot[_currentIndex] else null
        _itemsFlow.value = snapshot
        _currentItemFlow.value = active
    }
}
