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

fun extractPeerDetails(peers: List<String>?): List<PeerConnectionDetails> {
    if (peers == null) return emptyList()
    return peers.map { it ->
        val split = it.split(":")
        PeerConnectionDetails(split.first(), split.last().toInt())
    }
}
