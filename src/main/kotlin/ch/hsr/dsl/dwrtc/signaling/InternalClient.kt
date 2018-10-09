package ch.hsr.dsl.dwrtc.signaling

import mu.KLogging
import net.tomp2p.dht.PeerDHT

class InternalClient(private val peer: PeerDHT, private val clientService: ClientService, val sessionId: String) {
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

        clientService.addListener(sessionId, emitter)
    }
}
 
