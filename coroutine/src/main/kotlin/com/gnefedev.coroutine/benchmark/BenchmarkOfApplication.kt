package com.gnefedev.coroutine.benchmark

import com.gnefedev.coroutine.helper.RandomHolder
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Threads
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.OptionsBuilder
import org.springframework.web.client.RestTemplate
import kotlin.math.absoluteValue

val httpClient = RestTemplate()

//Benchmark                      Mode  Cnt    Score    Error  Units
//BenchmarkOfApplication.async  thrpt   20  151.128 ±  8.315  ops/s
//BenchmarkOfApplication.sync   thrpt   20  157.127 ± 10.344  ops/s
@Fork(1)
@Threads(Threads.MAX)
class BenchmarkOfApplication {
    @Benchmark
    fun sync(randomHolder: RandomHolder) = httpClient.getForEntity(
            "http://localhost:8080/sync/${randomHolder.random.nextInt(10_000).absoluteValue}",
            String::class.java
    )
    @Benchmark
    fun async(randomHolder: RandomHolder) = httpClient.getForEntity(
            "http://localhost:8080/async/${randomHolder.random.nextInt(10_000).absoluteValue}",
            String::class.java
    )
}

fun main(args: Array<String>) {
    val options = OptionsBuilder()
            .include(BenchmarkOfApplication::class.java.simpleName)
            .jvmArgs("-Xmx2g")
            .build()
    Runner(options).run()
}
