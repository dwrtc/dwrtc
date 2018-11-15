package test

import ch.hsr.dsl.dwrtc.signaling.ClientService
import ch.hsr.dsl.dwrtc.signaling.IExternalClient
import io.kotlintest.Description
import io.kotlintest.TestCaseOrder
import io.kotlintest.TestResult
import io.kotlintest.extensions.TestListener
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec

class MessageClientServicesTest : WordSpec(), TestListener {
    override fun isInstancePerTest(): Boolean = true
    override fun testCaseOrder() = TestCaseOrder.Random // make sure tests are not dependent on each other

    companion object {
        const val FIRST_CLIENT_ID = "FIRST_CLIENT_ID"
        const val SECOND_CLIENT_ID = "SECOND_CLIENT_ID"
        const val MESSAGE_BODY = "MESSAGE_BODY"
    }

    private val peers = generateDHT(5)

    override fun afterTest(description: Description, result: TestResult) {
        peers.forEach { it.shutdown().awaitListeners() }
    }

    init {
        val clientServiceFirst = ClientService(peers.first().peerAddress())
        val clientServiceSecond = ClientService(peers.last().peerAddress())

        val (clientFirst, firstFuture) = clientServiceFirst.addClient(FIRST_CLIENT_ID)
        val (clientSecond, secondFuture) = clientServiceSecond.addClient(SECOND_CLIENT_ID)

        firstFuture.await()
        secondFuture.await()

        "a client" should {
            val externalClientSecondFuture = clientServiceFirst.findClient(SECOND_CLIENT_ID)
            var externalClient: IExternalClient? = null
            externalClientSecondFuture.onGet { client -> externalClient = client }
            externalClientSecondFuture.await().awaitListeners()

            "be able to send a message" {

                var success = false
                val messageFuture = clientFirst.sendMessage(
                        "SignalingMessage",
                        MESSAGE_BODY,
                        externalClient!!
                )
                messageFuture.onComplete { success = true }
                messageFuture.awaitListeners()
                success.shouldBeTrue()
            }

            "be able to receive a message" {
                var message = ""
                clientSecond.onReceiveMessage { _, messageDto -> message = messageDto.messageBody }
                clientFirst.sendMessage("SignalingMessage", MESSAGE_BODY, externalClient!!).awaitListeners()
                message.shouldBe(MESSAGE_BODY)
            }
        }
    }
}
