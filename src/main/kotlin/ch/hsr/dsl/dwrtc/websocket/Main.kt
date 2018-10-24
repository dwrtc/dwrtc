package ch.hsr.dsl.dwrtc.websocket

import ch.hsr.dsl.dwrtc.signaling.ClientService
import ch.hsr.dsl.dwrtc.util.*
import io.javalin.Javalin
import io.javalin.staticfiles.Location

/** Main start point. Fires up the Webserver. */
fun main(args: Array<String>) {
    val app = Javalin.create()
            .enableStaticFiles("src/main/resources/public/", Location.EXTERNAL)
            // or .enableStaticFiles("/public") to use Class Path
            .start(config[WEBSERVER_PORT])

    val clientService =
            ClientService(config.getOrNull(BOOTSTRAP_IP), config.getOrNull(BOOTSTRAP_PORT), config.getOrNull(PEER_PORT))

    WebSocketHandler(app, clientService)
}
