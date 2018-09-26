import io.kotlintest.Description
import io.kotlintest.TestResult
import io.kotlintest.extensions.TestListener
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import net.tomp2p.dht.PeerBuilderDHT
import net.tomp2p.dht.PeerDHT
import net.tomp2p.p2p.PeerBuilder
import net.tomp2p.peers.Number160
import net.tomp2p.storage.Data

object PeersListener : TestListener {

    lateinit var peer1: PeerDHT
    lateinit var peer2: PeerDHT

    val KEY = Number160.ONE!!
    const val VALUE = "hallo"

    override fun beforeTest(description: Description) {
        peer1 = PeerBuilderDHT(PeerBuilder(Number160.createHash("test1")).ports(4000).start()).start()
        peer2 = PeerBuilderDHT(PeerBuilder(Number160.createHash("test2")).ports(4001).start()).start()
        peer1.peer().bootstrap().peerAddress(peer2.peerAddress()).start().awaitListeners()
        peer1.put(KEY).data(Data(VALUE)).start().awaitListeners()
    }

    override fun afterTest(description: Description, result: TestResult) {
        peer1.shutdown()
        peer2.shutdown()
    }
}

class MainTest : StringSpec() {

    override fun listeners(): List<PeersListener> = listOf(PeersListener)

    init {
        "Added key should be available in DHT" {
            val listener = listeners()[0]
            listener.peer2.get(listener.KEY).start().awaitUninterruptibly().data().`object`().shouldBe(listener.VALUE)
        }
    }
}