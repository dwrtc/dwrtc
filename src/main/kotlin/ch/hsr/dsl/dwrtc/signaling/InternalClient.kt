package ch.hsr.dsl.dwrtc.signaling

import mu.KLogging
import net.tomp2p.dht.PeerDHT

interface IInternalClient {
    fun sendMessage(messageBody: String, recipient: IExternalClient)
    fun onReceiveMessage(emitter: (IExternalClient, SignalingMessage) -> Unit)
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
    companion object : KLogging()

    override fun sendMessage(messageBody: String, recipient: IExternalClient) {
        logger.info { "send message $messageBody from ${peer.peerAddress()} to $recipient" }

        val result = recipient.sendMessage(messageBody)
        logger.info { "sent message $messageBody from ${peer.peerAddress()} to $recipient" }
        result.onFailure { logger.info { "send message failed: $it" } }
        result.onSuccess { logger.info { "message sent successfully" } }
    }

    /**
     * Register a listener that handles messages for this user
     *
     * @property emitter a callable that receives the sender and the actual message
     */
    override fun onReceiveMessage(emitter: (IExternalClient, SignalingMessage) -> Unit) {
        logger.info { "register emitter for message receiving (own peer address ${peer.peerAddress()})" }

        clientService.addDirectMessageListener(sessionId, emitter)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is InternalClient) return false

        if (sessionId != other.sessionId) return false
        if (peer.peerID() != other.peer.peerID()) return false

        return true
    }

    override fun hashCode(): Int {
        var result = peer.peerID().hashCode()
        result = 31 * result + sessionId.hashCode()
        return result
    }
}
 
