package ch.hsr.dsl.dwrtc.signaling

import java.net.InetAddress

class PeerConnectionDetails(ipAddressString: String, val port: Int) {
    val ipAddress: InetAddress = InetAddress.getByName(ipAddressString)
}
