package org.openmedia.sdk.core

/**
 * Represents the origin source from which media content is fetched and played.
 *
 * @property uri Uniform Resource Identifier pointing to the media asset (e.g. https://, file://, content://).
 * @property mimeType Standard MIME type format (e.g. "audio/mpeg", "video/mp4"), if known.
 * @property quality Descriptive quality indicator (e.g. "1080p", "320kbps", "HD"), when available.
 * @property headers Optional HTTP headers required for streaming authentication or session management.
 */
data class MediaSource(
    val uri: String,
    val mimeType: String? = null,
    val quality: String? = null,
    val headers: Map<String, String> = emptyMap()
)
