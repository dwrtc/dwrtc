import ch.hsr.dsl.dwrtc.signaling.ClientService
import ch.hsr.dsl.dwrtc.signaling.exceptions.ClientNotFoundException
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.StringSpec
import net.tomp2p.peers.Number160
import net.tomp2p.storage.Data

class ClientServiceTest : StringSpec() {
    override fun listeners(): List<DHTListener> = listOf(DHTListener)
    val dhtService = listeners().first().generateDHT(5)
    val clientService = ClientService(dhtService.peers.first.peerAddress())

    init {
        "Added key should be available in DHT" {
            val peer1 = dhtService.peers[0]
            val peer2 = dhtService.peers[1]
            peer1.put(Number160.createHash("HelloKey")).data(Data("HelloData")).start().awaitListeners()
            peer2.get(Number160.createHash("HelloKey")).start().awaitUninterruptibly()
                .data().`object`().shouldBe("HelloData")
        }
        "An added client should be found" {
            clientService.addClient("Richi")
            clientService.findClient("Richi").sessionId.shouldBe("Richi")
        }
        "A removed client should not be found" {
            val internalClient = clientService.addClient("Harry")
            clientService.findClient("Harry").sessionId.shouldBe("Harry")
            clientService.removeClient(internalClient)
            shouldThrow<ClientNotFoundException> {
                clientService.findClient("Harry")
            }
        }

    }
}
