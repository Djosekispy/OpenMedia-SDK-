package org.openmedia.sdk.player

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.annotation.OptIn
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player as ExoPlayerContract
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.openmedia.sdk.core.MediaError
import org.openmedia.sdk.core.MediaErrorCode
import org.openmedia.sdk.core.MediaItem
import org.openmedia.sdk.core.PlaybackEvent
import org.openmedia.sdk.core.PlaybackState

/**
 * Concrete [Player] implementation powered by AndroidX Media3 / ExoPlayer.
 *
 * Normalizes ExoPlayer states and exceptions into OpenMedia SDK contracts,
 * broadcasting reactive Flow streams for UI and background service observers.
 *
 * @param context Android Application or Activity context used to initialize Media3.
 * @param coroutineScope Optional CoroutineScope for polling timers and async event dispatch.
 * @param exoPlayer Optional pre-configured ExoPlayer instance (primarily for dependency injection or testing).
 */
class Media3Player(
    context: Context,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
    exoPlayer: ExoPlayer? = null
) : Player {

    private val playerInstance: ExoPlayer = exoPlayer ?: ExoPlayer.Builder(context.applicationContext).build()
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _state = MutableStateFlow(PlaybackState.IDLE)
    override val state: StateFlow<PlaybackState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<PlaybackEvent>(extraBufferCapacity = 64)
    override val events: Flow<PlaybackEvent> = _events.asSharedFlow()

    private val _currentMedia = MutableStateFlow<MediaItem?>(null)
    override val currentMedia: StateFlow<MediaItem?> = _currentMedia.asStateFlow()

    private var positionTickerJob: Job? = null

    private val listener = object : ExoPlayerContract.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            updateNormalizedState()
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            updateNormalizedState()
            val media = _currentMedia.value
            if (isPlaying) {
                emitEvent(PlaybackEvent.OnPlay(media))
                startPositionTicker()
            } else {
                if (_state.value != PlaybackState.COMPLETED && _state.value != PlaybackState.ERROR) {
                    emitEvent(PlaybackEvent.OnPause(media))
                }
                stopPositionTicker()
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            val normalizedError = mapExoError(error)
            _state.value = PlaybackState.ERROR
            emitEvent(PlaybackEvent.OnStateChanged(PlaybackState.ERROR))
            emitEvent(PlaybackEvent.OnError(normalizedError))
            stopPositionTicker()
        }
    }

    init {
        runOnMainThread {
            playerInstance.addListener(listener)
        }
    }

    override fun setMedia(media: MediaItem) {
        _currentMedia.value = media
        _state.value = PlaybackState.LOADING
        emitEvent(PlaybackEvent.OnMediaChanged(media))
        emitEvent(PlaybackEvent.OnStateChanged(PlaybackState.LOADING))

        runOnMainThread {
            val exoItem = buildExoMediaItem(media)
            playerInstance.setMediaItem(exoItem)
            playerInstance.prepare()
        }
    }

    override fun play() {
        runOnMainThread {
            if (playerInstance.playbackState == ExoPlayerContract.STATE_ENDED) {
                playerInstance.seekTo(0L)
            }
            playerInstance.playWhenReady = true
        }
    }

    override fun pause() {
        runOnMainThread {
            playerInstance.playWhenReady = false
        }
    }

    override fun resume() {
        play()
    }

    override fun stop() {
        runOnMainThread {
            playerInstance.stop()
            playerInstance.seekTo(0L)
            _state.value = PlaybackState.IDLE
            emitEvent(PlaybackEvent.OnStateChanged(PlaybackState.IDLE))
            stopPositionTicker()
        }
    }

    override fun seekTo(positionMs: Long) {
        runOnMainThread {
            val target = positionMs.coerceAtLeast(0L)
            playerInstance.seekTo(target)
            emitEvent(PlaybackEvent.OnPositionChanged(target, getDuration()))
        }
    }

    override fun getCurrentPosition(): Long {
        return playerInstance.currentPosition.coerceAtLeast(0L)
    }

    override fun getDuration(): Long {
        val dur = playerInstance.duration
        return if (dur > 0) dur else (_currentMedia.value?.duration ?: 0L)
    }

    override fun getState(): PlaybackState {
        return _state.value
    }

    override fun release() {
        stopPositionTicker()
        coroutineScope.cancel()
        runOnMainThread {
            playerInstance.removeListener(listener)
            playerInstance.release()
            _state.value = PlaybackState.IDLE
            _currentMedia.value = null
        }
    }

    private fun updateNormalizedState() {
        val newState = when (playerInstance.playbackState) {
            ExoPlayerContract.STATE_IDLE -> {
                if (playerInstance.playerError != null) PlaybackState.ERROR else PlaybackState.IDLE
            }
            ExoPlayerContract.STATE_BUFFERING -> PlaybackState.BUFFERING
            ExoPlayerContract.STATE_READY -> {
                if (playerInstance.isPlaying) PlaybackState.PLAYING else PlaybackState.PAUSED
            }
            ExoPlayerContract.STATE_ENDED -> PlaybackState.COMPLETED
            else -> PlaybackState.IDLE
        }

        if (_state.value != newState) {
            _state.value = newState
            emitEvent(PlaybackEvent.OnStateChanged(newState))

            when (newState) {
                PlaybackState.BUFFERING -> emitEvent(PlaybackEvent.OnBuffering(_currentMedia.value))
                PlaybackState.COMPLETED -> {
                    emitEvent(PlaybackEvent.OnCompleted(_currentMedia.value))
                    stopPositionTicker()
                }
                else -> { /* handled by other hooks */ }
            }
        }
    }

    private fun startPositionTicker() {
        positionTickerJob?.cancel()
        positionTickerJob = coroutineScope.launch {
            while (isActive) {
                val pos = getCurrentPosition()
                val dur = getDuration()
                emitEvent(PlaybackEvent.OnPositionChanged(pos, dur))
                delay(POSITION_UPDATE_INTERVAL_MS)
            }
        }
    }

    private fun stopPositionTicker() {
        positionTickerJob?.cancel()
        positionTickerJob = null
    }

    private fun emitEvent(event: PlaybackEvent) {
        coroutineScope.launch {
            _events.emit(event)
        }
    }

    private fun buildExoMediaItem(media: MediaItem): androidx.media3.common.MediaItem {
        val builder = androidx.media3.common.MediaItem.Builder()
            .setMediaId(media.id)
            .setUri(media.source.uri)

        media.source.mimeType?.let { builder.setMimeType(it) }

        val metadataBuilder = androidx.media3.common.MediaMetadata.Builder()
            .setTitle(media.title)

        media.artist?.let { metadataBuilder.setArtist(it) }
        media.album?.let { metadataBuilder.setAlbumTitle(it) }

        builder.setMediaMetadata(metadataBuilder.build())
        return builder.build()
    }

    private fun mapExoError(error: PlaybackException): MediaError {
        val code = when (error.errorCode) {
            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT,
            PlaybackException.ERROR_CODE_IO_CLEARTEXT_NOT_PERMITTED -> {
                MediaErrorCode.NETWORK_ERROR
            }
            PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS,
            PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND -> {
                MediaErrorCode.SOURCE_UNAVAILABLE
            }
            PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED,
            PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED,
            PlaybackException.ERROR_CODE_DECODER_INIT_FAILED -> {
                MediaErrorCode.UNSUPPORTED_FORMAT
            }
            else -> MediaErrorCode.PLAYBACK_ERROR
        }

        return MediaError(
            code = code,
            message = error.message ?: "Media3 error code ${error.errorCodeName}",
            cause = error
        )
    }

    private fun runOnMainThread(action: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            action()
        } else {
            mainHandler.post(action)
        }
    }

    companion object {
        private const val POSITION_UPDATE_INTERVAL_MS = 500L
    }
}
