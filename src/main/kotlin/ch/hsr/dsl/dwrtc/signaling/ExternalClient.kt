package ch.hsr.dsl.dwrtc.signaling

import net.tomp2p.dht.PeerDHT
import net.tomp2p.peers.PeerAddress


interface IExternalClient {
    fun sendMessage(messageBody: String): Future
    val sessionId: String
}

/** Represents another user. */
class ExternalClient(override val sessionId: String, val peerAddress: PeerAddress, val peer: PeerDHT) :
    IExternalClient {
    override fun sendMessage(messageBody: String) = Future(
        peer.peer()
            .sendDirect(peerAddress)
            .`object`(SignalingMessage(sessionId, sessionId, messageBody))
            .start()
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ExternalClient) return false

        if (sessionId != other.sessionId) return false
        if (peerAddress.peerId() != other.peerAddress.peerId()) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sessionId.hashCode()
        result = 31 * result + peerAddress.peerId().hashCode()
        return result
    }
}
