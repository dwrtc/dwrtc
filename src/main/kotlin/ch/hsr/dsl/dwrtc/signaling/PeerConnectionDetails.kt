package ch.hsr.dsl.dwrtc.signaling

import java.net.InetAddress

class PeerConnectionDetails(ip_address_string: String, val port: Int) {
    val ip_address: InetAddress = InetAddress.getByName(ip_address_string)
}
