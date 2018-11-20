package ch.hsr.dsl.dwrtc.signaling

import mu.KLogging
import net.tomp2p.dht.PeerDHT

/** Represents the own user */
interface IInternalClient {
    /**
     * Send a message to an external user.
     *
     * @param messageBody the message body
     * @param recipient the external user
     *
     * @return see [Future]
     */
    fun sendMessage(type: String, messageBody: String, recipient: IExternalClient): Future

    /**
     * Register a listener that handles messages for this user
     *
     * @param emitter a callable that receives the sender and the actual message
     */
    fun onReceiveMessage(emitter: (IExternalClient, ClientMessage) -> Unit)

    /** the user's session ID */
    val sessionId: String
}

/**
 * Represents the own user.
 *
 * @property peer the TomP2P peer object
 * @property clientService the ClientService
 * @property sessionId the user's session ID
 */
class InternalClient(
        private val peer: PeerDHT,
        private val clientService: ClientService,
        override val sessionId: String
) :
        IInternalClient {
    /** Logging companion */
    companion object : KLogging()

    override fun sendMessage(type: String, messageBody: String, recipient: IExternalClient): Future {
        logger.info { "send message $messageBody from $this to $recipient" }

        val result = recipient.sendMessage(type, messageBody, this.sessionId)
        logger.info { "sent message $messageBody from $this to $recipient" }
        result.onFailure { logger.info { "send message failed: $it" } }
        result.onSuccess { logger.info { "message sent successfully" } }
        return result
    }

    override fun onReceiveMessage(emitter: (IExternalClient, ClientMessage) -> Unit) {
        logger.info { "register emitter for message receiving (own peer address ${peer.peerAddress()})" }

        clientService.addDirectMessageListener(sessionId, emitter)
    }

    /** equals */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is InternalClient) return false

        if (sessionId != other.sessionId) return false
        if (peer.peerID() != other.peer.peerID()) return false

        return true
    }

    /** hashcode */
    override fun hashCode(): Int {
        var result = peer.peerID().hashCode()
        result = 31 * result + sessionId.hashCode()
        return result
    }

    override fun toString(): String {
        return "InternalClient Client (ID: $sessionId; Peer Address: ${peer.peerAddress()})"
    }
}
 
