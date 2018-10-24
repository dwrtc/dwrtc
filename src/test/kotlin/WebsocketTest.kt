import ch.hsr.dsl.dwrtc.signaling.ClientService
import ch.hsr.dsl.dwrtc.util.findFreePort
import ch.hsr.dsl.dwrtc.util.jsonTo
import ch.hsr.dsl.dwrtc.websocket.WEBSOCKET_PATH
import ch.hsr.dsl.dwrtc.websocket.WebSocketHandler
import ch.hsr.dsl.dwrtc.websocket.WebSocketIdMessage
import io.javalin.Javalin
import io.kotlintest.TestCaseOrder
import io.kotlintest.extensions.TestListener
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.matchers.string.shouldNotBeBlank
import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec
import mu.KLogging
import org.http4k.client.WebsocketClient
import org.http4k.core.Uri

class WebSocketTest : WordSpec(), TestListener {
    companion object : KLogging()

    override fun testCaseOrder() = TestCaseOrder.Random // make sure tests are not dependent on each other

    private val port = findFreePort()
    private val app = Javalin.create().start(port)!!
    private val service = ClientService()
    private val wsUri = Uri.of("ws://localhost:$port$WEBSOCKET_PATH")

    init {
        "the initial message" should {
            WebSocketHandler(app, service)
            val client = WebsocketClient.blocking(wsUri)
            val firstMessageString = client.received().take(1).toList().first().bodyString()

            "be accessible under the specified port" {
                firstMessageString.shouldNotBeBlank()
            }

            "be a WebSocketIdMessage" {
                firstMessageString.shouldContain("WebSocketIdMessage")
                firstMessageString.shouldContain("id")
            }

            "have a correct id" {
                val firstMessage = jsonTo<WebSocketIdMessage>(firstMessageString)
                firstMessage.id.length.shouldBe(36)
            }

            "have the correct type" {
                val firstMessage = jsonTo<WebSocketIdMessage>(firstMessageString)
                firstMessage.type.shouldBe("WebSocketIdMessage")
            }
        }
    }
}
