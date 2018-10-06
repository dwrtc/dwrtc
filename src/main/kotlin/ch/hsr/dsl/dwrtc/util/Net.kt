package ch.hsr.dsl.dwrtc.util

import java.net.ServerSocket

fun findFreePort() = ServerSocket(0).use { it.localPort }
