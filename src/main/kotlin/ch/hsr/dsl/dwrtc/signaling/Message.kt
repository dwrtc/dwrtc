package ch.hsr.dsl.dwrtc.signaling

abstract class Message constructor(val sender: Client, val messageBody: String) {
    abstract var recipient: Client

    fun sendTo(recipient: Client) {
        this.recipient = recipient
        sender.sendMessage(this)
    }
}
