package ch.hsr.dsl.dwrtc.util

import net.tomp2p.futures.BaseFuture
import net.tomp2p.futures.BaseFutureAdapter
import net.tomp2p.futures.FutureDone

fun BaseFuture.onComplete(emitter: (future: FutureDone<Void>) -> Unit) {
    this.addListener(object : BaseFutureAdapter<FutureDone<Void>>() {
        override fun operationComplete(future: FutureDone<Void>) {
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
