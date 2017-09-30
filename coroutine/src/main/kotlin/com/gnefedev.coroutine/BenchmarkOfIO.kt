package com.gnefedev.coroutine

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.OptionsBuilder
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ForkJoinPool

@Fork(1)
@Threads(Threads.MAX)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
class BenchmarkOfIO {
    @Benchmark
    fun syncRead(fileHolder: FileHolder) = fileHolder.readFileSync()

    @Benchmark
    fun syncReadInFuture(fileHolder: FileHolder): String = ForkJoinPool.commonPool()
            .submit(
                    Callable<String> {
                        fileHolder.readFileSync()
                    }
            ).get()

    @Benchmark
    fun asyncReadInCoroutine(fileHolder: FileHolder): String = runBlocking {
        fileHolder.readFileAsync()
    }
}

fun main(args: Array<String>) {
    val options = OptionsBuilder()
            .include(BenchmarkOfIO::class.java.simpleName)
            .jvmArgs("-Xmx2g")
            .build()
    Runner(options).run()
}
