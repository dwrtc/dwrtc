package test

import ch.hsr.dsl.dwrtc.signaling.PeerConnectionDetails
import ch.hsr.dsl.dwrtc.signaling.extractPeerDetails
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldThrow
import io.kotlintest.specs.WordSpec

class ExtractionTest : WordSpec() {

    init {
        "an empty string list" should {
            val extractPeerDetails = extractPeerDetails(emptyList())
            "yield an empty peer list" {
                extractPeerDetails.shouldBeEmpty()
            }
        }
        "a malformed string" should {
            "throw an exception" {
                shouldThrow<NumberFormatException> { extractPeerDetails(listOf("noColonsHere")) }
            }
        }
        "a null list" should {
            val extractPeerDetails = extractPeerDetails(null)
            "yield an empty peer list" {
                extractPeerDetails.shouldBeEmpty()
            }
        }
        "a string list with one elements" should {
            val extractPeerDetails = extractPeerDetails(listOf("127.0.0.1:5000"))
            "yield exactly one result" {
                val expected = listOf(PeerConnectionDetails("127.0.0.1", 5000))
                extractPeerDetails.shouldContainExactly(expected)
            }
        }
        "a string list with multiple elements" should {
            val extractPeerDetails = extractPeerDetails(listOf("127.0.0.1:5000", "127.0.0.1:4000"))
            "yield all results result" {
                val expected = listOf(
                        PeerConnectionDetails("127.0.0.1", 5000),
                        PeerConnectionDetails("127.0.0.1", 4000))
                extractPeerDetails.shouldContainExactly(expected)
            }
        }
    }
}
