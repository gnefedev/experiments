package com.gnefedev.coroutine.benchmark

import com.gnefedev.coroutine.helper.RandomHolder
import com.gnefedev.coroutine.peopleCount
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Threads
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.OptionsBuilder
import org.springframework.web.client.RestTemplate
import kotlin.math.absoluteValue

val httpClient = RestTemplate()

//4 core
//Benchmark                      Mode  Cnt    Score    Error  Units
//BenchmarkOfApplication.async  thrpt   20  151.128 ±  8.315  ops/s
//BenchmarkOfApplication.sync   thrpt   20  157.127 ± 10.344  ops/s
//8 core
//Benchmark                            Mode  Cnt   Score   Error  Units
//BenchmarkOfApplication.async        thrpt   20  25.735 ± 0.450  ops/s
//BenchmarkOfApplication.springAsync  thrpt   20  25.626 ± 0.757  ops/s
//BenchmarkOfApplication.sync         thrpt   20  25.739 ± 0.417  ops/s
@Fork(1)
@Threads(Threads.MAX)
class BenchmarkOfApplication {
    @Benchmark
    fun sync(randomHolder: RandomHolder): String = httpClient.getForEntity(
            "http://localhost:8080/sync/${randomHolder.random.nextInt(peopleCount).absoluteValue}",
            String::class.java
    ).body

    @Benchmark
    fun async(randomHolder: RandomHolder): String = httpClient.getForEntity(
            "http://localhost:8080/async/${randomHolder.random.nextInt(peopleCount).absoluteValue}",
            String::class.java
    ).body

    @Benchmark
    fun springAsync(randomHolder: RandomHolder): String = httpClient.getForEntity(
            "http://localhost:8080/springAsync/${randomHolder.random.nextInt(peopleCount).absoluteValue}",
            String::class.java
    ).body
}

fun main(args: Array<String>) {
    val options = OptionsBuilder()
            .include(BenchmarkOfApplication::class.java.simpleName)
            .jvmArgs("-Xmx2g")
            .build()
    Runner(options).run()
}
