package ch.hsr.dsl.dwrtc.signaling

import ch.hsr.dsl.dwrtc.util.onFailure
import ch.hsr.dsl.dwrtc.util.onGet
import ch.hsr.dsl.dwrtc.util.onGetAllCustom
import ch.hsr.dsl.dwrtc.util.onSuccess
import net.tomp2p.dht.FutureGet
import net.tomp2p.futures.BaseFuture

open class Future(private val baseFuture: BaseFuture) {
	fun await() = Future(baseFuture.awaitListeners())
	fun onSuccess(emitter: () -> Unit) = baseFuture.onSuccess(emitter)
	fun onFailure(emitter: (failedReason: String) -> Unit) = baseFuture.onFailure(emitter)
}

open class GetFuture<T>(private val futureGet: FutureGet) : Future(futureGet) {
	open fun onGet(emitter: (data: T, future: Future) -> Unit) = futureGet.onGet(emitter)
}

class GetAllCustomFuture<T, U>(private val futureGet: FutureGet, private val transformer: (List<U>) -> T) : GetFuture<T>(futureGet) {
	override fun onGet(emitter: (data: T, future: Future) -> Unit) = futureGet.onGetAllCustom(emitter, transformer)
}
