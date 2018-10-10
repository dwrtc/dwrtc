package ch.hsr.dsl.dwrtc.websocket

import ch.hsr.dsl.dwrtc.signaling.ClientService
import ch.hsr.dsl.dwrtc.util.WEBSERVER_PORT
import ch.hsr.dsl.dwrtc.util.config
import io.javalin.Javalin

fun main(args: Array<String>) {
    val app = Javalin.create().start(config[WEBSERVER_PORT])
    app.get("/") { ctx -> ctx.result("root") }

    val service = ClientService()

    WebsocketHandler(app, service)
}
