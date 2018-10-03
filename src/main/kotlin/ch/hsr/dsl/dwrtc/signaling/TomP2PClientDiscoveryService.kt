package ch.hsr.dsl.dwrtc.signaling

import net.tomp2p.dht.PeerBuilderDHT
import net.tomp2p.p2p.PeerBuilder
import net.tomp2p.peers.Number160
import net.tomp2p.storage.Data
import java.util.*

class TomP2PClientDiscoveryService : ClientDiscoveryService {
    private val peerId = UUID.randomUUID().toString()
    private val peer = PeerBuilderDHT(PeerBuilder(Number160.createHash(peerId)).ports(4000).start()).start()!!

    constructor(bootstrapPeerAddress: PeerConnectionDetails) {
        peer.peer().bootstrap().inetAddress(bootstrapPeerAddress.ip_address).ports(bootstrapPeerAddress.port).start().awaitListeners()
    }

    override fun registerClient(sessionId: String) {
        peer.put(Number160.createHash(sessionId)).data(Data(peerId))
    }

    override fun deregisterClient(sessionId: String) {
        peer.remove(Number160.createHash(sessionId))
    }

    override fun findClient(sessionId: String): Client {
        val peerIdGet = peer.get(Number160.createHash(sessionId)).start().awaitUninterruptibly()
        return if (peerIdGet.isSuccess) {
            val peerId = peerIdGet.data().`object`().toString()
            TomP2PClient(peer, sessionId, peerId)
        } else throw Exception("No peer found under session ID $sessionId")
    }
}
