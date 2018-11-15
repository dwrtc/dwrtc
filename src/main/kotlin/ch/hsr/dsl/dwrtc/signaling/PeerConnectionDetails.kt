package ch.hsr.dsl.dwrtc.signaling

import java.net.InetAddress

/**
 * Peer connection details.
 *
 * Similar to TomP2P's `PeerAddress`, but can be entered by a user.
 *
 * @property ipAddressString the IP address
 * @property port the port
 */
data class PeerConnectionDetails(val ipAddressString: String, val port: Int) {
    /** Technical IP address. Converted from [ipAddressString]. */
    val ipAddress: InetAddress = InetAddress.getByName(ipAddressString)
}

fun extractPeerDetails(peers: List<String>): List<PeerConnectionDetails> {
    return peers.map { it ->
        val split = it.split(":")
        PeerConnectionDetails(split.first(), split.last().toInt())
    }
}
