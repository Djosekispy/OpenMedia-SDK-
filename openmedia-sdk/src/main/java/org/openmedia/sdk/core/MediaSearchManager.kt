package org.openmedia.sdk.core

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * High-level search coordinator that queries across one or multiple registered [MediaProvider]s.
 *
 * Allows client applications to execute unified multi-provider catalog searches
 * without managing concurrency or provider registries manually.
 */
class MediaSearchManager(
    initialProviders: List<MediaProvider> = emptyList()
) {
    private val lock = Any()
    private val providers = mutableListOf<MediaProvider>()

    init {
        providers.addAll(initialProviders)
    }

    /**
     * Registers a new [MediaProvider] to participate in future searches.
     */
    fun registerProvider(provider: MediaProvider) = synchronized(lock) {
        if (providers.none { it.id == provider.id }) {
            providers.add(provider)
        }
    }

    /**
     * Removes a provider by its unique [providerId].
     */
    fun unregisterProvider(providerId: String) = synchronized(lock) {
        providers.removeAll { it.id == providerId }
    }

    /**
     * Returns an immutable snapshot list of all currently registered providers.
     */
    fun getRegisteredProviders(): List<MediaProvider> = synchronized(lock) {
        ArrayList(providers)
    }

    /**
     * Executes concurrent searches across all registered providers that declare
     * the [ProviderCapability.SEARCH] capability, flattening and returning results.
     *
     * In case one provider throws an exception, other provider results are preserved.
     */
    suspend fun search(query: String): List<MediaItem> = coroutineScope {
        val activeProviders = synchronized(lock) {
            providers.filter { it.capabilities.contains(ProviderCapability.SEARCH) }
        }

        if (activeProviders.isEmpty()) return@coroutineScope emptyList()

        val deferredResults = activeProviders.map { provider ->
            async {
                try {
                    provider.search(query)
                } catch (e: Exception) {
                    emptyList()
                }
            }
        }

        deferredResults.awaitAll().flatten()
    }

    /**
     * Executes concurrent searches and groups the results by provider.
     */
    suspend fun searchGrouped(query: String): Map<MediaProvider, List<MediaItem>> = coroutineScope {
        val activeProviders = synchronized(lock) {
            providers.filter { it.capabilities.contains(ProviderCapability.SEARCH) }
        }

        if (activeProviders.isEmpty()) return@coroutineScope emptyMap()

        val results = activeProviders.map { provider ->
            async {
                val items = try {
                    provider.search(query)
                } catch (e: Exception) {
                    emptyList()
                }
                provider to items
            }
        }.awaitAll().toMap()

        results
    }
}
