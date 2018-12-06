package ch.hsr.dsl.dwrtc.websocket

import ch.hsr.dsl.dwrtc.signaling.ClientMessage
import ch.hsr.dsl.dwrtc.signaling.ClientService
import ch.hsr.dsl.dwrtc.signaling.IInternalClient
import ch.hsr.dsl.dwrtc.util.jsonTo
import ch.hsr.dsl.dwrtc.util.toJson
import io.javalin.Javalin
import io.javalin.websocket.WsSession
import mu.KLogging
import java.util.concurrent.ConcurrentHashMap

/** WebSocket session idle timeout in milliseconds. */
const val IDLE_TIMEOUT_MS: Long = 15 * 60 * 1000
/** Path that the WebSocket handler is available on */
const val WEBSOCKET_PATH = "/ws"

/**
 * Main handler class
 *
 * @param app the Javalin app
 * @property signallingService the underlying ClientService
 * @constructor sets up the WebSocket path and various handlers
 */
class WebSocketHandler(app: Javalin, private val signallingService: ClientService) {
    /** Logging companion */
    companion object : KLogging()

    /** Map of session ID to InternalClient */
    private val clients = ConcurrentHashMap<String, IInternalClient>()
    /** Map of session ID to WebSocket session */
    private val sessions = ConcurrentHashMap<String, WsSession>()

    init {
        app.ws(WEBSOCKET_PATH) { ws ->
            ws.onConnect { session -> connect(session) }
            ws.onMessage { session, message -> onReceiveMessageFromWebSocket(session, message) }
            ws.onClose { session, _, reason -> close(session, reason) }
            ws.onError { _, _ -> logger.info { "Errored" } }
        }
    }

    /**
     * Handler for new WebSocket connections
     *
     * @param session the new WebSocket session
     */
    private fun connect(session: WsSession) {
        logger.info { "create client for session ${session.id}" }

        session.idleTimeout = IDLE_TIMEOUT_MS

        val (client, clientFuture) = signallingService.addClient(session.id)
        clientFuture.onSuccess {
            sessions[session.id] = session

            client.onReceiveMessage { _, messageDto -> onReceiveMessageFromSignaling(messageDto) }
            clients[session.id] = client

            val message = WebSocketIdMessage(session.id)
            session.send(toJson(message))
        }
    }

    /**
     * Handler for a new message from the WebSocket
     *
     * Sends the message to the recipient via P2P
     *
     * @param session the WebSocket session this message is from
     * @param message the message content
     */
    private fun onReceiveMessageFromWebSocket(session: WsSession, message: String) {
        val messageDto = jsonTo<ClientMessage>(message)
        messageDto.senderSessionId = session.id

        val future = signallingService.findClient(messageDto.recipientSessionId!!)
        future.onGet { recipient ->
            clients[session.id]?.let { it ->
                val sendFuture = it.sendMessage(
                        messageDto.type,
                        messageDto.messageBody,
                        recipient
                )
                sendFuture.onFailure {
                    logger.error { "message could not be sent to the P2P layer" }
                    session.send(toJson(WebSocketErrorMessage("message could not be sent to the P2P layer")))
                }
                sendFuture.onSuccess { logger.debug { "message could be sent to the P2P layer" } }
            }
        }
        future.onNotFound { session.send(toJson(WebSocketErrorMessage("not found"))) }
    }

    /**
     * Handler for when a WebSocket session closes
     *
     * @param session the closing WebSocket session
     * @param reason the reason why this session closed
     */
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

    /**
     * Handler for a new message from the P2P layer
     *
     * Sends the message to the recipient via WebSocket
     */
    private fun onReceiveMessageFromSignaling(message: ClientMessage) {
        logger.info { "sending message $message" }
        sessions[message.recipientSessionId]?.remote.sendStringByFuture(toJson(message))
    }
}
