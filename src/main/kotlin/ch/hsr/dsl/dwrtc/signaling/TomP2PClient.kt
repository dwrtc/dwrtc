package ch.hsr.dsl.dwrtc.signaling

import net.tomp2p.p2p.Peer
import kotlin.properties.ObservableProperty

class TomP2PClient constructor(private val peer: Peer): Client() {

    override fun sendMessage(message: Message) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

        peer.sendDirect(message.recipient).`object`(message).start()
    }

    override val receiveMessage: ObservableProperty<Message>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun createMessage(messageBody: String): Message = TomP2PMessage(this, messageBody)
}
