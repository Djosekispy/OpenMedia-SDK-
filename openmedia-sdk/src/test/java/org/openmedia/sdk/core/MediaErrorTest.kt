package org.openmedia.sdk.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class MediaErrorTest {

    @Test
    fun testAllStandardErrorCodesExist() {
        val expectedCodes = listOf(
            MediaErrorCode.MEDIA_NOT_FOUND,
            MediaErrorCode.SOURCE_UNAVAILABLE,
            MediaErrorCode.UNSUPPORTED_FORMAT,
            MediaErrorCode.PLAYBACK_ERROR,
            MediaErrorCode.NETWORK_ERROR
        )

        for (code in expectedCodes) {
            val error = MediaError(code = code, message = "Test error for $code")
            assertEquals(code, error.code)
            assertEquals("Test error for $code", error.message)
        }
    }

    @Test
    fun testCausePreservation() {
        val rootCause = IOException("Connection timed out")
        val error = MediaError(
            code = MediaErrorCode.NETWORK_ERROR,
            message = "Failed to stream audio",
            cause = rootCause
        )

        assertEquals(MediaErrorCode.NETWORK_ERROR, error.code)
        assertEquals("Failed to stream audio", error.message)
        assertEquals(rootCause, error.cause)
        assertTrue(error.message!!.contains("Failed"))
    }

    @Test
    fun testEqualityAndToString() {
        val err1 = MediaError(MediaErrorCode.UNSUPPORTED_FORMAT, "Bad codec")
        val err2 = MediaError(MediaErrorCode.UNSUPPORTED_FORMAT, "Bad codec")
        assertEquals(err1, err2)
        assertTrue(err1.toString().contains("UNSUPPORTED_FORMAT"))
    }
}
