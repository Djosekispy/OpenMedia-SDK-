package org.openmedia.sdk.core

/**
 * Standardized error categories recognized by the OpenMedia SDK.
 */
enum class MediaErrorCode {
    /** The requested media content identifier or resource could not be found. */
    MEDIA_NOT_FOUND,

    /** The underlying source stream or file is unreachable, forbidden, or offline. */
    SOURCE_UNAVAILABLE,

    /** The codec, container format, or media protocol is not supported by the platform. */
    UNSUPPORTED_FORMAT,

    /** Generic playback failure occurring during decoding or rendering. */
    PLAYBACK_ERROR,

    /** Network connectivity issue, timeout, or DNS resolution failure. */
    NETWORK_ERROR
}

/**
 * Normalized exception representing errors encountered in playback or media resolution.
 *
 * @property code Standardized error classification [MediaErrorCode].
 * @property message Detailed, human-readable description of the problem.
 * @property cause Underlying root cause exception (if any), preserving debugging context.
 */
class MediaError(
    val code: MediaErrorCode,
    override val message: String? = null,
    override val cause: Throwable? = null
) : Exception(message ?: "Media error occurred: $code", cause) {

    override fun toString(): String {
        return "MediaError(code=$code, message=$message, cause=${cause?.message})"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MediaError) return false
        return code == other.code && message == other.message
    }

    override fun hashCode(): Int {
        var result = code.hashCode()
        result = 31 * result + (message?.hashCode() ?: 0)
        return result
    }
}
