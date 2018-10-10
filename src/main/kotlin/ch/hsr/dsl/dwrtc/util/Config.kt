package ch.hsr.dsl.dwrtc.util

import com.natpryce.konfig.*

val WEBSOCKET_PORT = Key("http.port", intType)
val config = ConfigurationProperties.systemProperties() overriding
        EnvironmentVariables overriding
        ConfigurationProperties.fromResource("defaults.properties")
