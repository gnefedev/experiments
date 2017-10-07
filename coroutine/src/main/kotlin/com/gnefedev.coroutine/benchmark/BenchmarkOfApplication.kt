package com.gnefedev.coroutine.benchmark

import com.gnefedev.coroutine.helper.RandomHolder
import com.gnefedev.coroutine.peopleCount
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.OptionsBuilder
import org.springframework.web.client.RestTemplate
import kotlin.math.absoluteValue

private val httpClient = RestTemplate()

//4 core
//Benchmark                      Mode  Cnt    Score    Error  Units
//BenchmarkOfApplication.async  thrpt   20  151.128 ±  8.315  ops/s
//BenchmarkOfApplication.sync   thrpt   20  157.127 ± 10.344  ops/s
//8 core
//Benchmark                            Mode  Cnt   Score   Error  Units
//BenchmarkOfApplication.async        thrpt   20  25.626 ± 0.757  ops/s
//BenchmarkOfApplication.sync         thrpt   20  25.739 ± 0.417  ops/s
//Benchmark                          Mode  Cnt   Score   Error  Units
//BenchmarkOfApplication.httpAsync  thrpt   20  21.010 ± 0.350  ops/s
//BenchmarkOfApplication.httpSync   thrpt   20  20.493 ± 0.215  ops/s
//BenchmarkOfApplication.httpAsync   avgt   20   0.193 ± 0.005   s/op
//BenchmarkOfApplication.httpSync    avgt   20   0.198 ± 0.002   s/op
@Fork(1)
@BenchmarkMode(Mode.AverageTime, Mode.Throughput)
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
    fun httpSync() = httpClient.getForEntity(
            "http://192.168.0.11:8080/http/sync/100/1",
            String::class.java
    )
    @Benchmark
    fun httpAsync() = httpClient.getForEntity(
            "http://192.168.0.11:8080/http/async/100/1",
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
