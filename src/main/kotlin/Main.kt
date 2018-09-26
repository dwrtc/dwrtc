import net.tomp2p.dht.PeerBuilderDHT
import net.tomp2p.p2p.PeerBuilder
import net.tomp2p.peers.Number160
import net.tomp2p.storage.Data

fun main(args: Array<String>) {
    println("Hello World")

    val peer1 = PeerBuilderDHT(PeerBuilder(Number160.createHash("test1")).ports(4000).start()).start()
    val peer2 = PeerBuilderDHT(PeerBuilder(Number160.createHash("test2")).ports(4001).start()).start()
    peer1.peer().bootstrap().peerAddress(peer2.peerAddress()).start().awaitListeners()
    peer1.put(Number160.ONE).data(Data("hallo")).start().awaitListeners()
    val obj = peer2.get(Number160.ONE).start().awaitUninterruptibly().data().`object`()
    println("out: $obj")
}