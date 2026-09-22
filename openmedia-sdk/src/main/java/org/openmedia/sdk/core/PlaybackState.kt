package org.openmedia.sdk.core

/**
 * Normalized playback states exposed by the OpenMedia SDK.
 *
 * Client applications interact exclusively with these states, shielding them from
 * underlying engine details (such as ExoPlayer/Media3 internal integer states).
 */
enum class PlaybackState {
    /** The player is idle; no media is loaded or playback has been stopped/reset. */
    IDLE,

    /** The player is preparing media or resolving sources. */
    LOADING,

    /** Media is actively playing. */
    PLAYING,

    /** Media playback is paused and can be resumed. */
    PAUSED,

    /** Playback is stalled while awaiting additional buffer data. */
    BUFFERING,

    /** Playback has reached the end of the current media stream. */
    COMPLETED,

    /** Playback encountered a terminal error. Detailed diagnostics are in [MediaError]. */
    ERROR
}
