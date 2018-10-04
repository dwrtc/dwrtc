import io.kotlintest.Description
import io.kotlintest.TestResult
import io.kotlintest.extensions.TestListener
import net.tomp2p.dht.PeerBuilderDHT
import net.tomp2p.dht.PeerDHT
import net.tomp2p.p2p.PeerBuilder
import net.tomp2p.peers.Number160
import java.util.*

object DHTListener : TestListener {

    val peers = LinkedList<PeerDHT>()
    private const val PORT_START = 5100

    override fun afterTest(description: Description, result: TestResult) {
        peers.forEach { it.shutdown() }
    }

    /**
     * Creates a DHT network of `numberOfPeers` peers
     * It always includes one additional peer that's used for bootstrapping
     */
    fun generateDHT(numberOfPeers: Int): DHTListener {
        val firstPeer = PeerBuilderDHT(
            PeerBuilder(Number160.createHash(UUID.randomUUID().toString()))
                .ports(5000).start()
        ).start()
        for (i in 0..numberOfPeers) {
            val peer = PeerBuilderDHT(
                PeerBuilder(Number160.createHash(UUID.randomUUID().toString()))
                    .ports(PORT_START + i).start()
            ).start()
            peer.peer().bootstrap().peerAddress(firstPeer.peerAddress()).start().awaitListeners()
            peers.push(peer)
        }
        return this
    }
}
