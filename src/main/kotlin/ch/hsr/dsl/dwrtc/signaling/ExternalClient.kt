package ch.hsr.dsl.dwrtc.signaling

import net.tomp2p.peers.PeerAddress

data class ExternalClient(val sessionId: String, val peerAddress: PeerAddress)
