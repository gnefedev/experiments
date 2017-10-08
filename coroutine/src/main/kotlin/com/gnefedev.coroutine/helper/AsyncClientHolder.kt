package com.gnefedev.coroutine.helper

import kotlinx.coroutines.experimental.AbstractCoroutine
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.selects.SelectInstance
import kotlinx.coroutines.experimental.suspendCancellableCoroutine
import org.asynchttpclient.AsyncCompletionHandler
import org.asynchttpclient.BoundRequestBuilder
import org.asynchttpclient.DefaultAsyncHttpClient
import org.asynchttpclient.Response
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.TearDown
import kotlin.coroutines.experimental.CoroutineContext

@State(Scope.Benchmark)
class AsyncClientHolder {
    var client = DefaultAsyncHttpClient()

    @TearDown(Level.Trial)
    fun tearDown() {
        client.close()
    }
}

suspend fun BoundRequestBuilder.executeAsync() = suspendCancellableCoroutine<Response> { cont ->
    execute(object : AsyncCompletionHandler<Response>() {
        override fun onCompleted(response: Response): Response {
            cont.resume(response)
            return response
        }

        override fun onThrowable(t: Throwable) {
            cont.resumeWithException(t)
        }
    })
}

fun BoundRequestBuilder.future(coroutineContext: CoroutineContext): Deferred<Response> {
    val deferred = DeferredCoroutine<Response>(coroutineContext, true)
    execute(object : AsyncCompletionHandler<Response>() {
        override fun onCompleted(response: Response): Response {
            deferred.resume(response)
            return response
        }

        override fun onThrowable(t: Throwable) {
            deferred.resumeWithException(t)
        }
    })
    return deferred
}


@Suppress("UNCHECKED_CAST")
private open class DeferredCoroutine<T>(
        parentContext: CoroutineContext,
        active: Boolean
) : AbstractCoroutine<T>(parentContext, active), Deferred<T> {
    override fun getCompleted(): T = getCompletedInternal() as T
    suspend override fun await(): T = awaitInternal() as T
    override fun <R> registerSelectAwait(select: SelectInstance<R>, block: suspend (T) -> R) =
            registerSelectAwaitInternal(select, block as (suspend (Any?) -> R))
}
