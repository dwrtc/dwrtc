package test

import ch.hsr.dsl.dwrtc.signaling.ClientService
import io.kotlintest.Description
import io.kotlintest.TestCaseOrder
import io.kotlintest.TestResult
import io.kotlintest.extensions.TestListener
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec

class BasicClientServiceTest : WordSpec(), TestListener {
    override fun isInstancePerTest(): Boolean = true
    override fun testCaseOrder() = TestCaseOrder.Random // make sure tests are not dependent on each other

    companion object {
        const val FIRST_CLIENT_ID = "FIRST_CLIENT_ID"
        const val SECOND_CLIENT_ID = "SECOND_CLIENT_ID"
        const val THIRD_CLIENT_ID = "THIRD_CLIENT_ID"
    }

    private val peers = generateDHT(5)

    override fun afterTest(description: Description, result: TestResult) {
        peers.forEach { it.shutdown().awaitListenersUninterruptibly() }
    }

    init {
        val clientServiceFirst = ClientService(peers.first().peerAddress())
        val clientServiceSecond = ClientService(peers.last().peerAddress())
        clientServiceFirst.addClient(FIRST_CLIENT_ID).second.awaitListeners()
        clientServiceSecond.addClient(SECOND_CLIENT_ID).second.awaitListeners()
        val (thirdClient, clientFuture) = clientServiceFirst.addClient(THIRD_CLIENT_ID)
        clientFuture.onComplete { clientServiceFirst.removeClient(thirdClient).awaitListeners() }
        clientFuture.awaitListeners().await()

        "Two Client Services" should {
            "find clients on the same one" {
                var sessionId = ""
                val future = clientServiceFirst.findClient(FIRST_CLIENT_ID)
                future.onGet { externalClient -> sessionId = externalClient.sessionId }
                future.awaitListeners().await()
                sessionId.shouldBe(FIRST_CLIENT_ID)
            }
            "find clients of another one" {
                var firstSessionId = ""
                var secondSessionId = ""

                val firstFuture = clientServiceFirst.findClient(SECOND_CLIENT_ID)
                firstFuture.onGet { client -> secondSessionId = client.sessionId }

                val secondFuture = clientServiceSecond.findClient(FIRST_CLIENT_ID)
                secondFuture.onGet { client -> firstSessionId = client.sessionId }

                firstFuture.awaitListeners()
                secondFuture.awaitListeners()

                firstSessionId.shouldBe(FIRST_CLIENT_ID)
                secondSessionId.shouldBe(SECOND_CLIENT_ID)
            }
            "not find old clients" {
                var success = false
                val findFuture = clientServiceFirst.findClient(THIRD_CLIENT_ID)
                findFuture.onNotFound {
                    success = true
                }
                findFuture.awaitListeners()

                success.shouldBeTrue()
            }
        }
    }
}
