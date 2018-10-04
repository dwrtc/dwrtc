package ch.hsr.dsl.dwrtc.signaling

import mu.KLogging
import net.tomp2p.dht.PeerDHT

class InternalClient(private val peer: PeerDHT, val sessionId: String) {
    companion object : KLogging()

    fun sendMessage(messageBody: String, recipient: ExternalClient) {
        logger.info { "send message $messageBody to $recipient" }

        peer.peer()
            .sendDirect(recipient.peerAddress)
            .`object`(MessageDto(sessionId, recipient.sessionId, messageBody))
            .start()
    }

    fun onReceiveMessage(emitter: (ExternalClient, MessageDto) -> Any) {
        logger.info { "register emitter for message receiving" }

        peer.peer().objectDataReply { senderPeerAddress, messageDto ->
            if (messageDto is MessageDto && messageDto.senderSessionId == sessionId) emitter(
                ExternalClient(
                    messageDto.senderSessionId,
                    senderPeerAddress
                ), messageDto
            )
        }
    }
}
 
