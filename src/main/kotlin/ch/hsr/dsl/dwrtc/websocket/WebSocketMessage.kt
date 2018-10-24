package ch.hsr.dsl.dwrtc.websocket

import ch.hsr.dsl.dwrtc.signaling.Message

/**
 * Error messages meant for the WebSocket
 *
 * @property error the error message
 */
data class WebSocketErrorMessage(val error: String) : Message("WebSocketErrorMessage")

/**
 * New ID message
 *
 * @property id the ID
 */
data class WebSocketIdMessage(val id: String) : Message("WebSocketIdMessage")
