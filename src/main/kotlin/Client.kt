package clientdiscovery

import kotlin.properties.ObservableProperty

abstract class Client {
    internal abstract fun sendMessage(message: Message)

    abstract val recieveMessage: ObservableProperty<Message>
    abstract fun createMessage(messageBody: String): Message
}
