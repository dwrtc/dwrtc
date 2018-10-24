package ch.hsr.dsl.dwrtc.signaling.exceptions

/**
 * Base class for all exceptions that may happen during signaling.
 *
 * @property message the exception message
 */
open class SignalingException(message: String) : Exception(message)

/**
 * The requested user was not found in the DHT.
 */
class ClientNotFoundException(message: String) : SignalingException(message)
