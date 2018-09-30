import io.javalin.Javalin
import io.javalin.json.JavalinJackson
import io.javalin.websocket.WsSession
import net.tomp2p.dht.PeerBuilderDHT
import net.tomp2p.p2p.PeerBuilder
import net.tomp2p.peers.Number160
import net.tomp2p.storage.Data
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 *Generates a new, unique ID
 * TODO replace this with code that checks the ID is not yet used in the DHT
 */
fun getNewId(): String {
    return UUID.randomUUID().toString()
}

val sessionMap = ConcurrentHashMap<String, WsSession>()
val peer1 = PeerBuilderDHT(PeerBuilder(Number160.createHash("test1")).ports(4000).start()).start()
val peer2 = PeerBuilderDHT(PeerBuilder(Number160.createHash("test2")).ports(4001).start()).start()

fun main(args: Array<String>) {
    // Setup DHT
    peer1.peer().bootstrap().peerAddress(peer2.peerAddress()).start().awaitListeners()
    val peer1Data = Data(peer1.peerAddress().toByteArray())
    val peer2Data = Data(peer2.peerAddress().toByteArray())

    println("Hello World")

    val app = Javalin.create().start(7000)
    app.get("/") { ctx -> ctx.result("Hello World") }
    app.ws("/websocket1") { ws ->
        ws.onConnect { session ->
            val id = getNewId()
            sessionMap[id] = session
            peer1.put(Number160.createHash(id)).data(peer1Data).start().awaitListeners()
            // TODO create subclasses of Message for each kind
            val message = JavalinJackson.toJson(Message(id, mapOf("id" to id).toString()))
            session.send(message)
        }
        ws.onMessage { session, message ->
            // val message = JavalinJackson.fromJson<Message>(message, Message::javaClass)
            // println("Message from " + message.)
        }
        ws.onClose { session, statusCode, reason -> println("Closed") }
        ws.onError { session, throwable -> println("Errored") }
    }
}
