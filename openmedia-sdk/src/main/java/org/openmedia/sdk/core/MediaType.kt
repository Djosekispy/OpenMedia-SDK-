package org.openmedia.sdk.core

/**
 * Defines the type of media content supported by the OpenMedia SDK.
 *
 * This enumeration can be extended in future versions (e.g. LIVE_STREAM, PODCAST)
 * without breaking backwards compatibility.
 */
enum class MediaType {
    /** Audio track, song, voice clip, or podcast episode. */
    AUDIO,

    /** Video stream, clip, or movie. */
    VIDEO
}
