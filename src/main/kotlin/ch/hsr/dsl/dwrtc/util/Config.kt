package ch.hsr.dsl.dwrtc.util

import com.natpryce.konfig.*

val WEBSOCKET_PORT = Key("http.port", intType)
val config = EnvironmentVariables overriding
        ConfigurationProperties.fromResource("defaults.properties")
// TODO find out how command line parameters are supposed to be used

