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
import io.kotlintest.*
import io.kotlintest.extensions.TestListener
import io.kotlintest.specs.WordSpec
import mu.KLogging
import org.http4k.client.WebsocketClient
import org.http4k.core.Uri
import org.http4k.websocket.WsMessage

class WebSocketCloseBehaviorTest : WordSpec(), TestListener {
    companion object : KLogging()

    override fun testCaseOrder() = TestCaseOrder.Random // make sure tests are not dependent on each other

    private val port = findFreePort()
    private val app = Javalin.create().start(port)!!
    private val service = ClientService()
    private val wsUri = Uri.of("ws://localhost:$port$WEBSOCKET_PATH")

    init {
        "two WebSocket clients where some close" should {
            eventually(5.seconds) {
                WebSocketHandler(app, service)
                val clientOne = WebsocketClient.blocking(wsUri)
                val clientTwo = WebsocketClient.blocking(wsUri)
                val clientThree = WebsocketClient.blocking(wsUri)
                val clientOneIdMessage = clientOne.received().take(1).toList().first().bodyString()
                val clientOneId = jsonTo<WebSocketIdMessage>(clientOneIdMessage).id
                val clientTwoIdMessage = clientTwo.received().take(1).toList().first().bodyString()
                val clientTwoId = jsonTo<WebSocketIdMessage>(clientTwoIdMessage).id
                val clientThreeIdMessage = clientThree.received().take(1).toList().first().bodyString()
                val clientThreeId = jsonTo<WebSocketIdMessage>(clientThreeIdMessage).id

                val message = ClientMessage("SignalingMessage", null, clientOneId, "Hello World")

                clientOne.close()
                clientTwo.send(WsMessage(toJson(message)))
                val firstTry = jsonTo<WebSocketErrorMessage>(clientTwo.received().take(1).toList().first().bodyString())

                clientTwo.send(WsMessage(toJson(message)))  // re-send original message
                val secondTry =
                    jsonTo<WebSocketErrorMessage>(clientTwo.received().take(1).toList().first().bodyString())

                Thread.sleep(2_000)

                val stillWorkingMessage = ClientMessage("SignalingMessage", null, clientThreeId, "Hello World")
                clientTwo.send(WsMessage(toJson(stillWorkingMessage)))
                val receivedMessageString = clientThree.received().take(1).toList().first().bodyString()
                val receivedMessage = jsonTo<ClientMessage>(receivedMessageString)



                "get an error message when sending to a client that closed its session" {
                    // the first try can either be a error message, or not found
                    when {
                        firstTry.error == "message could not be sent to the P2P layer" -> {
                            success()
                        }
                        firstTry.error == "not found" -> {
                            success()
                        }
                        else -> fail("Was neither expected message")
                    }
                    // the second try HAS to be not found
                    secondTry.error.shouldBe("not found")
                }
                "still work after some close" {
                    receivedMessage.senderSessionId.shouldBe(clientTwoId)
                    receivedMessage.messageBody.shouldBe("Hello World")
                }
            }
        }
    }
}
