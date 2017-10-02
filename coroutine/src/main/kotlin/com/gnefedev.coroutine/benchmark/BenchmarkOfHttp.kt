package com.gnefedev.coroutine.benchmark

import com.gnefedev.coroutine.helper.AsyncClientHolder
import com.gnefedev.coroutine.helper.executeAsync
import kotlinx.coroutines.experimental.runBlocking
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Threads
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.OptionsBuilder
import org.springframework.web.client.RestTemplate

val restTemplate = RestTemplate()

//4-core
//Benchmark                              Mode  Cnt   Score   Error  Units
//BenchmarkOfHttp.asyncReadInCoroutine  thrpt   20  98.375 ± 5.721  ops/s
//BenchmarkOfHttp.syncReadInCoroutine   thrpt   20  90.700 ± 3.713  ops/s
//8-core
//Benchmark                              Mode  Cnt    Score   Error  Units
//BenchmarkOfHttp.asyncReadInCoroutine  thrpt   20  421.974 ± 7.866  ops/s
//BenchmarkOfHttp.syncReadInCoroutine   thrpt   20  410.956 ± 5.413  ops/s
@Fork(1)
@Threads(Threads.MAX)
class BenchmarkOfHttp {
    @Benchmark
    fun syncReadInCoroutine(asyncClientHolder: AsyncClientHolder): String =
            asyncClientHolder.client
                    .prepareGet("http://www.ya.ru/")
                    .execute()
                    .get()
                    .responseBody

    @Benchmark
    fun asyncReadInCoroutine(asyncClientHolder: AsyncClientHolder): String = runBlocking {
        asyncClientHolder.client
                .prepareGet("http://www.ya.ru/")
                .executeAsync()
                .responseBody
    }
}

fun main(args: Array<String>) {
    val options = OptionsBuilder()
            .include(BenchmarkOfHttp::class.java.simpleName)
            .jvmArgs("-Xmx2g")
            .build()
    Runner(options).run()
}
