package ch.hsr.dsl.dwrtc.signaling

import ch.hsr.dsl.dwrtc.signaling.exceptions.ClientNotFoundException
import ch.hsr.dsl.dwrtc.util.buildNewPeer
import ch.hsr.dsl.dwrtc.util.findFreePort
import mu.KLogging
import net.tomp2p.dht.PeerDHT
import net.tomp2p.peers.Number160
import net.tomp2p.peers.PeerAddress
import util.onSuccess
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/** Connection to the P2P network
 */
interface IClientService {
    /** Add a new user.
     *
     * @param sessionId the user's session ID
     * @returns the built InternalClient
     */
    fun addClient(sessionId: String): IInternalClient

    /**
     * Remove a user.
     *
     * @param client the client to remove
     */
    fun removeClient(client: IInternalClient)

    /**
     * Find another user
     *
     * @param sessionId the user to find
     * @throws ClientNotFoundException when a user is not found
     */
    fun findClient(sessionId: String): IExternalClient

    /**
     * Add a direct message listener. See [InternalClient.onReceiveMessage]
     *
     * @param sessionId the session ID this listener is added for
     * @param emitter the callable to be called when a message is received
     */
    fun addDirectMessageListener(sessionId: String, emitter: (IExternalClient, SignalingMessage) -> Unit)
}

/**
 * Connection to the P2P network.
 *
 * @constructor Creates a peer. Optionally, set the port this peer uses
 * @param peerPort the port this peer uses
 */
class ClientService constructor(peerPort: Int? = findFreePort()) : IClientService {
    companion object : KLogging()

    /** The peer's ID */
    private val peerId = UUID.randomUUID().toString()
    /** The TomP2P peer */
    internal var peer: PeerDHT
    /** Map of user's session ID to their message handlers. See [InternalClient.onReceiveMessage] */
    private val emitterMap = ConcurrentHashMap<String, (ExternalClient, SignalingMessage) -> Unit>()

    init {
        this.peer = buildNewPeer(peerId, peerPort ?: findFreePort())

        logger.info {
            "creating service with peer id $peerId at port " +
                    "${peer.peerAddress().tcpPort()} (TCP)/${peer.peerAddress().udpPort()} (UDP)"
        }

        setupDirectMessageListener()
    }

    /** Creates a peer and bootstraps. Optionally, set the port this peer uses.
     *
     * @param bootstrapPeerAddress the peer address to bootstrap with
     * @param peerPort the port this peer uses
     */
    constructor(bootstrapPeerAddress: PeerAddress, peerPort: Int? = findFreePort()) : this(peerPort) {
        logger.info { "bootstrapping with address:$bootstrapPeerAddress" }
        bootstrapPeer(bootstrapPeerAddress).onSuccess { logger.info { "bootstrapping completed" } }
    }

    /**
     *
     * @param bootstrapIp the peer's IP to bootstrap with
     * @param bootstrapPort the peer's port to bootstrap with
     * @param peerPort the port this peer uses
     */
    constructor(bootstrapIp: String?, bootstrapPort: Int?, peerPort: Int?) : this(peerPort) {
        if (bootstrapIp != null && bootstrapPort != null) {
            logger.info { "bootstrapping with $bootstrapIp:$bootstrapPort" }
            bootstrapPeer(PeerConnectionDetails(bootstrapIp, bootstrapPort))
        }
    }

    override fun addClient(sessionId: String): IInternalClient {
        logger.info { "add client $sessionId" }
        logger.info { "own peer: ${peer.peerAddress()} " }

        peer.put(Number160.createHash(sessionId)).`object`(peer.peerAddress()).start().awaitUninterruptibly()
        return InternalClient(peer, this, sessionId)
    }

    override fun removeClient(client: IInternalClient) {
        logger.info { "remove client ${client.sessionId}" }

        peer.remove(Number160.createHash(client.sessionId)).all().start().awaitUninterruptibly()
        emitterMap.remove(client.sessionId)
    }

    override fun findClient(sessionId: String): IExternalClient {
        logger.info { "try to find client $sessionId" }

        val peerIdGet = peer.get(Number160.createHash(sessionId)).start().awaitUninterruptibly()
        return if (peerIdGet.isSuccess && peerIdGet.data() != null) {
            logger.info { "found client" }

            val peerAddress = peerIdGet.data().`object`() as PeerAddress
            ExternalClient(sessionId, peerAddress, peer)
        } else throw ClientNotFoundException("No peer found under session ID $sessionId")
    }

    override fun addDirectMessageListener(sessionId: String, emitter: (IExternalClient, SignalingMessage) -> Unit) {
        emitterMap[sessionId] = emitter
    }

    /**
     * Bootstrap our peer to another peer
     *
     * @param peerAddress the peer to bootstrap to
     */
    private fun bootstrapPeer(peerAddress: PeerAddress) = peer.peer()
            .bootstrap()
            .peerAddress(peerAddress)
            .start().awaitListeners()

    /**
     * Bootstrap our peer to another peer
     *
     * @param peerDetails the peer to bootstrap to
     */
    private fun bootstrapPeer(peerDetails: PeerConnectionDetails) = peer.peer()
            .bootstrap()
            .inetAddress(peerDetails.ipAddress)
            .ports(peerDetails.port)
            .start().awaitListeners()

    /**
     * Setup the dispatcher to send the incoming messages to the correct user
     */
    private fun setupDirectMessageListener() {
        /**
         * Dispatch the actual message
         */
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

        /**
         * Only dispatch a message if it's actually one of our own messages
         */
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

