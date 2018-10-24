package ch.hsr.dsl.dwrtc.signaling

import net.tomp2p.peers.PeerAddress

/** Represents another user. */
data class ExternalClient(val sessionId: String, val peerAddress: PeerAddress)
