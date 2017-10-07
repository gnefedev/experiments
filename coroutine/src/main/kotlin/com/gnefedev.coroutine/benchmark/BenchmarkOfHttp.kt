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
import org.springframework.web.client.RestTemplate

private val httpClient = RestTemplate()


//Benchmark                              Mode  Cnt    Score   Error  Units
//BenchmarkOfHttp.asyncReadInCoroutine  thrpt   20  304.831 ± 8.281  ops/s
//BenchmarkOfHttp.restTemplate          thrpt   20  302.579 ± 7.834  ops/s
//BenchmarkOfHttp.syncReadInCoroutine   thrpt   20  307.901 ± 8.304  ops/s
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

    @Benchmark
    fun restTemplate(): String = httpClient.getForEntity(
            "http://192.168.0.11:8080/stub/10",
            String::class.java
    ).body

    fun httpGetSync(asyncHttpClient: AsyncHttpClient): String = asyncHttpClient
            .prepareGet("http://192.168.0.11:8080/stub/10")
            .execute()
            .get()
            .responseBody

    suspend fun httpGetAsync(asyncHttpClient: AsyncHttpClient): String = asyncHttpClient
            .prepareGet("http://192.168.0.11:8080/stub/10")
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
