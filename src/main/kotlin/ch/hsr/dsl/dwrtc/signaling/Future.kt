package ch.hsr.dsl.dwrtc.signaling

import ch.hsr.dsl.dwrtc.util.*
import net.tomp2p.dht.FutureGet
import net.tomp2p.futures.BaseFuture

/**
 * Base class for all our Futures.
 *
 * This mainly decouples our Futures from the TomP2P Futures. This means, users of this class need not import TomP2P.
 *
 * Note: DO have a look at how TomP2P defines [completion][net.tomp2p.futures.BaseFuture.onComplete], [success][net.tomp2p.futures.BaseFuture.onSuccess] and [failure][net.tomp2p.futures.BaseFuture.onFailure] before using this!
 *
 * @property baseFuture base TomP2P BaseFuture
 */
open class Future(private val baseFuture: BaseFuture) {
    /** Await all registered listeners */
    fun await() {
        baseFuture.await()
        baseFuture.awaitListeners()
    }

    /** An operation has completed. Check [net.tomp2p.futures.BaseFuture.onComplete] for the full semantics
     *
     * @param emitter callable, when the operation has completed
     */
    fun onComplete(emitter: () -> Unit) = baseFuture.onComplete { emitter() }

    /** A response has been received. Check [net.tomp2p.futures.BaseFuture.onSuccess] for the full semantics
     *
     * @param emitter callable, when the operation has succeeded
     */
    fun onSuccess(emitter: () -> Unit) = baseFuture.onSuccess(emitter)

    /** The connection has failed. Check [net.tomp2p.futures.BaseFuture.onFailure] for the full semantics
     *
     * @param emitter callable, when the operation has failed
     */
    fun onFailure(emitter: (failedReason: String) -> Unit) = baseFuture.onFailure(emitter)
}

/**
 * Base class for all futures that get something. Get one element.
 *
 * In the subclasses, you will get easy semantics.
 *
 * * `onGet` is called when a successful response has been received (not null)
 * * `onNotFound` is called, when a response has been received, but the answer is null, indicating that the searched key was not found
 *
 * The subclasses differ by these orthogonal concepts:

 * * Do you get one or multiple things? Multiple things have the suffix `All`
 * * In your method, do you transform the response from the DHT before you return it? Transformed responses have the suffix `Custom`
 *   * E.g., you get a `UserId` from the DHT, but you want to return a `User`
 *
 *  @property futureGet base TomP2P FutureGet
 */
open class GetFuture<T>(private val futureGet: FutureGet) : Future(futureGet) {
    /**
     * A single element has been found
     *
     * @param emitter callable, when the data bas been found
     */
    open fun onGet(emitter: (data: T, future: Future) -> Unit) =
            futureGet.onGet { data: T?, future -> if (data != null) emitter(data, future) }

    /**
     * A single element has not been found
     *
     * @param emitter callable, when the data has not been found
     */
    open fun onNotFound(emitter: () -> Unit) = futureGet.onGet { data: T?, _ -> if (data == null) emitter() }
}

/**
 * Get one element and transform it.
 *
 * @property futureGet base TomP2P FutureGet
 * @property transformer callable, to transform the DHT's response type to the method's return type.
 * Note: when the response is null, the transformer is skipped.
 *
 */
open class GetCustomFuture<T, U>(private val futureGet: FutureGet, private val transformer: (U) -> T) :
        GetFuture<T>(futureGet) {
    /** A single element has been found and has been transformed
     *
     * @param emitter callable, when the response has been found
     */
    override fun onGet(emitter: (data: T, future: Future) -> Unit) =
            futureGet.onGetCustom({ data: T?, future -> if (data != null) emitter(data, future) }, transformer)
}

/** Get multiple elements and transform them
 *
 * @property futureGet base TomP2P FutureGet
 * @property transformer callable, to transform the DHT's response type to the method's return type
 * Note: when the response is null, the transformer is skipped (`onNotFound` will be called)
 */
class GetAllCustomFuture<T, U>(private val futureGet: FutureGet, private val transformer: (List<U>) -> T) : GetFuture<T>(futureGet) {
    /** Multiple element have been found and have been transformed
     *
     * @param emitter callable, when the response has been found
     */
    override fun onGet(emitter: (data: T, future: Future) -> Unit) =
            futureGet.onGetAllCustom({ data: T?, future: Future -> if (data != null) emitter(data, future) }, transformer)

    /**
     * Multiple elements have not been found
     *
     * @param emitter callable, when the data has not been found
     */
    override fun onNotFound(emitter: () -> Unit) =
            futureGet.onGetAllCustom({ data: T?, _ -> if (data == null) emitter() }, transformer)
}
