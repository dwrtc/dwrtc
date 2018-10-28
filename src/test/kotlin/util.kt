import ch.hsr.dsl.dwrtc.util.buildNewPeer
import io.kotlintest.matchers.boolean.shouldBeTrue
import net.tomp2p.dht.PeerDHT
import java.util.*

/**
 * Creates a DHT network of `numberOfPeers` peers
 * It always includes one additional peer that's used for bootstrapping
 */
fun generateDHT(numberOfPeers: Int): List<PeerDHT> {
    val peers = LinkedList<PeerDHT>()
    val firstPeer = buildNewPeer()
    for (i in 0..numberOfPeers) {
        val peer = buildNewPeer()
        peer.peer().bootstrap().peerAddress(firstPeer.peerAddress()).start().awaitListeners()
        peers.add(peer)
    }
    return peers
}

fun success() {
    true.shouldBeTrue()
}
