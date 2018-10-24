package ch.hsr.dsl.dwrtc.signaling

import java.io.Serializable

open class Message(val type: String) : Serializable

data class SignalingMessage(var senderSessionId: String?, var recipientSessionId: String?, val messageBody: String) :
        Message("SignalingMessage") {
        init {
                this.senderSessionId = senderSessionId?.trim()
                this.recipientSessionId = recipientSessionId?.trim()
        }
}

