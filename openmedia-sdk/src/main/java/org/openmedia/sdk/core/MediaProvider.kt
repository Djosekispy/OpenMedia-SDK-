package org.openmedia.sdk.core

/**
 * Fundamental contract for external or local media sources providing discovery and search capabilities.
 *
 * Concrete providers (e.g. LocalMediaProvider, PodcastProvider, YouTubeProvider) implement
 * this interface to supply [MediaItem] catalog entries to the application without coupling
 * the application or player to the backend source.
 */
interface MediaProvider {
    /** Unique alphanumeric identifier for this provider. */
    val id: String

    /** Human-readable display name (e.g. "Local Storage", "Radio Directory"). */
    val name: String

    /** Capabilities supported by this provider instance. */
    val capabilities: Set<ProviderCapability>
        get() = setOf(ProviderCapability.SEARCH, ProviderCapability.STREAM)

    /**
     * Executes an asynchronous search query against this provider's catalog.
     *
     * @param query Search keywords or criteria.
     * @return List of matching [MediaItem] results.
     */
    suspend fun search(query: String): List<MediaItem>
}
