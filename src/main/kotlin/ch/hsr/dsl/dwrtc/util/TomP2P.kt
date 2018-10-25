package util

import ch.hsr.dsl.dwrtc.signaling.Future
import net.tomp2p.dht.FutureGet
import net.tomp2p.futures.BaseFuture
import net.tomp2p.futures.BaseFutureAdapter

fun BaseFuture.onComplete(emitter: (future: BaseFuture) -> Unit) {
    this.addListener(object : BaseFutureAdapter<BaseFuture>() {
        override fun operationComplete(future: BaseFuture) {
            emitter(future)
        }
    })
}

fun BaseFuture.onSuccess(emitter: () -> Unit) {
    this.onComplete { result -> if (result.isSuccess) emitter() }
}

fun BaseFuture.onFailure(emitter: (failedReason: String) -> Unit) {
    this.onComplete { result -> if (result.isFailed) emitter(result.failedReason()) }
}

@Suppress("UNCHECKED_CAST")
fun <T, U> FutureGet.onGetCustom(emitter: (data: U?, future: Future) -> Unit, transformer: (T) -> U) {
    this.addListener(object : BaseFutureAdapter<FutureGet>() {
        override fun operationComplete(future: FutureGet) {
            val data = transformer(future.data()?.`object`() as T)
            emitter(data, Future(future))
        }
    })
}

fun <T> FutureGet.onGet(emitter: (data: T?, future: Future) -> Unit) = this.onGetCustom<T, T>(emitter, { it })

@Suppress("UNCHECKED_CAST")
fun <T, U> FutureGet.onGetAllCustom(emitter: (data: U?, future: Future) -> Unit, transformer: (List<T>) -> U) {
    this.addListener(object : BaseFutureAdapter<FutureGet>() {
        override fun operationComplete(future: FutureGet) {
            val list = future.dataMap().values.map { it.`object`() as T }
            emitter(transformer(list), Future(future))
        }
    })
}

fun <T> FutureGet.onGetAll(emitter: (data: List<T>?, future: Future) -> Unit) {
    this.onGetAllCustom<T, List<T>>(emitter, { it })
}
