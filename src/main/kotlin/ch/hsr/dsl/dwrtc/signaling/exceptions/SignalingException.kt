package ch.hsr.dsl.dwrtc.signaling.exceptions

open class SignalingException(override var message: String) : Exception(message)

class ClientNotFoundException(override var message: String) : SignalingException(message)
