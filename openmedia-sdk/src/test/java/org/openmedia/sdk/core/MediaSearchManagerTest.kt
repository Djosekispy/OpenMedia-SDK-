package org.openmedia.sdk.core

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaSearchManagerTest {

    private class MockProvider(
        override val id: String,
        override val name: String,
        override val capabilities: Set<ProviderCapability> = setOf(ProviderCapability.SEARCH),
        private val results: List<MediaItem> = emptyList(),
        private val shouldFail: Boolean = false
    ) : MediaProvider {
        override suspend fun search(query: String): List<MediaItem> {
            if (shouldFail) throw RuntimeException("Network down")
            return results.filter { it.title.contains(query, ignoreCase = true) }
        }
    }

    @Test
    fun testRegisterAndUnregisterProviders() {
        val manager = MediaSearchManager()
        val provider1 = MockProvider("p1", "Provider One")
        val provider2 = MockProvider("p2", "Provider Two")

        manager.registerProvider(provider1)
        manager.registerProvider(provider2)
        assertEquals(2, manager.getRegisteredProviders().size)

        manager.unregisterProvider("p1")
        assertEquals(1, manager.getRegisteredProviders().size)
        assertEquals("p2", manager.getRegisteredProviders()[0].id)
    }

    @Test
    fun testSearchAggregatesResultsAcrossMultipleProviders() = runTest {
        val item1 = MediaItem("1", "Imagine Dragons - Radioactive", MediaType.AUDIO, MediaSource("https://example.com/1.mp3"))
        val item2 = MediaItem("2", "Imagine Dragons - Believer", MediaType.AUDIO, MediaSource("https://example.com/2.mp3"))
        val item3 = MediaItem("3", "Coldplay - Yellow", MediaType.AUDIO, MediaSource("https://example.com/3.mp3"))

        val providerA = MockProvider("prov_a", "Local Music", results = listOf(item1, item3))
        val providerB = MockProvider("prov_b", "Radio Directory", results = listOf(item2))

        val manager = MediaSearchManager(listOf(providerA, providerB))
        val searchResults = manager.search("Imagine Dragons")

        assertEquals(2, searchResults.size)
        assertTrue(searchResults.any { it.id == "1" })
        assertTrue(searchResults.any { it.id == "2" })
    }

    @Test
    fun testSearchIgnoresProvidersWithoutSearchCapability() = runTest {
        val item = MediaItem("1", "Stream Track", MediaType.AUDIO, MediaSource("https://example.com/1.mp3"))
        val streamOnlyProvider = MockProvider(
            id = "stream_only",
            name = "Stream Only",
            capabilities = setOf(ProviderCapability.STREAM),
            results = listOf(item)
        )

        val manager = MediaSearchManager(listOf(streamOnlyProvider))
        val results = manager.search("Stream")
        assertTrue(results.isEmpty())
    }

    @Test
    fun testFaultTolerantSearchWhenOneProviderFails() = runTest {
        val goodItem = MediaItem("1", "Healthy Track", MediaType.AUDIO, MediaSource("https://example.com/1.mp3"))
        val goodProvider = MockProvider("good", "Good Provider", results = listOf(goodItem))
        val faultyProvider = MockProvider("bad", "Failing Provider", shouldFail = true)

        val manager = MediaSearchManager(listOf(goodProvider, faultyProvider))
        val results = manager.search("Track")

        // Should return results from the healthy provider despite the other failing
        assertEquals(1, results.size)
        assertEquals("1", results[0].id)
    }
}
