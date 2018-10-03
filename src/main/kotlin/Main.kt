import io.javalin.Javalin
import io.javalin.json.JavalinJackson
import io.javalin.websocket.WsSession
import mu.KotlinLogging
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

private val logger = KotlinLogging.logger {}
val sessionMap = ConcurrentHashMap<String, WsSession>()
val peer1 = PeerBuilderDHT(PeerBuilder(Number160.createHash("test1")).ports(4000).start()).start()!!
val peer2 = PeerBuilderDHT(PeerBuilder(Number160.createHash("test2")).ports(4001).start()).start()!!

fun main(args: Array<String>) {
    // Setup DHT
    peer1.peer().bootstrap().peerAddress(peer2.peerAddress()).start().awaitListeners()
    val peer1Data = Data(peer1.peerAddress().toString())
    val peer2Data = Data(peer2.peerAddress().toString())

    fun store(sessionId: String, peerData: Data) = peer1.put(Number160.createHash(sessionId))
        .data(peerData).start()
        .await()

    fun get(sessionId: String): String {
        var futureGet = peer1.get(Number160.createHash(sessionId)).start().awaitUninterruptibly()
        logger.info { futureGet.isSuccess }
        return if (futureGet.isSuccess) {
            logger.info { futureGet.data() }
            futureGet.data().`object`().toString()
        } else "not found"
    }

    logger.info { "Hello World" }

    val app = Javalin.create().start(7000)
    app.get("/") { ctx -> ctx.result("Hello World") }
    app.ws("/websocket-first") { ws ->
        ws.onConnect { session ->
            val id = getNewId()
            sessionMap[id] = session
            logger.info { sessionMap }
            store(id, peer1Data)
            // TODO create subclasses of Message for each kind
            val message = JavalinJackson.toJson(Message(id, mapOf("id" to id).toString()))
            session.send(message)
        }
        ws.onMessage { session, message ->
            val message = JavalinJackson.fromJson(message, Message::class.java)
            logger.info { """Message from ${message.id}""" }
        }
        ws.onClose { session, statusCode, reason -> logger.info { "Closed" } }
        ws.onError { session, throwable -> logger.info { "Errored" } }
    }

    app.ws("/websocket-second") { ws ->
        ws.onConnect { session ->
            val id = getNewId()
            sessionMap[id] = session
            logger.info { sessionMap }
            store(id, peer2Data)
            // TODO create subclasses of Message for each kind

            val message = JavalinJackson.toJson(Message(id, mapOf("id" to id).toString()))
            session.send(message)
        }
        ws.onMessage { session, message ->
            val message = JavalinJackson.fromJson(message, Message::class.java)
            logger.info { """Message from ${message.id}""" }
            logger.info { """Message from ${message.message}""" }

            val peer = get(message.message)
            logger.info { "connect to $peer" }
            session.send(peer)
        }
        ws.onClose { session, statusCode, reason -> logger.info { "Closed" } }
        ws.onError { session, throwable -> logger.info { "Errored" } }
    }
}
