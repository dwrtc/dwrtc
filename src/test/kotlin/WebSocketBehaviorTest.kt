import ch.hsr.dsl.dwrtc.signaling.ClientService
import ch.hsr.dsl.dwrtc.signaling.SignalingMessage
import ch.hsr.dsl.dwrtc.util.findFreePort
import ch.hsr.dsl.dwrtc.util.jsonTo
import ch.hsr.dsl.dwrtc.util.toJson
import ch.hsr.dsl.dwrtc.websocket.WEBSOCKET_PATH
import ch.hsr.dsl.dwrtc.websocket.WebSocketErrorMessage
import ch.hsr.dsl.dwrtc.websocket.WebSocketHandler
import ch.hsr.dsl.dwrtc.websocket.WebSocketIdMessage
import io.javalin.Javalin
import io.kotlintest.TestCaseOrder
import io.kotlintest.extensions.TestListener
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
            WebSocketHandler(app, service)
            val clientOne = WebsocketClient.blocking(wsUri)
            val clientTwo = WebsocketClient.blocking(wsUri)
            val clientOneIdMessage = clientOne.received().take(1).toList().first().bodyString()
            val clientOneId = jsonTo<WebSocketIdMessage>(clientOneIdMessage).id
            val clientTwoIdMessage = clientTwo.received().take(1).toList().first().bodyString()
            val clientTwoId = jsonTo<WebSocketIdMessage>(clientTwoIdMessage).id

            val message = SignalingMessage(null, clientOneId, "Hello World")
            clientTwo.send(WsMessage(toJson(message)))
            val receivedMessageString = clientOne.received().take(1).toList().first().bodyString()
            val receivedMessage = jsonTo<SignalingMessage>(receivedMessageString)

            val invalidIdMessage = SignalingMessage(null, "NOTFOUND", "Hello World")
            clientTwo.send(WsMessage(toJson(invalidIdMessage)))
            val invalidIdReply = jsonTo<WebSocketErrorMessage>(clientTwo.received().take(1).toList().first().bodyString())

            clientOne.close()
            clientTwo.send(WsMessage(toJson(message)))  // re-send original message
            val sendingErrorReply = jsonTo<WebSocketErrorMessage>(clientTwo.received().take(1).toList().first().bodyString())

            // This test DEPENDS on the fact, that it takes a while for removeClient to do its work
            // At first, we receive that we had a problem sending the message
            // After waiting some time, we should receive that the client could not be found
            Thread.sleep(1000)

            clientTwo.send(WsMessage(toJson(message)))  // re-send original message
            val notFoundReply = jsonTo<WebSocketErrorMessage>(clientTwo.received().take(1).toList().first().bodyString())


            "be able to send messages" {
                receivedMessage.messageBody.shouldBe("Hello World")
            }
            "receive correct IDs" {
                receivedMessage.senderSessionId.shouldBe(clientTwoId)
            }
            "get an error message when sending to an unknown client" {
                invalidIdReply.error.shouldBe("not found")
            }
            "get an error message when sending to a client that closed its session" {
                sendingErrorReply.error.shouldBe("message could not be sent to the P2P layer")
                notFoundReply.error.shouldBe("not found")
            }
        }
    }
}
