package ch.hsr.dsl.dwrtc.signaling

import net.tomp2p.dht.PeerDHT
import net.tomp2p.peers.PeerAddress

/** Represents another user */
interface IExternalClient {
    /**
     * Send a message to this user
     *
     * @param messageBody the message body
     * @return a future. See [Future]
     */
    fun sendMessage(messageBody: String): Future

    /** the user's session ID */
    val sessionId: String
}

/** Represents another user.
 *
 * @property peerAddress the user's peer address
 * @property peer the peer to use for all operations
 */
class ExternalClient(override val sessionId: String, val peerAddress: PeerAddress, val peer: PeerDHT) :
        IExternalClient {
    override fun sendMessage(messageBody: String) = Future(
            peer.peer()
                    .sendDirect(peerAddress)
                    .`object`(SignalingMessage(sessionId, sessionId, messageBody))
                    .start()
    )

    /** equals */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ExternalClient) return false

        if (sessionId != other.sessionId) return false
        if (peerAddress.peerId() != other.peerAddress.peerId()) return false

        return true
    }

    /** hashcode */
    override fun hashCode(): Int {
        var result = sessionId.hashCode()
        result = 31 * result + peerAddress.peerId().hashCode()
        return result
    }
}
