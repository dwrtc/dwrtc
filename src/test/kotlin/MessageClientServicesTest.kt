import ch.hsr.dsl.dwrtc.signaling.ClientService
import ch.hsr.dsl.dwrtc.signaling.exceptions.ClientNotFoundException
import io.kotlintest.*
import io.kotlintest.extensions.TestListener
import io.kotlintest.specs.WordSpec

class MessageClientServicesTest : WordSpec(), TestListener {
    override fun isInstancePerTest(): Boolean = true  // we get a new `peers` per test
    override fun testCaseOrder() = TestCaseOrder.Random // make sure tests are not dependent on each other

    companion object {
        const val FIRST_CLIENT_ID = "FIRST_CLIENT_ID"
        const val SECOND_CLIENT_ID = "SECOND_CLIENT_ID"
        const val THIRD_CLIENT_ID = "THIRD_CLIENT_ID"
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
        val externalClientSecond = clientServiceFirst.findClient(SECOND_CLIENT_ID)
        var message = ""

        clientSecond.onReceiveMessage { _, messageDto -> message = messageDto.messageBody }
        clientFirst.sendMessage(MESSAGE_BODY, externalClientSecond)


        "A sent message" should {
            "be received" {
                message.shouldBe(MESSAGE_BODY)
            }
        }
    }
}
