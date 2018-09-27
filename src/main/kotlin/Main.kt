import io.javalin.Javalin

fun main(args: Array<String>) {
    println("Hello World")

    val app = Javalin.create().start(7000)
    app.get("/") { ctx -> ctx.result("Hello World") }
    app.ws("/websocket") { ws ->
        ws.onConnect { session -> println("Connected") }
        ws.onMessage { session, message ->
            println("Received: " + message)
            session.remote.sendString("Echo: " + message)
        }
        ws.onClose { session, statusCode, reason -> println("Closed") }
        ws.onError { session, throwable -> println("Errored") }
    }
}
