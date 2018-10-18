package ch.hsr.dsl.dwrtc.util

import net.tomp2p.dht.PeerBuilderDHT
import net.tomp2p.dht.PeerDHT
import net.tomp2p.p2p.PeerBuilder
import net.tomp2p.peers.Number160
import java.util.*

fun buildNewPeer(id: String = UUID.randomUUID().toString(), port: Int = findFreePort()): PeerDHT {
    return PeerBuilderDHT(PeerBuilder(Number160.createHash(id)).ports(port).start()).start()!!
}
