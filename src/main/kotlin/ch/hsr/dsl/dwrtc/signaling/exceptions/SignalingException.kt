package ch.hsr.dsl.dwrtc.signaling.exceptions

open class SignalingException(message: String) : Exception(message)

class ClientNotFoundException(message: String) : SignalingException(message)
