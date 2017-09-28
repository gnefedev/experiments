package com.gnefedev.coroutine

import kotlinx.coroutines.experimental.runBlocking
import kotlinx.coroutines.experimental.suspendCancellableCoroutine
import org.asynchttpclient.AsyncCompletionHandler
import org.asynchttpclient.AsyncHttpClient
import org.asynchttpclient.BoundRequestBuilder
import org.asynchttpclient.Response
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Warmup
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.OptionsBuilder
import java.math.BigDecimal
import java.util.*


@Fork(1)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
class BenchmarkOfHttp {
    @Benchmark
    fun syncReadInFuture(asyncClientHolder: AsyncClientHolder): String {
        return DoInFuture.collectStrings(TASK_COUNT) {
            httpGetSync(asyncClientHolder.client)
        }
    }

    @Benchmark
    fun asyncReadFileInCoroutine(fileHolder: FileHolder): String = runBlocking {
        DoInCoroutine.collectStrings(TASK_COUNT) {
            fileHolder.readFileAsync()
        }
    }

//    @Benchmark
    fun syncReadInCoroutine(asyncClientHolder: AsyncClientHolder): String = runBlocking {
        DoInCoroutine.collectStrings(TASK_COUNT) {
            httpGetSync(asyncClientHolder.client)
        }
    }

    @Benchmark
    fun asyncReadInCoroutine(asyncClientHolder: AsyncClientHolder): String = runBlocking {
        DoInCoroutine.collectStrings(TASK_COUNT) {
            httpGetAsync(asyncClientHolder.client)
        }
    }

    @Benchmark
    fun doWorkInCoroutine(asyncClientHolder: AsyncClientHolder, randomHolder: RandomHolder): String = runBlocking {
        DoInCoroutine.collectStrings(TASK_COUNT) {
            gcLoadWork(randomHolder.random)
        }
    }

    @Benchmark
    fun asyncReadAndDoWorkInCoroutine(fileHolder: FileHolder, asyncClientHolder: AsyncClientHolder, randomHolder: RandomHolder): String = runBlocking {
        DoInCoroutine.collectStrings(TASK_COUNT) {
            fileHolder.readFileAsync() + httpGetAsync(asyncClientHolder.client) + gcLoadWork(randomHolder.random)
        }
    }

    @Benchmark
    fun syncReadAndDoWorkInFuture(fileHolder: FileHolder, asyncClientHolder: AsyncClientHolder, randomHolder: RandomHolder): String {
        return DoInFuture.collectStrings(TASK_COUNT) {
            fileHolder.readFileSync() + httpGetSync(asyncClientHolder.client) + gcLoadWork(randomHolder.random)
        }
    }

    private fun gcLoadWork(random: Random): String {
        var result = 0.0
        repeat(2_000) {
            result *= BigDecimal(random.nextDouble()).hashCode()
        }
        return result.toString()
    }

    fun httpGetSync(asyncHttpClient: AsyncHttpClient): String = asyncHttpClient
            .prepareGet("http://www.ya.ru/")
            .execute()
            .get()
            .responseBody

    suspend fun httpGetAsync(asyncHttpClient: AsyncHttpClient): String = asyncHttpClient
            .prepareGet("http://www.ya.ru/")
            .executeAsync()
            .responseBody
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


fun main(args: Array<String>) {
    var count = 1
//    while (count <= Runtime.getRuntime().availableProcessors() * 2) {
    val options = OptionsBuilder()
            .include(BenchmarkOfHttp::class.java.simpleName)
            .threads(16)
            .jvmArgs("-Xmx2g", "-Dcount=$count")
            .build()
    Runner(options).run()
    count *= 2
//    }
}