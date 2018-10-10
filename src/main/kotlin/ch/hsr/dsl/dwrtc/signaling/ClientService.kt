package ch.hsr.dsl.dwrtc.signaling

import ch.hsr.dsl.dwrtc.signaling.exceptions.ClientNotFoundException
import ch.hsr.dsl.dwrtc.util.buildNewPeer
import mu.KLogging
import net.tomp2p.peers.Number160
import net.tomp2p.peers.PeerAddress
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class ClientService() {
    companion object : KLogging()

    private val peerId = UUID.randomUUID().toString()
    internal val peer = buildNewPeer(peerId)
    private val emitterMap = ConcurrentHashMap<String, (ExternalClient, MessageDto) -> Unit>()

    init {
        logger.info {
            "creating service with peer id $peerId at port " +
                    "${peer.peerAddress().tcpPort()} (TCP)/${peer.peerAddress().udpPort()} (UDP)"
        }

        setupDirectMessageListener()
    }

    private fun setupDirectMessageListener() {
        peer.peer().objectDataReply { senderPeerAddress, messageDto ->
            logger.info { "got message $messageDto" }
            if (messageDto !is MessageDto) {
                logger.info { "message discarded (not a message dto)" }
            } else {
                val recipientSessionId = messageDto.recipientSessionId
                emitterMap[recipientSessionId]?.let {
                    logger.info { "message accepted, found emitter for $recipientSessionId" }
                    it(ExternalClient(messageDto.senderSessionId, senderPeerAddress), messageDto)
                } ?: run {
                    logger.info { "message discarded (no registered emitter for session id $recipientSessionId" }
                }
                logger.info { "message discarded (not a message dto)" }
                messageDto
            }
        }
    }

    constructor(bootstrapPeerAddress: PeerConnectionDetails) : this() {
        logger.info { "using bootstrap peer $bootstrapPeerAddress" }

        peer.peer().bootstrap().inetAddress(bootstrapPeerAddress.ipAddress).ports(bootstrapPeerAddress.port).start()
                .awaitListeners()
    }

    constructor(bootstrapPeerAddress: PeerAddress) : this() {
        logger.info { "using bootstrap peer (TomP2P format) $bootstrapPeerAddress" }

        peer.peer().bootstrap().peerAddress(bootstrapPeerAddress).start()
                .awaitListeners()
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

    internal fun addDirectMessageListener(sessionId: String, emitter: (ExternalClient, MessageDto) -> Unit) {
        emitterMap[sessionId] = emitter
    }
}
