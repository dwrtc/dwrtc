package test

import ch.hsr.dsl.dwrtc.util.buildNewPeer
import ch.hsr.dsl.dwrtc.util.findFreePort
import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec
import net.tomp2p.peers.Number160
import java.util.*

class PeerBuilderTest : WordSpec() {

    init {
        "a built peer" should {
            val uuid = UUID.randomUUID()
            val port = findFreePort()
            val peer = buildNewPeer(uuid.toString(), port)

            "use the uuid and port" {
                peer.peerID().shouldBe(Number160.createHash(uuid.toString()))
                peer.peerAddress().tcpPort().shouldBe(port)
                peer.peerAddress().udpPort().shouldBe(port)
            }

        }
    }
}
