package ch.hsr.dsl.dwrtc.signaling

import mu.KLogging
import net.tomp2p.dht.PeerDHT

class InternalClient(private val peer: PeerDHT, val sessionId: String) {
    companion object : KLogging()

    fun sendMessage(messageBody: String, recipient: ExternalClient) {
        logger.info { "send message $messageBody from ${peer.peerAddress()} to $recipient" }

        peer.peer()
                .sendDirect(recipient.peerAddress)
                .`object`(MessageDto(sessionId, recipient.sessionId, messageBody))
                .start()
                .awaitListeners()
        logger.info { "sent" }
    }

    fun onReceiveMessage(emitter: (ExternalClient, MessageDto) -> Unit) {
        logger.info { "register emitter for message receiving (own peer address ${peer.peerAddress()})" }

        peer.peer().objectDataReply { senderPeerAddress, messageDto ->
            logger.info { "got message " }
            if (messageDto is MessageDto && messageDto.senderSessionId == sessionId) {
                logger.info { "message accepted" }
                emitter(
                        ExternalClient(
                                messageDto.senderSessionId,
                                senderPeerAddress
                        ), messageDto
                )
            }
            logger.info { "message discarded" }
        }
    }
}
 
