import ch.hsr.dsl.dwrtc.signaling.ClientService
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import net.tomp2p.peers.Number160
import net.tomp2p.storage.Data

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
