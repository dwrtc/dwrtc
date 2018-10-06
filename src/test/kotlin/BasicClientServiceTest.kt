import ch.hsr.dsl.dwrtc.signaling.ClientService
import ch.hsr.dsl.dwrtc.signaling.exceptions.ClientNotFoundException
import io.kotlintest.*
import io.kotlintest.extensions.TestListener
import io.kotlintest.specs.WordSpec

class BasicClientServiceTest : WordSpec(), TestListener {
    override fun isInstancePerTest(): Boolean = true  // we get a new `peers` per test
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
        clientServiceFirst.addClient(FIRST_CLIENT_ID)
        clientServiceSecond.addClient(SECOND_CLIENT_ID)
        clientServiceFirst.removeClient(clientServiceFirst.addClient(THIRD_CLIENT_ID))

        "Two Client Services" should {
            "find clients on the same one" {
                clientServiceFirst.findClient(FIRST_CLIENT_ID).sessionId.shouldBe(FIRST_CLIENT_ID)
            }
            "find clients of another one" {
                clientServiceFirst.findClient(SECOND_CLIENT_ID).sessionId.shouldBe(SECOND_CLIENT_ID)
                clientServiceSecond.findClient(FIRST_CLIENT_ID).sessionId.shouldBe(FIRST_CLIENT_ID)
            }
            "not find old clients" {
                shouldThrow<ClientNotFoundException> {
                    clientServiceFirst.findClient(THIRD_CLIENT_ID)
                }
                shouldThrow<ClientNotFoundException> {
                    clientServiceSecond.findClient(THIRD_CLIENT_ID)
                }
            }
            "find the correct peer address" {
                clientServiceFirst.findClient(SECOND_CLIENT_ID).peerAddress.shouldBe(clientServiceSecond.peer.peerAddress())
            }
        }
    }
}
