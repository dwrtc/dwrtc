package ch.hsr.dsl.dwrtc.signaling

data class MessageDto(val senderSessionId: String, val recipientSessionId: String, val messageBody: String)
