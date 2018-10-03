package ch.hsr.dsl.dwrtc.signaling

interface ClientDiscoveryService {
    fun registerClient(sessionId: String)
    fun deregisterClient(sessionId: String)
    fun findClient(sessionId: String): Client
}
