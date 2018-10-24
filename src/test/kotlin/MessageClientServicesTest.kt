import ch.hsr.dsl.dwrtc.signaling.ClientService
import ch.hsr.dsl.dwrtc.signaling.Future
import io.kotlintest.*
import io.kotlintest.extensions.TestListener
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

        val (clientFirst, firstFuture) = clientServiceFirst.addClient(FIRST_CLIENT_ID)
        val (clientSecond, secondFuture) = clientServiceSecond.addClient(SECOND_CLIENT_ID)

        val externalClientSecondFuture = clientServiceFirst.findClient(SECOND_CLIENT_ID)

        var message = ""

        "a client" should {
            "be able to send a message" {
                var messageFuture: Future? = null
                externalClientSecondFuture.onGet { externalClient, _ ->
                    messageFuture = clientFirst.sendMessage(
                        MESSAGE_BODY,
                        externalClient
                    )
                }
                externalClientSecondFuture.await()
                messageFuture?.onFailure { fail("message failed") } ?: fail("messageFuture not set")
                messageFuture?.await()
            }

            "be able to receive a message" {
                clientSecond.onReceiveMessage { _, messageDto -> message = messageDto.messageBody }
                externalClientSecondFuture.onGet { externalClient, _ ->
                    clientFirst.sendMessage(MESSAGE_BODY, externalClient).await()
                }
                externalClientSecondFuture.await()
                message.shouldBe(MESSAGE_BODY)
            }
        }
    }
}
