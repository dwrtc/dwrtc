package ch.hsr.dsl.dwrtc.websocket

import ch.hsr.dsl.dwrtc.signaling.ClientService
import ch.hsr.dsl.dwrtc.util.*
import io.javalin.Javalin
import io.javalin.staticfiles.Location
import mu.KotlinLogging
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector

/** Main start point. Fires up the Webserver. */
fun main(args: Array<String>) {
    val logger = KotlinLogging.logger {}
    val app = Javalin.create()
    if (config.getOrElse(PACKED_RESOURCES, true)) {
        logger.info { "using packed web resources" }
        app.enableStaticFiles("/public")
    } else {
        logger.info { "using file system web resources" }
        app.enableStaticFiles("src/main/resources/public/", Location.EXTERNAL)
    }
    app.server {
        Server().apply {
            connectors = arrayOf(ServerConnector(this).apply {
                this.host = config[WEBSERVER_IP]
                this.port = config[WEBSERVER_PORT]
            })
        }
    }.start()

    val clientService =
            ClientService(config.getOrNull(BOOTSTRAP_PEER), config.getOrNull(PEER_PORT))

    WebSocketHandler(app, clientService)
}
