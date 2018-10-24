package ch.hsr.dsl.dwrtc.util

import java.net.ServerSocket

/** Find a free port */
fun findFreePort() = ServerSocket(0).use { it.localPort }
