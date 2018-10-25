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
 */
open class Future(private val baseFuture: BaseFuture) {
    /** Await all registered listeners */
    fun await() = Future(baseFuture.awaitListeners())

    /** An operation has completed. Check [net.tomp2p.futures.BaseFuture.onComplete] for the full semantics */
    fun onComplete(emitter: () -> Unit) = baseFuture.onComplete { emitter() }

    /** A response has been received. Check [net.tomp2p.futures.BaseFuture.onSuccess] for the full semantics */
    fun onSuccess(emitter: () -> Unit) = baseFuture.onSuccess(emitter)

    /** The connection has failed. Check [net.tomp2p.futures.BaseFuture.onFailure] for the full semantics */
    fun onFailure(emitter: (failedReason: String) -> Unit) = baseFuture.onFailure(emitter)
}

open class GetFuture<T>(private val futureGet: FutureGet) : Future(futureGet) {
    open fun onGet(emitter: (data: T, future: Future) -> Unit) =
            futureGet.onGet { data: T?, future -> if (data != null) emitter(data, future) }

    open fun onNotFound(emitter: () -> Unit) = futureGet.onGet { data: T?, _ -> if (data == null) emitter() }
}

open class GetCustomFuture<T, U>(private val futureGet: FutureGet, private val transformer: (U) -> T) :
        GetFuture<T>(futureGet) {
    override fun onGet(emitter: (data: T, future: Future) -> Unit) =
            futureGet.onGetCustom({ data: T?, future -> if (data != null) emitter(data, future) }, transformer)
}

class GetAllCustomFuture<T, U>(private val futureGet: FutureGet, private val transformer: (List<U>) -> T) : GetFuture<T>(futureGet) {
    override fun onGet(emitter: (data: T, future: Future) -> Unit) =
            futureGet.onGetAllCustom({ data: T?, future: Future -> if (data != null) emitter(data, future) }, transformer)

    override fun onNotFound(emitter: () -> Unit) =
            futureGet.onGetAllCustom({ data: T?, _ -> if (data == null) emitter() }, transformer)
}
