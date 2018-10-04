package ch.hsr.dsl.dwrtc.signaling

import net.tomp2p.dht.PeerBuilderDHT
import net.tomp2p.p2p.PeerBuilder
import net.tomp2p.peers.Number160
import net.tomp2p.peers.PeerAddress
import java.util.*

class ClientService() {
    private val peerId = UUID.randomUUID().toString()
    private val peer = PeerBuilderDHT(PeerBuilder(Number160.createHash(peerId)).ports(4000).start()).start()!!

    constructor(bootstrapPeerAddress: PeerConnectionDetails) : this() {
        peer.peer().bootstrap().inetAddress(bootstrapPeerAddress.ipAddress).ports(bootstrapPeerAddress.port).start()
                .awaitListeners()
    }

    fun addClient(sessionId: String): InternalClient {
        peer.put(Number160.createHash(sessionId)).`object`(peer.peerAddress()).start().awaitUninterruptibly()
        return InternalClient(peer, sessionId)
    }

    fun removeClient(internalClient: InternalClient) {
        peer.remove(Number160.createHash(internalClient.sessionId)).all().start().awaitUninterruptibly()
    }

    fun findClient(sessionId: String): ExternalClient {
        val peerIdGet = peer.get(Number160.createHash(sessionId)).start().awaitUninterruptibly()
        return if (peerIdGet.isSuccess) {
            val peerAddress = peerIdGet.data().`object`() as PeerAddress
            ExternalClient(sessionId, peerAddress)
        } else throw Exception("No peer found under session ID $sessionId")
    }
}
