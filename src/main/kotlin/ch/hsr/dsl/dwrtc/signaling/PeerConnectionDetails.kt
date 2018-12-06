package ch.hsr.dsl.dwrtc.signaling

import java.net.InetAddress

/**
 * Peer connection details.
 *
 * Similar to TomP2P's `PeerAddress`, but can be entered by a user.
 *
 * @property ipAddress IP address
 * @property port the port
 */
data class PeerConnectionDetails(val ipAddress: InetAddress, val port: Int) {
    /**
     * Second constructor, taking a IP address string
     *
     * @property ipAddressString IP address as a string
     */
    constructor(ipAddressString: String, port: Int) : this(InetAddress.getByName(ipAddressString), port)
}

/**
 * Extract a list of [PeerConnectionDetails] of a configuration string.
 *
 * @param peers List of Strings. Strings formatted as "HOST_ADDRESS:PORT"
 */
fun extractPeerDetails(peers: List<String>?): List<PeerConnectionDetails> {
    if (peers == null) return emptyList()
    return peers.map { it ->
        val split = it.split(":", limit = 2)
        PeerConnectionDetails(split.first(), split.last().toInt())
    }
}
