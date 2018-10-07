import ch.hsr.dsl.dwrtc.signaling.ClientService
import io.kotlintest.Description
import io.kotlintest.TestCaseOrder
import io.kotlintest.TestResult
import io.kotlintest.extensions.TestListener
import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec

class MessageClientServicesTest : WordSpec(), TestListener {
    override fun testCaseOrder() = TestCaseOrder.Random // make sure tests are not dependent on each other

    companion object {
        const val FIRST_CLIENT_ID = "FIRST_CLIENT_ID"
        const val SECOND_CLIENT_ID = "SECOND_CLIENT_ID"
        const val MESSAGE_BODY = "MESSAGE_BODY"
    }

    private val peers = generateDHT(5)

    override fun afterTest(description: Description, result: TestResult) {
        peers.forEach { it.shutdown().awaitListenersUninterruptibly() }
    }

    init {
        val clientServiceFirst = ClientService(peers.first().peerAddress())
        val clientServiceSecond = ClientService(peers.last().peerAddress())

        val clientFirst = clientServiceFirst.addClient(FIRST_CLIENT_ID)
        val clientSecond = clientServiceSecond.addClient(SECOND_CLIENT_ID)

        val externalClientSecond = clientServiceFirst.findClient(SECOND_CLIENT_ID) // TODO inconsistent

        var message = ""

        "a client" should {
            "be able to send a message" {
                clientFirst.sendMessage(MESSAGE_BODY, externalClientSecond)
            }

            "be able to receive a message" {
                clientSecond.onReceiveMessage { _, messageDto -> message = messageDto.messageBody }
                clientFirst.sendMessage(MESSAGE_BODY, externalClientSecond)
                message.shouldBe(MESSAGE_BODY)
            }
        }
    }
}
