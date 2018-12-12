package test

import ch.hsr.dsl.dwrtc.signaling.ClientMessage
import ch.hsr.dsl.dwrtc.signaling.ClientService
import ch.hsr.dsl.dwrtc.util.findFreePort
import ch.hsr.dsl.dwrtc.util.jsonTo
import ch.hsr.dsl.dwrtc.util.toJson
import ch.hsr.dsl.dwrtc.websocket.WEBSOCKET_PATH
import ch.hsr.dsl.dwrtc.websocket.WebSocketHandler
import ch.hsr.dsl.dwrtc.websocket.WebSocketIdMessage
import io.javalin.Javalin
import io.kotlintest.TestCaseOrder
import io.kotlintest.eventually
import io.kotlintest.extensions.TestListener
import io.kotlintest.matchers.collections.shouldContainAll
import io.kotlintest.seconds
import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec
import mu.KLogging
import org.http4k.client.WebsocketClient
import org.http4k.core.Uri
import org.http4k.websocket.WsMessage

class WebSocketStressTest : WordSpec(), TestListener {
    companion object : KLogging()

    override fun testCaseOrder() = TestCaseOrder.Random // make sure tests are not dependent on each other

    private val port = findFreePort()
    private val app = Javalin.create().start(port)!!
    private val service = ClientService()
    private val wsUri = Uri.of("ws://localhost:$port$WEBSOCKET_PATH")

    init {
        "two WebSocket clients sending many messages" should {
            eventually(5.seconds) {
                WebSocketHandler(app, service)
                val clientOne = WebsocketClient.blocking(wsUri)
                val clientTwo = WebsocketClient.blocking(wsUri)
                val clientOneIdMessage = clientOne.received().take(1).toList().first().bodyString()
                val clientOneId = jsonTo<WebSocketIdMessage>(clientOneIdMessage).id
                val clientTwoIdMessage = clientTwo.received().take(1).toList().first().bodyString()
                val clientTwoId = jsonTo<WebSocketIdMessage>(clientTwoIdMessage).id

                val n = 100

                val sentMessages = mutableListOf<WsMessage>()

                for (i in 1..n) {
                    // we explicitly set the senderSessionId, so the list matches exactly
                    val message = ClientMessage("SignalingMessage", clientTwoId, clientOneId, n.toString())
                    val wsMessage = WsMessage(toJson(message))
                    sentMessages.add(wsMessage)
                    clientTwo.send(wsMessage)
                }
                
                val receivedMessages = clientOne.received().take(n).toList()

                "receive all the messages" {
                    receivedMessages.size.shouldBe(sentMessages.size)
                    receivedMessages.size.shouldBe(n)
                    receivedMessages.shouldContainAll(sentMessages)
                }
            }
        }
    }
}
