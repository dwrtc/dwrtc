package ch.hsr.dsl.dwrtc.signaling

import net.tomp2p.dht.PeerDHT

class InternalClient(private val peer: PeerDHT, val sessionId: String) {
    fun sendMessage(messageBody: String, recipient: ExternalClient) {
        peer.peer()
            .sendDirect(recipient.peerAddress)
            .`object`(MessageDto(sessionId, recipient.sessionId, messageBody))
            .start()
    }

    fun onReceiveMessage(emitter: (ExternalClient, MessageDto) -> Any) {
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
 
