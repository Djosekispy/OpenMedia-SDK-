package org.openmedia.sdk.core

/**
 * Represents asynchronous playback lifecycle events emitted by a [org.openmedia.sdk.player.Player].
 */
sealed interface PlaybackEvent {
    /** Emitted when playback transitions to active playing state. */
    data class OnPlay(val item: MediaItem?) : PlaybackEvent

    /** Emitted when playback is paused. */
    data class OnPause(val item: MediaItem?) : PlaybackEvent

    /** Emitted when current media item finishes playing. */
    data class OnCompleted(val item: MediaItem?) : PlaybackEvent

    /** Emitted when player enters buffering state awaiting more stream data. */
    data class OnBuffering(val item: MediaItem?) : PlaybackEvent

    /** Emitted when player encounters a playback error. */
    data class OnError(val error: MediaError) : PlaybackEvent

    /** Emitted periodically to communicate current playback time and total track duration. */
    data class OnPositionChanged(val positionMs: Long, val durationMs: Long) : PlaybackEvent

    /** Emitted when the currently loaded active media item changes. */
    data class OnMediaChanged(val item: MediaItem?) : PlaybackEvent

    /** Emitted whenever the normalized [PlaybackState] transitions. */
    data class OnStateChanged(val state: PlaybackState) : PlaybackEvent
}
