package test

import ch.hsr.dsl.dwrtc.signaling.ClientMessage
import ch.hsr.dsl.dwrtc.signaling.ClientService
import ch.hsr.dsl.dwrtc.util.findFreePort
import ch.hsr.dsl.dwrtc.util.jsonTo
import ch.hsr.dsl.dwrtc.util.toJson
import ch.hsr.dsl.dwrtc.websocket.WEBSOCKET_PATH
import ch.hsr.dsl.dwrtc.websocket.WebSocketErrorMessage
import ch.hsr.dsl.dwrtc.websocket.WebSocketHandler
import ch.hsr.dsl.dwrtc.websocket.WebSocketIdMessage
import io.javalin.Javalin
import io.kotlintest.TestCaseOrder
import io.kotlintest.eventually
import io.kotlintest.extensions.TestListener
import io.kotlintest.seconds
import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec
import mu.KLogging
import org.http4k.client.WebsocketClient
import org.http4k.core.Uri
import org.http4k.websocket.WsMessage

class WebSocketBehaviorTest : WordSpec(), TestListener {
    companion object : KLogging()

    override fun testCaseOrder() = TestCaseOrder.Random // make sure tests are not dependent on each other

    private val port = findFreePort()
    private val app = Javalin.create().start(port)!!
    private val service = ClientService()
    private val wsUri = Uri.of("ws://localhost:$port$WEBSOCKET_PATH")

    init {
        "two WebSocket clients" should {
            eventually(5.seconds) {
                WebSocketHandler(app, service)
                val clientOne = WebsocketClient.blocking(wsUri)
                val clientTwo = WebsocketClient.blocking(wsUri)
                val clientOneIdMessage = clientOne.received().take(1).toList().first().bodyString()
                val clientOneId = jsonTo<WebSocketIdMessage>(clientOneIdMessage).id
                val clientTwoIdMessage = clientTwo.received().take(1).toList().first().bodyString()
                val clientTwoId = jsonTo<WebSocketIdMessage>(clientTwoIdMessage).id

                val message = ClientMessage("SignalingMessage", null, clientOneId, "Hello World")
                clientTwo.send(WsMessage(toJson(message)))
                val receivedMessageString = clientOne.received().take(1).toList().first().bodyString()
                val receivedMessage = jsonTo<ClientMessage>(receivedMessageString)

                val invalidIdMessage = ClientMessage("SignalingMessage", null, "NOTFOUND", "Hello World")
                clientTwo.send(WsMessage(toJson(invalidIdMessage)))
                val invalidIdReply =
                    jsonTo<WebSocketErrorMessage>(clientTwo.received().take(1).toList().first().bodyString())

                val nullIdMessage = ClientMessage("SignalingMessage", null, null, "Hello World")
                clientTwo.send(WsMessage(toJson(nullIdMessage)))
                val nullIdReply =
                    jsonTo<WebSocketErrorMessage>(clientTwo.received().take(1).toList().first().bodyString())

                "be able to send messages" {
                    receivedMessage.messageBody.shouldBe("Hello World")
                }
                "receive correct IDs" {
                    receivedMessage.senderSessionId.shouldBe(clientTwoId)
                }
                "get an error message when sending to an unknown client" {
                    invalidIdReply.error.shouldBe("not found")
                }
                "get an error message when not setting the recipient ID client" {
                    nullIdReply.error.shouldBe("Recipient ID not set, aborting")
                }
            }
        }
    }
}
