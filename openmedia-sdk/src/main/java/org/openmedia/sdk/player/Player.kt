package org.openmedia.sdk.player

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.openmedia.sdk.core.MediaItem
import org.openmedia.sdk.core.PlaybackEvent
import org.openmedia.sdk.core.PlaybackState

/**
 * Primary media player contract defined by the OpenMedia SDK.
 *
 * Provides a normalized control interface and reactive state/event streams
 * for media playback, independent of any specific playback engine implementation.
 */
interface Player {
    /**
     * Reactive observable state of the player.
     */
    val state: StateFlow<PlaybackState>

    /**
     * Flow of playback lifecycle events ([PlaybackEvent.OnPlay], [PlaybackEvent.OnPause], etc.).
     */
    val events: Flow<PlaybackEvent>

    /**
     * Current [MediaItem] loaded into the player, or null if none.
     */
    val currentMedia: StateFlow<MediaItem?>

    /**
     * Sets and prepares a [MediaItem] for playback.
     *
     * @param media The media item to load.
     */
    fun setMedia(media: MediaItem)

    /**
     * Begins or resumes media playback.
     */
    fun play()

    /**
     * Pauses active media playback.
     */
    fun pause()

    /**
     * Resumes playback if paused.
     */
    fun resume()

    /**
     * Stops media playback and resets playback position.
     */
    fun stop()

    /**
     * Seeks to a specific playback position in milliseconds.
     *
     * @param positionMs Target time offset in milliseconds.
     */
    fun seekTo(positionMs: Long)

    /**
     * Returns the current playback position in milliseconds.
     */
    fun getCurrentPosition(): Long

    /**
     * Returns the total duration of the current media in milliseconds,
     * or 0 if unknown or during a live stream.
     */
    fun getDuration(): Long

    /**
     * Returns the current normalized [PlaybackState].
     */
    fun getState(): PlaybackState

    /**
     * Releases underlying media engine resources and cleans up coroutine jobs.
     */
    fun release()
}
