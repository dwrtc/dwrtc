package ch.hsr.dsl.dwrtc.signaling

import net.tomp2p.dht.PeerBuilderDHT
import net.tomp2p.p2p.PeerBuilder
import net.tomp2p.peers.Number160
import net.tomp2p.peers.PeerAddress
import net.tomp2p.storage.Data

class TomP2PClientDiscoveryService(bootrapPeerAddress: PeerAddress): ClientDiscoveryService {
    private val peer = PeerBuilderDHT(PeerBuilder(Number160.createHash("test1")).ports(4000).start()).start()!!

    init {
        peer.peer().bootstrap().peerAddress(bootrapPeerAddress).start().awaitListeners()
    }

    override fun registerClient(sessionId: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deregisterClient(sessionId: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findClient(id: String): Client {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun findPeer(sessionId: String): PeerAddress = PeerAddress(peer
            .get(Number160.createHash(sessionId))
            .start()
            .await()
            .data()
            .`object`() as ByteArray)

    private fun peerAddressData(): Data = Data(peer.peerAddress().toByteArray())
}
