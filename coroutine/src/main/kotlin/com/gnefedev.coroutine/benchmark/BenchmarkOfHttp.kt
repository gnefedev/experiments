package com.gnefedev.coroutine.benchmark

import com.gnefedev.coroutine.helper.AsyncClientHolder
import com.gnefedev.coroutine.helper.executeAsync
import kotlinx.coroutines.experimental.runBlocking
import org.asynchttpclient.AsyncHttpClient
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.OptionsBuilder


@Fork(1)
@Threads(Threads.MAX)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
class BenchmarkOfHttp {
    @Benchmark
    fun syncReadInCoroutine(asyncClientHolder: AsyncClientHolder): String = runBlocking {
        httpGetSync(asyncClientHolder.client)
    }

    @Benchmark
    fun asyncReadInCoroutine(asyncClientHolder: AsyncClientHolder): String = runBlocking {
        httpGetAsync(asyncClientHolder.client)
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

fun main(args: Array<String>) {
    val options = OptionsBuilder()
            .include(BenchmarkOfHttp::class.java.simpleName)
            .jvmArgs("-Xmx2g")
            .build()
    Runner(options).run()
}
