package ch.hsr.dsl.dwrtc.util

import com.natpryce.konfig.*

/** The port the TomP2P peer runs on */
val PEER_PORT = Key("peer.port", intType)
/** The IP the Webserver binds to */
val WEBSERVER_IP = Key("http.ip", stringType)
/** The port the Webserver runs on */
val WEBSERVER_PORT = Key("http.port", intType)
/** The list of peers to bootstrap to. Comma-separated list of "HOST:PORT" */
val BOOTSTRAP_PEER = Key("bootstrap.peer", listType(stringType))
/** Build variable. Pack the resources in the JAR (otherwise, load from file system) */
val PACKED_RESOURCES = Key("packed.resources", booleanType)

/** The configuration object, defining the priorities in which configuration is read */
val config = ConfigurationProperties.systemProperties() overriding
        EnvironmentVariables overriding
        ConfigurationProperties.fromResource("defaults.properties")
