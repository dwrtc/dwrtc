package ch.hsr.dsl.dwrtc.util

import ch.hsr.dsl.dwrtc.signaling.Future
import net.tomp2p.dht.FutureGet
import net.tomp2p.futures.BaseFuture
import net.tomp2p.futures.BaseFutureAdapter

/**
 * Raised whenever an operation has been completed. It's not yet defined, if the operation has been successful.
 *
 * @param emitter callable, when the operation has been completed
 */
fun BaseFuture.onComplete(emitter: (future: BaseFuture) -> Unit) {
    this.addListener(object : BaseFutureAdapter<BaseFuture>() {
        override fun operationComplete(future: BaseFuture) {
            emitter(future)
        }
    })
}

/**
 * Raised whenever an operation is successful
 *
 * TomP2P defines success, such that *a response was received*. Therefore, *empty responses are considered a success*
 *
 * @param emitter callable, when the operation has succeeded
 */
fun BaseFuture.onSuccess(emitter: () -> Unit) {
    this.onComplete { result -> if (result.isSuccess) emitter() }
}

/**
 * Raised whenever an operation has failed
 *
 * TomP2P defines failure, such that *there's a connection problem*.
 * Therefore, this only fires in cases where the DHT connection
 *
 * @param emitter callable, when the operation has failed
 */
fun BaseFuture.onFailure(emitter: (failedReason: String) -> Unit) {
    this.onComplete { result -> if (result.isFailed) emitter(result.failedReason()) }
}

/**
 * A response has been received (for one element) and you need to transform it, before returning.
 *
 * @param emitter callable, when the response has been received and transformed
 * @param transformer callable, to transform the DHT's response type to the method's return type.
 * Note: when the response is `null`, the transformer is skipped, and returns `null`
 */
@Suppress("UNCHECKED_CAST")
fun <T, U> FutureGet.onGetCustom(emitter: (data: U?, future: Future) -> Unit, transformer: (T) -> U) {
    this.addListener(object : BaseFutureAdapter<FutureGet>() {
        override fun operationComplete(future: FutureGet) {
            val data = future.data()?.`object`()
            emitter(data?.let { transformer(it as T) }, Future(future))
        }
    })
}

/**
 * A response has been received (for one element).
 *
 * @param emitter callable, when the response has been received
 */
fun <T> FutureGet.onGet(emitter: (data: T?, future: Future) -> Unit) = this.onGetCustom<T, T>(emitter, { it })

/**
 * A response has been received (for multiple elements) and you need to transform them, before returning.
 *
 * @param emitter callable, when the response has been received and transformed
 * @param transformer callable, to transform the DHT's response type to the method's return type.
 * Note: when the response is `null`, the transformer is skipped, and returns `null`
 */
@Suppress("UNCHECKED_CAST")
fun <T, U> FutureGet.onGetAllCustom(emitter: (data: U?, future: Future) -> Unit, transformer: (List<T>) -> U) {
    this.addListener(object : BaseFutureAdapter<FutureGet>() {
        override fun operationComplete(future: FutureGet) {
            val list: List<T>? = future.dataMap()?.values?.map { it.`object`() as T }
            emitter(list?.let { transformer(it) }, Future(future))
        }
    })
}

/**
 * A response has been received (for multiple elements).
 *
 * @param emitter callable, when the response has been received
 */
fun <T> FutureGet.onGetAll(emitter: (data: List<T>?, future: Future) -> Unit) {
    this.onGetAllCustom<T, List<T>>(emitter, { it })
}
