package ch.hsr.dsl.dwrtc.signaling

import net.tomp2p.dht.FutureGet
import net.tomp2p.futures.BaseFuture
import util.*

open class Future(private val baseFuture: BaseFuture) {
	fun await() = Future(baseFuture.awaitListeners())
	fun onComplete(emitter: () -> Unit) = baseFuture.onComplete { emitter() }
	fun onSuccess(emitter: () -> Unit) = baseFuture.onSuccess(emitter)
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
