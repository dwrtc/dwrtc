import io.kotlintest.*
import io.kotlintest.extensions.TestListener
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.specs.WordSpec
import net.tomp2p.dht.PeerDHT
import net.tomp2p.peers.Number160
import net.tomp2p.storage.Data

class DhtTest : WordSpec(), TestListener {
    override fun isInstancePerTest(): Boolean = true
    override fun testCaseOrder() = TestCaseOrder.Random // make sure tests are not dependent on each other

    companion object {
        const val DATA_KEY = "DATA_KEY"
        val DATA_KEY_HASH = Number160.createHash(DATA_KEY)!!
        const val DATA_VALUE = "DATA_VALUE"
    }

    private lateinit var peers: List<PeerDHT>

    override fun beforeTest(description: Description) {
        println("=== BEFORETEST ${description.fullName()} ===")
        peers = generateDHT(5)
    }

    override fun afterTest(description: Description, result: TestResult) {
        println("=== AFTERTEST ${description.fullName()} ===")
        peers.forEach { it.shutdown().awaitListenersUninterruptibly() }
    }

    init {
        "A peer in a DHT network" should {
            "find existing data it put there itself" {
                println("SHOULD SEE TWICE INIT")
                peers.first().put(DATA_KEY_HASH).data(Data(DATA_VALUE)).start().awaitListenersUninterruptibly()
                val firstDataGet = peers.first().get(DATA_KEY_HASH).start().awaitListenersUninterruptibly()
                val secondDataGet = peers.last().get(DATA_KEY_HASH).start().awaitListenersUninterruptibly()
                println("SHOULD SEE ONCE FIRST")
                firstDataGet.isSuccess.shouldBeTrue()
                firstDataGet.data().shouldNotBe(null)
                firstDataGet.data().`object`().shouldBe(DATA_VALUE)
            }
            "find existing data some other peer put there" {
                println("SHOULD SEE TWICE INIT")
                peers.first().put(DATA_KEY_HASH).data(Data(DATA_VALUE)).start().awaitListenersUninterruptibly()
                val firstDataGet = peers.first().get(DATA_KEY_HASH).start().awaitListenersUninterruptibly()
                val secondDataGet = peers.last().get(DATA_KEY_HASH).start().awaitListenersUninterruptibly()
                println("SHOULD SEE ONCE SECOND")
                secondDataGet.isSuccess.shouldBeTrue()
                secondDataGet.data().shouldNotBe(null)
                secondDataGet.data().`object`().shouldBe(DATA_VALUE)
            }
        }
    }
}
