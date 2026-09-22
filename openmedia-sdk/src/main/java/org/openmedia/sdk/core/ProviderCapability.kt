package org.openmedia.sdk.core

/**
 * Functional capabilities that a [MediaProvider] can declare.
 *
 * Allows client applications and the SDK to query features supported by a specific provider.
 */
enum class ProviderCapability {
    /** The provider supports querying and searching media catalog. */
    SEARCH,

    /** The provider can resolve playable media streams. */
    STREAM,

    /** The provider supports offline media caching/downloads (reserved for future versions). */
    DOWNLOAD
}
