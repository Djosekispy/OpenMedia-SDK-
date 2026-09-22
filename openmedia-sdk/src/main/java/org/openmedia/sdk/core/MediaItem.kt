package org.openmedia.sdk.core

/**
 * Fundamental model representing any multimedia entity in the OpenMedia SDK.
 *
 * @property id Unique identifier for the media item.
 * @property title Human-readable display title.
 * @property type Media type classification ([MediaType.AUDIO] or [MediaType.VIDEO]).
 * @property source Destination source location and playback parameters.
 * @property duration Total duration in milliseconds, or null if unknown / live stream.
 * @property metadata Arbitrary extensible key-value metadata (e.g. artist, album, thumbnailUri).
 */
data class MediaItem(
    val id: String,
    val title: String,
    val type: MediaType,
    val source: MediaSource,
    val duration: Long? = null,
    val metadata: Map<String, String> = emptyMap()
) {
    init {
        require(id.isNotBlank()) { "MediaItem id must not be blank" }
        require(title.isNotBlank()) { "MediaItem title must not be blank" }
    }

    /**
     * Convenience property to access artist from metadata, if present.
     */
    val artist: String?
        get() = metadata[METADATA_ARTIST]

    /**
     * Convenience property to access album from metadata, if present.
     */
    val album: String?
        get() = metadata[METADATA_ALBUM]

    /**
     * Convenience property to access artwork / thumbnail URI, if present.
     */
    val artworkUri: String?
        get() = metadata[METADATA_ARTWORK_URI]

    companion object {
        const val METADATA_ARTIST = "artist"
        const val METADATA_ALBUM = "album"
        const val METADATA_ARTWORK_URI = "artwork_uri"
    }
}
