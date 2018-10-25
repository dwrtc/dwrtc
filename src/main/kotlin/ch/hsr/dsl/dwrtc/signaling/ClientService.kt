package ch.hsr.dsl.dwrtc.signaling

import ch.hsr.dsl.dwrtc.util.buildNewPeer
import ch.hsr.dsl.dwrtc.util.findFreePort
import mu.KLogging
import net.tomp2p.dht.PeerDHT
import net.tomp2p.peers.Number160
import net.tomp2p.peers.PeerAddress
import util.onSuccess
import java.util.*
import java.util.concurrent.ConcurrentHashMap


interface IClientService {
    fun addClient(sessionId: String): Pair<IInternalClient, Future>
    fun removeClient(client: IInternalClient): Future
    fun findClient(sessionId: String): GetFuture<IExternalClient>
    fun addDirectMessageListener(sessionId: String, emitter: (IExternalClient, SignalingMessage) -> Unit)
}

class ClientService constructor(peerPort: Int? = findFreePort()) : IClientService {
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

    override fun addClient(sessionId: String): Pair<IInternalClient, Future> {
        logger.info { "add client $sessionId" }
        logger.info { "own peer: ${peer.peerAddress()} " }

        val future = Future(peer.put(Number160.createHash(sessionId)).`object`(peer.peerAddress()).start())
        return Pair(InternalClient(peer, this, sessionId), future)
    }

    override fun removeClient(client: IInternalClient): Future {
        logger.info { "remove client ${client.sessionId}" }

        val future = Future(peer.remove(Number160.createHash(client.sessionId)).all().start())
        future.onComplete { emitterMap.remove(client.sessionId) }
        future.onFailure { reason -> logger.info { "client remove failed $reason" } }

        return future
    }

    override fun findClient(sessionId: String): GetFuture<IExternalClient> {
        logger.info { "try to find client $sessionId" }

        val dhtFuture = peer.get(Number160.createHash(sessionId)).start()
        val future = GetCustomFuture<IExternalClient, PeerAddress>(dhtFuture) { peerAddress ->
            ExternalClient(sessionId, peerAddress, peer)
        }

        future.onFailure { logger.info { "find client with $sessionId failed" } }
        future.onSuccess { logger.info { "find client with $sessionId successful" } }

        return future
    }

    override fun addDirectMessageListener(sessionId: String, emitter: (IExternalClient, SignalingMessage) -> Unit) {
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
                it(ExternalClient(senderSessionId, senderPeerAddress, peer), signalingMessage)
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
