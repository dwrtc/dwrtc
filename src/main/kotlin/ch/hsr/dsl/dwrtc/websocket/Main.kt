package ch.hsr.dsl.dwrtc.websocket

import ch.hsr.dsl.dwrtc.signaling.ClientService
import ch.hsr.dsl.dwrtc.util.*
import io.javalin.Javalin
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector

/** Main start point. Fires up the Webserver. */
fun main(args: Array<String>) {
    val app = Javalin.create()
            // .enableStaticFiles("src/main/resources/public/", Location.EXTERNAL)
            .enableStaticFiles("/public").server {
                Server().apply {
                    connectors = arrayOf(ServerConnector(this).apply {
                        this.host = config[WEBSERVER_IP]
                        this.port = config[WEBSERVER_PORT]
                    })
                }
            }
            .start()

    val clientService =
            ClientService(config.getOrNull(BOOTSTRAP_PEER), ch.hsr.dsl.dwrtc.util.config.getOrNull(PEER_PORT))

    WebSocketHandler(app, clientService)
}
