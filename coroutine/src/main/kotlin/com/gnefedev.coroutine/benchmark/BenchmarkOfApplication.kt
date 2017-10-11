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
//1 Thread - 0.5core
//Benchmark                                Mode  Cnt    Score   Error  Units
//BenchmarkOfApplication.httpBatchAsyncV1  avgt   20   10.652 ± 0.334  ms/op
//BenchmarkOfApplication.httpBatchAsyncV2  avgt   20   10.066 ± 0.214  ms/op
//BenchmarkOfApplication.httpBatchSync     avgt   20  163.857 ± 2.391  ms/op
//BenchmarkOfApplication.httpSerialAsync   avgt   20  154.114 ± 0.968  ms/op
//BenchmarkOfApplication.httpSerialSync    avgt   20  160.265 ± 1.840  ms/op
//BenchmarkOfApplication.justGet           avgt   20    0.149 ± 0.008  ms/op
//BenchmarkOfApplication.withDelay         avgt   20    1.608 ± 0.010  ms/op
//6 Threads - 0.5core
//Benchmark                                Mode  Cnt    Score    Error  Units
//BenchmarkOfApplication.httpBatchAsyncV1  avgt   20   79.313 ±  3.707  ms/op
//BenchmarkOfApplication.httpBatchAsyncV2  avgt   20   83.932 ±  2.887  ms/op
//BenchmarkOfApplication.httpBatchSync     avgt   20  984.777 ± 34.177  ms/op
//BenchmarkOfApplication.httpSerialAsync   avgt   20  146.310 ±  2.453  ms/op
//BenchmarkOfApplication.httpSerialSync    avgt   20  146.832 ±  2.539  ms/op
//BenchmarkOfApplication.justGet           avgt   20    0.515 ±  0.013  ms/op
//BenchmarkOfApplication.withDelay         avgt   20    1.558 ±  0.024  ms/op
//1 Threads - 2 core
//Benchmark                                Mode  Cnt    Score   Error  Units
//BenchmarkOfApplication.httpBatchAsyncV1  avgt   20   15.401 ± 0.174  ms/op
//BenchmarkOfApplication.httpBatchAsyncV2  avgt   20   15.351 ± 0.131  ms/op
//BenchmarkOfApplication.httpBatchSync     avgt   20   57.607 ± 0.452  ms/op
//BenchmarkOfApplication.httpSerialAsync   avgt   20  156.339 ± 0.679  ms/op
//BenchmarkOfApplication.httpSerialSync    avgt   20  159.264 ± 1.018  ms/op
//BenchmarkOfApplication.justGet           avgt   20    0.160 ± 0.005  ms/op
//BenchmarkOfApplication.withDelay         avgt   20    1.633 ± 0.014  ms/op
//6 Threads - 2 core
//Benchmark                                Mode  Cnt    Score    Error  Units
//BenchmarkOfApplication.httpBatchAsyncV1  avgt   20   86.723 ±  3.679  ms/op
//BenchmarkOfApplication.httpBatchAsyncV2  avgt   20   91.679 ±  2.679  ms/op
//BenchmarkOfApplication.httpBatchSync     avgt   20  343.850 ± 17.213  ms/op
//BenchmarkOfApplication.httpSerialAsync   avgt   20  143.800 ±  1.884  ms/op
//BenchmarkOfApplication.httpSerialSync    avgt   20  151.250 ±  1.554  ms/op
//BenchmarkOfApplication.justGet           avgt   20    0.406 ±  0.008  ms/op
//BenchmarkOfApplication.withDelay         avgt   20    1.557 ±  0.016  ms/op
//1 Thread - 4 core
//Benchmark                                Mode  Cnt    Score   Error  Units
//BenchmarkOfApplication.httpBatchAsyncV1  avgt   20   14.770 ± 0.285  ms/op
//BenchmarkOfApplication.httpBatchAsyncV2  avgt   20   15.099 ± 0.197  ms/op
//BenchmarkOfApplication.httpBatchSync     avgt   20   25.737 ± 0.222  ms/op
//BenchmarkOfApplication.httpSerialAsync   avgt   20  157.416 ± 1.253  ms/op
//BenchmarkOfApplication.httpSerialSync    avgt   20  159.017 ± 1.141  ms/op
//BenchmarkOfApplication.justGet           avgt   20    0.172 ± 0.006  ms/op
//BenchmarkOfApplication.withDelay         avgt   20    1.625 ± 0.012  ms/op
//6 Threads - 4 core
//Benchmark                                Mode  Cnt    Score   Error  Units
//BenchmarkOfApplication.httpBatchAsyncV1  avgt   20   81.705 ± 4.814  ms/op
//BenchmarkOfApplication.httpBatchAsyncV2  avgt   20   82.966 ± 3.069  ms/op
//BenchmarkOfApplication.httpBatchSync     avgt   20  135.011 ± 2.990  ms/op
//BenchmarkOfApplication.httpSerialAsync   avgt   20  144.742 ± 1.213  ms/op
//BenchmarkOfApplication.httpSerialSync    avgt   20  152.276 ± 1.494  ms/op
//BenchmarkOfApplication.justGet           avgt   20    0.286 ± 0.010  ms/op
//BenchmarkOfApplication.withDelay         avgt   20    1.558 ± 0.020  ms/op
@Fork(1)
@Threads(6)
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
    fun justGet(): String = httpClient.getForEntity(
            "https://www.ok.ru/favicon.ico",
            String::class.java
    ).body

    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Benchmark
    fun httpSerialSync(): String = httpClient.getForEntity(
            "http://localhost:8080/http/serial/sync/20",
            String::class.java
    ).body

    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Benchmark
    fun httpSerialAsync(): String = httpClient.getForEntity(
            "http://localhost:8080/http/serial/async/20",
            String::class.java
    ).body

    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Benchmark
    fun httpBatchSync(): String = httpClient.getForEntity(
            "http://localhost:8080/http/batch/sync/20",
            String::class.java
    ).body

    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Benchmark
    fun httpBatchAsyncV1(): String = httpClient.getForEntity(
            "http://localhost:8080/http/batch/async_v1/20",
            String::class.java
    ).body

    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Benchmark
    fun httpBatchAsyncV2(): String = httpClient.getForEntity(
            "http://localhost:8080/http/batch/async_v2/20",
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
