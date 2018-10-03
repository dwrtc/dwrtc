package ch.hsr.dsl.dwrtc.signaling

class Message constructor(val sender: Client, val messageBody: String) {
    lateinit var recipient: Client

    fun sendTo(recipient: Client) {
        this.recipient = recipient
        sender.sendMessage(this)
    }
}
