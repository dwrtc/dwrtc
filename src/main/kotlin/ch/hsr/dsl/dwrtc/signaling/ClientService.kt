package ch.hsr.dsl.dwrtc.signaling

import ch.hsr.dsl.dwrtc.util.buildNewPeer
import ch.hsr.dsl.dwrtc.util.findFreePort
import ch.hsr.dsl.dwrtc.util.onFailure
import ch.hsr.dsl.dwrtc.util.onSuccess
import mu.KLogging
import net.tomp2p.dht.PeerDHT
import net.tomp2p.futures.BaseFuture
import net.tomp2p.peers.Number160
import net.tomp2p.peers.PeerAddress
import java.net.UnknownHostException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread

/** Base unit for sleep time */
const val SECOND = 1_000.toLong()

/** Connection to the P2P network */
interface IClientService {
    /** Add a new user.
     *
     * @param sessionId the user's session ID
     * @returns the IInternalClient and Future (see [Future])
     */
    fun addClient(sessionId: String): Pair<IInternalClient, Future>

    /**
     * Remove a user.
     *
     * @param client the client to remove
     * @returns see [Future]
     */
    fun removeClient(client: IInternalClient): Future

    /**
     * Find another user
     *
     * @param sessionId the user to find
     *
     * @returns see [Future]
     */
    fun findClient(sessionId: String): GetFuture<IExternalClient>

    /**
     * Add a direct message listener. See [InternalClient.onReceiveMessage]
     *
     * @param sessionId the session ID this listener is added for
     * @param emitter the callable to be called when a message is received
     */
    fun addDirectMessageListener(sessionId: String, emitter: (IExternalClient, ClientMessage) -> Unit)
}

/**
 * Connection to the P2P network.
 *
 * @constructor Creates a peer. Optionally, set the port this peer uses
 * @param peerPort the port this peer uses
 */
class ClientService constructor(peerPort: Int? = findFreePort()) : IClientService {
    /** Logging companion */
    companion object : KLogging()

    /** The peer's ID */
    private val peerId = UUID.randomUUID().toString()
    /** The TomP2P peer */
    internal var peer: PeerDHT
    /** Map of user's session ID to their message handlers. See [InternalClient.onReceiveMessage] */
    private val emitterMap = ConcurrentHashMap<String, (ExternalClient, ClientMessage) -> Unit>()
    /** Peers used for bootstrapping */
    private var bootstrapPeers: List<PeerConnectionDetails> = mutableListOf()

    init {
        this.peer = buildNewPeer(peerId, peerPort ?: findFreePort())

        logger.info {
            "creating service with peer id $peerId at port " +
                    "${peer.peerAddress().tcpPort()} (TCP)/${peer.peerAddress().udpPort()} (UDP)"
        }

        setupDirectMessageListener()
    }

    /** Creates a peer and bootstraps with a given TomP2P Peer Address. Optionally, set the port this peer uses.
     *
     * @param bootstrapPeerAddress the peer address to bootstrap with
     * @param peerPort the port this peer uses
     */
    constructor(bootstrapPeerAddress: PeerAddress, peerPort: Int? = findFreePort()) : this(peerPort) {
        logger.info { "bootstrapping with address:$bootstrapPeerAddress" }
        this.bootstrapPeers = listOf(PeerConnectionDetails(bootstrapPeerAddress.inetAddress(), bootstrapPeerAddress.tcpPort()))
        bootstrapPeer(bootstrapPeerAddress)
    }

    /** Creates a peer and bootstraps with a given IP/port pair. Optionally, set the port this peer uses.
     *
     * @param bootstrapPeers the list of [PeerConnectionDetails] to bootstrap with
     * @param peerPort the port this peer uses
     */
    constructor(bootstrapPeers: List<PeerConnectionDetails>, peerPort: Int?) : this(peerPort) {
        this.bootstrapPeers = bootstrapPeers
        if (bootstrapPeers.isNotEmpty()) {
            logger.info { "bootstrapping with $bootstrapPeers" }
            try {
                bootstrapPeers(bootstrapPeers)
            } catch (e: UnknownHostException) {
                logger.error { "Bootstrap FAILED. Peer could not be resolved: $e" }
            }
        }
    }

    override fun addClient(sessionId: String): Pair<IInternalClient, Future> {
        logger.info { "add client $sessionId" }
        logger.info { "own peer: ${peer.peerAddress()} " }

        // Ask all bootstrap servers how they see this peer
        // The first response is used
        // If no response is received, the peer's peer address is used
        val peerAddress = bootstrapPeers.map {
            logger.info { "gathering IP from $it" }
            peer.peer().discover().inetAddress(it.ipAddress).ports(it.port).start()
        }.map { it.await() }
                .filter { it.isSuccess }
                .map { it.peerAddress() }.firstOrNull() ?: peer.peerAddress()

        logger.info { "gathered peer address: $peerAddress" }

        val future = Future(peer.put(Number160.createHash(sessionId)).`object`(peerAddress).start())
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
            logger.info { "Found peer: $peerAddress" }
            ExternalClient(sessionId, peerAddress, peer)
        }

        future.onFailure { logger.info { "find client with $sessionId failed completely" } }
        future.onNotFound { logger.info { "find client with $sessionId failed (not found)" } }
        future.onGet { logger.info { "find client with $sessionId successful" } }

        return future
    }

    override fun addDirectMessageListener(sessionId: String, emitter: (IExternalClient, ClientMessage) -> Unit) {
        emitterMap[sessionId] = emitter
    }

    /**
     * Bootstrap our peer to another peer. Only used for testing!
     *
     * @param peerAddress the peer to bootstrap to
     */
    private fun bootstrapPeer(peerAddress: PeerAddress) {
        var success = false
        while (!success) {
            logger.info { "own id ${peer.peerAddress().peerId()}" }
            logger.info { "other id ${peerAddress.peerId()}" }
            val future: BaseFuture? = peer.peer()
                    .bootstrap()
                    .peerAddress(peerAddress)
                    .start()
            future?.onSuccess {
                logger.info { "bootstrapping successful" }
                success = true
            }
            future?.onFailure {
                logger.info { "bootstrapping failed: $it retrying" }
            }
            future?.awaitListeners()
        }
    }

    /**
     * Bootstrap our peer to another peer
     *
     * @param peersDetails the peer to bootstrap to
     */
    private fun bootstrapPeers(peersDetails: List<PeerConnectionDetails>) = peersDetails.forEach { details ->
        thread(isDaemon = true) {
            var sleepTime = 0.toLong()

            while (true) {
                logger.info { "bootstrapping with $details on thread ${Thread.currentThread()}" }
                val future: BaseFuture? = peer.peer()
                        .bootstrap()
                        .inetAddress(details.ipAddress)
                        .ports(details.port)
                        .start()
                future?.onSuccess {
                    logger.info { "bootstrapping with $details on thread ${Thread.currentThread()} successful" }
                    sleepTime = 60 * SECOND
                }
                future?.onFailure {
                    logger.info { "bootstrapping with $details on thread ${Thread.currentThread()} failed: $it" }
                    sleepTime = 120 * SECOND
                }
                future?.awaitListeners()
                Thread.sleep(sleepTime)
            }
        }.setUncaughtExceptionHandler { t, e -> logger.warn { "exception $e while bootstrapping in thread $t. Stacktrace: ${e.stackTrace}" } }

    }

    /** Setup the dispatcher to send the incoming messages to the correct user */
    private fun setupDirectMessageListener() {
        /** Dispatch the actual message */
        fun dispatchMessage(clientMessage: ClientMessage, senderPeerAddress: PeerAddress) {
            val recipientSessionId = clientMessage.recipientSessionId!!
            val senderSessionId = clientMessage.senderSessionId!!

            emitterMap[recipientSessionId]?.let {
                logger.info { "message accepted, found emitter for $recipientSessionId" }
                it(ExternalClient(senderSessionId, senderPeerAddress, peer), clientMessage)
            } ?: logger.info { "message discarded (no registered emitter for session id $recipientSessionId" }
        }

        /** Only dispatch a message if it's actually one of our own messages */
        fun tryDispatchingMessage(messageDto: Any?, senderPeerAddress: PeerAddress): Boolean {
            logger.info { "got message $messageDto" }
            return if (messageDto is ClientMessage) {
                dispatchMessage(messageDto, senderPeerAddress)
                true
            } else {
                logger.info { "message discarded (not a message dto)" }
                false
            }
        }

        peer.peer().objectDataReply { senderPeerAddress, messageDto ->
            tryDispatchingMessage(messageDto, senderPeerAddress)
        }
    }
}

