package ch.hsr.dsl.dwrtc.util.config

import ch.hsr.dsl.dwrtc.signaling.PeerConnectionDetails

fun extractPeerDetails(peers: List<String>): List<PeerConnectionDetails> {
    return peers.map { it ->
        val split = it.split(":")
        PeerConnectionDetails(split.first(), split.last().toInt())
    }
}
