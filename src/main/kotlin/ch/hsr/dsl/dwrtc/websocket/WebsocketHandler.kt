package ch.hsr.dsl.dwrtc.websocket

import ch.hsr.dsl.dwrtc.signaling.ClientService
import ch.hsr.dsl.dwrtc.signaling.ExternalClient
import ch.hsr.dsl.dwrtc.signaling.InternalClient
import ch.hsr.dsl.dwrtc.signaling.SignalingMessage
import io.javalin.Javalin
import io.javalin.json.JavalinJackson
import io.javalin.websocket.WsSession
import mu.KLogging
import java.util.concurrent.ConcurrentHashMap

class WebsocketHandler(app: Javalin, private val signallingService: ClientService) {
    companion object : KLogging()

    private val clients = ConcurrentHashMap<String, InternalClient>()
    private val sessions = ConcurrentHashMap<String, WsSession>()

    init {
        app.ws("/ws") { ws ->
            ws.onConnect { session -> connect(session) }
            ws.onMessage { session, message -> onReceiveMessageFromWebsocket(session, message) }
            ws.onClose { session, statusCode, reason -> close(session, reason) }
            ws.onError { session, throwable -> logger.info { "Errored" } }
        }
    }

    private fun connect(session: WsSession) {
        logger.info { "create client for session ${session.id}" }

        sessions[session.id] = session

        val client = signallingService.addClient(session.id)
        client.onReceiveMessage { sender, messageDto -> onReceiveMessageFromSignaling(sender, messageDto) }
        clients[session.id] = client
        session.send(session.id)
    }

    private fun onReceiveMessageFromWebsocket(session: WsSession, message: String) {
        val messageDto = jsonTo<SignalingMessage>(message)
        messageDto.senderSessionId = session.id
        val recipient = signallingService.findClient(messageDto.recipientSessionId!!)

        clients[session.id]?.let { it.sendMessage(messageDto.messageBody, recipient) }
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
        sessions[message.recipientSessionId]?.let { it.send(xToJson(message)) }
    }

    private inline fun <reified OutputType> jsonTo(jsonString: String) =
            JavalinJackson.fromJson(jsonString, OutputType::class.java)

    private fun xToJson(message: Any) = JavalinJackson.toJson(message)
}
