package ch.hsr.dsl.dwrtc.signaling

class TomP2PMessage constructor(sender: Client, messageBody: String): Message(sender, messageBody) {
    override lateinit var recipient: Client
}
