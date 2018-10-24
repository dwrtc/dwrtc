package ch.hsr.dsl.dwrtc.websocket

import ch.hsr.dsl.dwrtc.signaling.Message

data class WebSocketErrorMessage(val error: String) : Message("WebSocketErrorMessage")
data class WebSocketIdMessage(val id: String) : Message("WebSocketIdMessage")
