package com.gnefedev.coroutine.helper

import kotlinx.coroutines.experimental.suspendCancellableCoroutine
import org.asynchttpclient.AsyncCompletionHandler
import org.asynchttpclient.BoundRequestBuilder
import org.asynchttpclient.DefaultAsyncHttpClient
import org.asynchttpclient.Response
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.TearDown

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
