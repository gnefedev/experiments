package com.gnefedev.coroutine.benchmark

import com.gnefedev.coroutine.helper.RandomHolder
import com.gnefedev.coroutine.peopleCount
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.OptionsBuilder
import org.springframework.web.client.RestTemplate
import java.util.concurrent.TimeUnit
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
//1 Thread
//Benchmark                                Mode  Cnt    Score   Error  Units
//BenchmarkOfApplication.httpBatchAsyncV1  avgt   20   10.652 ± 0.334  ms/op
//BenchmarkOfApplication.httpBatchAsyncV2  avgt   20   10.066 ± 0.214  ms/op
//BenchmarkOfApplication.httpBatchSync     avgt   20  163.857 ± 2.391  ms/op
//BenchmarkOfApplication.httpSerialAsync   avgt   20  154.114 ± 0.968  ms/op
//BenchmarkOfApplication.httpSerialSync    avgt   20  160.265 ± 1.840  ms/op
//BenchmarkOfApplication.justGet           avgt   20    0.149 ± 0.008  ms/op
//BenchmarkOfApplication.withDelay         avgt   20    1.608 ± 0.010  ms/op
//10 Threads
//Benchmark                                Mode  Cnt     Score     Error  Units
//BenchmarkOfApplication.httpBatchAsyncV1  avgt   20    83.995 ±   4.421  ms/op
//BenchmarkOfApplication.httpBatchAsyncV2  avgt   20    85.130 ±   3.345  ms/op
//BenchmarkOfApplication.httpBatchSync     avgt   20  1292.494 ± 166.473  ms/op
//BenchmarkOfApplication.httpSerialAsync   avgt   20   154.243 ±   3.218  ms/op
//BenchmarkOfApplication.httpSerialSync    avgt   20   161.975 ±   2.855  ms/op
//BenchmarkOfApplication.justGet           avgt   20     0.805 ±   0.015  ms/op
//BenchmarkOfApplication.withDelay         avgt   20     1.572 ±   0.024  ms/op
@Fork(1)
@Threads(1)
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

    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Benchmark
    fun withDelay(): String = httpClient.getForEntity(
            "http://localhost:8081/stub/1",
            String::class.java
    ).body

    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Benchmark
    fun justGet(): String = httpClient.getForEntity(
            "http://localhost:8080/nothingToDo",
            String::class.java
    ).body

    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Benchmark
    fun httpSerialSync(): String = httpClient.getForEntity(
            "http://localhost:8080/http/serial/sync/100/1",
            String::class.java
    ).body

    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Benchmark
    fun httpSerialAsync(): String = httpClient.getForEntity(
            "http://localhost:8080/http/serial/async/100/1",
            String::class.java
    ).body

    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Benchmark
    fun httpBatchSync(): String = httpClient.getForEntity(
            "http://localhost:8080/http/batch/sync/100/1",
            String::class.java
    ).body

    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Benchmark
    fun httpBatchAsyncV1(): String = httpClient.getForEntity(
            "http://localhost:8080/http/batch/async_v1/100/1",
            String::class.java
    ).body

    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Benchmark
    fun httpBatchAsyncV2(): String = httpClient.getForEntity(
            "http://localhost:8080/http/batch/async_v2/100/1",
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
