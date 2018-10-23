package ch.hsr.dsl.dwrtc.signaling

import ch.hsr.dsl.dwrtc.signaling.exceptions.ClientNotFoundException
import ch.hsr.dsl.dwrtc.util.buildNewPeer
import ch.hsr.dsl.dwrtc.util.findFreePort
import ch.hsr.dsl.dwrtc.util.onSuccess
import mu.KLogging
import net.tomp2p.dht.PeerDHT
import net.tomp2p.peers.Number160
import net.tomp2p.peers.PeerAddress
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Connection to the P2P network
 */
class ClientService constructor(peerPort: Int? = findFreePort()) {
    companion object : KLogging()

    private val peerId = UUID.randomUUID().toString()
    internal var peer: PeerDHT
    private val emitterMap = ConcurrentHashMap<String, (ExternalClient, SignalingMessage) -> Unit>()

    init {
        this.peer = buildNewPeer(peerId, peerPort ?: findFreePort())

        logger.info {
            "creating service with peer id $peerId at port " +
                    "${peer.peerAddress().tcpPort()} (TCP)/${peer.peerAddress().udpPort()} (UDP)"
        }

        setupDirectMessageListener()
    }

    constructor(bootstrapPeerAddress: PeerAddress, peerPort: Int? = findFreePort()) : this(peerPort) {
        logger.info { "bootstrapping with address:$bootstrapPeerAddress" }
        bootstrapPeer(bootstrapPeerAddress).onSuccess { logger.info { "bootstrapping completed" } }

    }

    constructor(bootstrapIp: String?, bootstrapPort: Int?, peerPort: Int?) : this(peerPort) {
        if (bootstrapIp != null && bootstrapPort != null) {
            logger.info { "bootstrapping with $bootstrapIp:$bootstrapPort" }
            bootstrapPeer(PeerConnectionDetails(bootstrapIp, bootstrapPort))
        }
    }

    fun addClient(sessionId: String): InternalClient {
        logger.info { "add client $sessionId" }
        logger.info { "own peer: ${peer.peerAddress()} " }

        peer.put(Number160.createHash(sessionId)).`object`(peer.peerAddress()).start().awaitUninterruptibly()
        return InternalClient(peer, this, sessionId)
    }

    fun removeClient(internalClient: InternalClient) {
        logger.info { "remove client ${internalClient.sessionId}" }

        peer.remove(Number160.createHash(internalClient.sessionId)).all().start().awaitUninterruptibly()
        emitterMap.remove(internalClient.sessionId)
    }

    fun findClient(sessionId: String): ExternalClient {
        logger.info { "try to find client $sessionId" }

        val peerIdGet = peer.get(Number160.createHash(sessionId)).start().awaitUninterruptibly()
        return if (peerIdGet.isSuccess && peerIdGet.data() != null) {
            logger.info { "found client" }

            val peerAddress = peerIdGet.data().`object`() as PeerAddress
            ExternalClient(sessionId, peerAddress)
        } else throw ClientNotFoundException("No peer found under session ID $sessionId")
    }

    internal fun addDirectMessageListener(sessionId: String, emitter: (ExternalClient, SignalingMessage) -> Unit) {
        emitterMap[sessionId] = emitter
    }

    private fun bootstrapPeer(peerAddress: PeerAddress) = peer.peer()
        .bootstrap()
        .peerAddress(peerAddress)
        .start().awaitListeners()

    private fun bootstrapPeer(peerDetails: PeerConnectionDetails) = peer.peer()
        .bootstrap()
        .inetAddress(peerDetails.ipAddress)
        .ports(peerDetails.port)
        .start().awaitListeners()

    private fun setupDirectMessageListener() {
        fun dispatchMessage(signalingMessage: SignalingMessage, senderPeerAddress: PeerAddress) {
            val recipientSessionId = signalingMessage.recipientSessionId!!
            val senderSessionId = signalingMessage.senderSessionId!!
            emitterMap[recipientSessionId]?.let {
                logger.info { "message accepted, found emitter for $recipientSessionId" }
                it(ExternalClient(senderSessionId, senderPeerAddress), signalingMessage)
            } ?: run {
                logger.info { "message discarded (no registered emitter for session id $recipientSessionId" }
            }
        }

        fun tryDispatchingMessage(messageDto: Any?, senderPeerAddress: PeerAddress): Any {
            logger.info { "got message $messageDto" }
            return if (messageDto is SignalingMessage) {
                dispatchMessage(messageDto, senderPeerAddress)
                messageDto
            } else {
                logger.info { "message discarded (not a message dto)" }
            }
        }

        peer.peer().objectDataReply { senderPeerAddress, messageDto ->
            tryDispatchingMessage(messageDto, senderPeerAddress)
        }
    }
}
