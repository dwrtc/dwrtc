package ch.hsr.dsl.dwrtc.websocket

import ch.hsr.dsl.dwrtc.signaling.*
import ch.hsr.dsl.dwrtc.signaling.exceptions.SignalingException
import ch.hsr.dsl.dwrtc.util.jsonTo
import ch.hsr.dsl.dwrtc.util.toJson
import io.javalin.Javalin
import io.javalin.websocket.WsSession
import mu.KLogging
import java.util.concurrent.ConcurrentHashMap

const val IDLE_TIMEOUT_MS: Long = 15 * 60 * 1000
const val WEBSOCKET_PATH = "/ws"

class WebsocketHandler(app: Javalin, private val signallingService: ClientService) {
    companion object : KLogging()

    private val clients = ConcurrentHashMap<String, InternalClient>()
    private val sessions = ConcurrentHashMap<String, WsSession>()

    init {
        app.ws(WEBSOCKET_PATH) { ws ->
            ws.onConnect { session -> connect(session) }
            ws.onMessage { session, message -> onReceiveMessageFromWebsocket(session, message) }
            ws.onClose { session, _, reason -> close(session, reason) }
            ws.onError { _, _ -> logger.info { "Errored" } }
        }
    }

    private fun connect(session: WsSession) {
        logger.info { "create client for session ${session.id}" }

        session.idleTimeout = IDLE_TIMEOUT_MS

        sessions[session.id] = session

        val client = signallingService.addClient(session.id)
        client.onReceiveMessage { sender, messageDto -> onReceiveMessageFromSignaling(sender, messageDto) }
        clients[session.id] = client

        val message = WebsocketIdMessage(session.id)
        session.send(toJson(message))
    }

    private fun onReceiveMessageFromWebsocket(session: WsSession, message: String) {
        val messageDto = jsonTo<SignalingMessage>(message)
        messageDto.senderSessionId = session.id

        try {
            val recipient = signallingService.findClient(messageDto.recipientSessionId!!)
            clients[session.id]?.let { it.sendMessage(messageDto.messageBody, recipient) }
        } catch (e: SignalingException) {
            session.send(toJson(WebsocketErrorMessage(e.message!!)))
        }
    }

    private fun close(session: WsSession, reason: String) {
        logger.info { "close session ${session.id} because of $reason" }

        clients[session.id]?.let {
            logger.info { "remove client $it" }
            signallingService.removeClient(it)
            clients.remove(session.id)
        } ?: run {
            logger.error { "client ${session.id} not found" }
            throw Exception("Client Not Found")
        }
    }

    private fun onReceiveMessageFromSignaling(sender: ExternalClient, message: SignalingMessage) {
        logger.info { "sending message ${message}" }
        sessions[message.recipientSessionId]?.let { it.send(toJson(message)) }
    }
}
