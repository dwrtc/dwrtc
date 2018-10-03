package ch.hsr.dsl.dwrtc.signaling

interface ClientDiscoveryService {
    fun registerClient(id: String)
    fun deregisterClient(id: String)
    fun findClient(id: String): Client
}
