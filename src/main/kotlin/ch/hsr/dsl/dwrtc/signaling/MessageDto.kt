package ch.hsr.dsl.dwrtc.signaling

import java.io.Serializable

data class MessageDto(val senderSessionId: String, val recipientSessionId: String, val messageBody: String) :
    Serializable
