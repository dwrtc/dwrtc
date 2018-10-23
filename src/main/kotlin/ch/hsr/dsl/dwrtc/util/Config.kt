package ch.hsr.dsl.dwrtc.util

import com.natpryce.konfig.*

val PEER_PORT = Key("peer.port", intType)
val WEBSERVER_PORT = Key("http.port", intType)
val BOOTSTRAP_IP = Key("bootstrap.ip", stringType)
val BOOTSTRAP_PORT = Key("bootstrap.port", intType)

val config = ConfigurationProperties.systemProperties() overriding
        EnvironmentVariables overriding
        ConfigurationProperties.fromResource("defaults.properties")
