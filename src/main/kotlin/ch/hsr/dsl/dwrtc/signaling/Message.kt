package ch.hsr.dsl.dwrtc.signaling

import java.io.Serializable

/**
 * Generic Message class
 *
 * @property type the type of the message. Enables casting to a specific subclass.
 */
open class Message(val type: String) : Serializable

/**
 * A [Message] type that's sent and received over the P2P network.
 *
 *  [senderSessionId] and [recipientSessionId] are optional. Lower layers may reset these properties before they're sent onto the P2P layer.
 *
 *  @property senderSessionId the sender's session ID.
 *  @property recipientSessionId the recipient's session ID
 *  @property messageBody the message's (free-form) body
 */
data class SignalingMessage(var senderSessionId: String?, var recipientSessionId: String?, val messageBody: String) :
        Message("SignalingMessage") {
    init {
        this.senderSessionId = senderSessionId?.trim()
        this.recipientSessionId = recipientSessionId?.trim()
    }
}

