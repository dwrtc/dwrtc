package ch.hsr.dsl.dwrtc.signaling

import net.tomp2p.dht.PeerDHT
import net.tomp2p.peers.PeerAddress
import kotlin.properties.ObservableProperty

class TomP2PClient(private val peer: PeerDHT, val sessionId: String, val peerAddress: PeerAddress) : Client() {
    override fun sendMessage(message: Message) {
        peer.peer().sendDirect((message.recipient as TomP2PClient).peerAddress).`object`(message).start()
    }

    override val receiveMessage: ObservableProperty<Message>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun createMessage(messageBody: String): Message = Message(this, messageBody)
}
