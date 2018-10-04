import ch.hsr.dsl.dwrtc.signaling.ClientService
import io.kotlintest.Description
import io.kotlintest.TestResult
import io.kotlintest.extensions.TestListener
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import net.tomp2p.dht.PeerBuilderDHT
import net.tomp2p.dht.PeerDHT
import net.tomp2p.p2p.PeerBuilder
import net.tomp2p.peers.Number160
import net.tomp2p.storage.Data
import java.util.*

object DHTListener : TestListener {

    val peers = LinkedList<PeerDHT>()
    private const val PORT_START = 5100

    override fun afterTest(description: Description, result: TestResult) {
        peers.forEach { it.shutdown() }
    }

    /**
     * Creates a DHT network of `numberOfPeers` peers
     * It always includes one additional peer that's used for bootstrapping
     */
    fun generateDHT(numberOfPeers: Int): DHTListener {
        val firstPeer = PeerBuilderDHT(PeerBuilder(Number160.createHash(UUID.randomUUID().toString()))
                .ports(5000).start()).start()
        for (i in 0..numberOfPeers) {
            val peer = PeerBuilderDHT(PeerBuilder(Number160.createHash(UUID.randomUUID().toString()))
                    .ports(PORT_START + i).start()).start()
            peer.peer().bootstrap().peerAddress(firstPeer.peerAddress()).start().awaitListeners()
            peers.push(peer)
        }
        return this
    }
}

class Main2Test : StringSpec() {

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
            val externalClient = clientService.findClient("Richi")
            externalClient.sessionId.shouldBe("Richi")
        }
        "A sent message should be received" {
            val FIRST_CLIENT = "Richi"
            val SECOND_CLIENT = "Hermann"
            val MESSAGE = "HalloVelo"

            val firstClient = clientService.addClient(FIRST_CLIENT)
            val secondClient = clientService.addClient(SECOND_CLIENT)

            var gotMessage = ""

            val firstexternalClient = clientService.findClient(FIRST_CLIENT)
            print(firstexternalClient.peerAddress)

            val secondExternalClient = clientService.findClient(SECOND_CLIENT)
            print(secondExternalClient.peerAddress)

            secondClient.onReceiveMessage { client, message -> gotMessage = message.messageBody }
            firstClient.sendMessage(MESSAGE, secondExternalClient)
        }
    }
}
