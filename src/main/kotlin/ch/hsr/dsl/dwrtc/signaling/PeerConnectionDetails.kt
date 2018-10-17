package ch.hsr.dsl.dwrtc.signaling

import java.net.InetAddress

data class PeerConnectionDetails(val ipAddressString: String, val port: Int) {
    val ipAddress: InetAddress = InetAddress.getByName(ipAddressString)
}
