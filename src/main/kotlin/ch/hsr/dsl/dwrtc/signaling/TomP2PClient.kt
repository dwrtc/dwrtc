package ch.hsr.dsl.dwrtc.signaling

import net.tomp2p.dht.PeerDHT
import kotlin.properties.ObservableProperty

class TomP2PClient(private val peer: PeerDHT, sessionId: String, peerId: String) : Client() {
    override fun sendMessage(message: Message) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override val receiveMessage: ObservableProperty<Message>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun createMessage(messageBody: String): Message {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
