package test

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

/** Returns a successful test */
fun success() {
    true.shouldBeTrue()
}

/** Sets and resets a system property to ensure nothing else in the same JVM suddenly fails */
class PropertySetter(private val key: String, private val value: String) {
    private var oldProperty: String? = System.getProperty(key)

    fun set() {
        System.setProperty(key, value)
    }

    fun reset() {
        if (oldProperty != null) {
            System.setProperty(key, oldProperty)
        }
    }
}
