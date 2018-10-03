package ch.hsr.dsl.dwrtc.signaling

import kotlin.properties.ObservableProperty

abstract class Client {
    internal abstract fun sendMessage(message: Message)

    abstract val receiveMessage: ObservableProperty<Message>
    abstract fun createMessage(messageBody: String): Message
}
