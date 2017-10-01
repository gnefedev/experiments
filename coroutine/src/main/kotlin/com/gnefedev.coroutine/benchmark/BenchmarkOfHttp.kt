package com.gnefedev.coroutine.benchmark

import com.gnefedev.coroutine.helper.AsyncClientHolder
import com.gnefedev.coroutine.helper.executeAsync
import kotlinx.coroutines.experimental.runBlocking
import org.asynchttpclient.AsyncHttpClient
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Threads
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.OptionsBuilder

//Benchmark                              Mode  Cnt   Score   Error  Units
//BenchmarkOfHttp.asyncReadInCoroutine  thrpt   20  98.375 ± 5.721  ops/s
//BenchmarkOfHttp.syncReadInCoroutine   thrpt   20  90.700 ± 3.713  ops/s
@Fork(1)
@Threads(Threads.MAX)
class BenchmarkOfHttp {
    @Benchmark
    fun syncReadInCoroutine(asyncClientHolder: AsyncClientHolder): String =
            httpGetSync(asyncClientHolder.client)

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
