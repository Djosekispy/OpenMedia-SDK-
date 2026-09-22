package org.openmedia.sdk.core

/**
 * Represents an ordered, user- or provider-defined collection of [MediaItem]s.
 *
 * Encapsulates playlist metadata (id, name, description) along with ordered track manipulation.
 *
 * @property id Unique identifier of the playlist.
 * @property name Title / name of the playlist.
 * @property description Optional descriptive text.
 */
class Playlist(
    val id: String,
    var name: String,
    var description: String? = null,
    initialItems: List<MediaItem> = emptyList()
) {
    init {
        require(id.isNotBlank()) { "Playlist id must not be blank" }
        require(name.isNotBlank()) { "Playlist name must not be blank" }
    }

    private val lock = Any()
    private val _items = ArrayList<MediaItem>(initialItems)

    /**
     * Immutable snapshot of media items in the playlist.
     */
    val items: List<MediaItem>
        get() = synchronized(lock) { ArrayList(_items) }

    /**
     * Total number of items in the playlist.
     */
    val size: Int
        get() = synchronized(lock) { _items.size }

    /**
     * Returns true if the playlist contains no items.
     */
    val isEmpty: Boolean
        get() = synchronized(lock) { _items.isEmpty() }

    /**
     * Appends an item to the playlist.
     */
    fun add(item: MediaItem) = synchronized(lock) {
        _items.add(item)
    }

    /**
     * Appends a collection of items to the playlist.
     */
    fun addAll(newItems: Collection<MediaItem>) = synchronized(lock) {
        _items.addAll(newItems)
    }

    /**
     * Inserts an item at a specific [index].
     */
    fun insert(index: Int, item: MediaItem) = synchronized(lock) {
        val clampedIndex = index.coerceIn(0, _items.size)
        _items.add(clampedIndex, item)
    }

    /**
     * Removes the item with matching [id].
     *
     * @return true if the item was found and removed, false otherwise.
     */
    fun remove(id: String): Boolean = synchronized(lock) {
        val index = _items.indexOfFirst { it.id == id }
        if (index == -1) return false
        _items.removeAt(index)
        true
    }

    /**
     * Removes and returns the item at [index], or null if index is out of bounds.
     */
    fun removeAt(index: Int): MediaItem? = synchronized(lock) {
        if (index !in _items.indices) return null
        _items.removeAt(index)
    }

    /**
     * Clears all items from the playlist.
     */
    fun clear() = synchronized(lock) {
        _items.clear()
    }

    /**
     * Moves an item from [fromIndex] to [toIndex].
     *
     * @return true if indices are valid and item was moved, false otherwise.
     */
    fun move(fromIndex: Int, toIndex: Int): Boolean = synchronized(lock) {
        if (fromIndex !in _items.indices || toIndex !in _items.indices || fromIndex == toIndex) {
            return false
        }
        val item = _items.removeAt(fromIndex)
        _items.add(toIndex, item)
        true
    }

    /**
     * Retrieves the item at [index], or null if out of bounds.
     */
    fun get(index: Int): MediaItem? = synchronized(lock) {
        if (index in _items.indices) _items[index] else null
    }

    override fun toString(): String {
        return "Playlist(id='$id', name='$name', size=$size)"
    }
}
