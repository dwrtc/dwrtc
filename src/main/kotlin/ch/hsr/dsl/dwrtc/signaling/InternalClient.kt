package ch.hsr.dsl.dwrtc.signaling

import mu.KLogging
import net.tomp2p.dht.PeerDHT

class InternalClient(private val peer: PeerDHT, val sessionId: String) {
    companion object : KLogging()

    fun sendMessage(messageBody: String, recipient: ExternalClient) {
        logger.info { "send message $messageBody from ${peer.peerAddress()} to $recipient" }

        val result = peer.peer()
                .sendDirect(recipient.peerAddress)
                .`object`(MessageDto(sessionId, recipient.sessionId, messageBody))
                .start().await()
        logger.info { "sent message $messageBody from ${peer.peerAddress()} to $recipient" }
        if (result.isFailed) throw Exception(result.failedReason())
    }

    fun onReceiveMessage(emitter: (ExternalClient, MessageDto) -> Unit) {
        logger.info { "register emitter for message receiving (own peer address ${peer.peerAddress()})" }

        peer.peer().objectDataReply { senderPeerAddress, messageDto ->
            logger.info { "got message $messageDto" }
            if (messageDto is MessageDto) {
                if (messageDto.recipientSessionId == sessionId) {
                    logger.info { "message accepted" }
                    emitter(
                            ExternalClient(
                                    messageDto.senderSessionId,
                                    senderPeerAddress
                            ), messageDto
                    )
                } else {
                    logger.info { "message discarded (expecting $sessionId)" }
                }
            } else {
                logger.info { "message discarded (not a message dto)" }
            }
            messageDto
        }
    }
}
 
