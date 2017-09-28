package com.gnefedev.coroutine

import kotlinx.coroutines.experimental.runBlocking
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Warmup
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.OptionsBuilder
import java.math.BigDecimal
import java.util.*

@Fork(1)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
class BenchmarkOfIO {
    @Benchmark
    fun syncReadInFuture(fileHolder: FileHolder): String {
        return DoInFuture.collectStrings(TASK_COUNT) {
            fileHolder.readFileSync()
        }
    }

    @Benchmark
    fun syncReadInCoroutine(fileHolder: FileHolder): String = runBlocking {
        DoInCoroutine.collectStrings(TASK_COUNT) {
            fileHolder.readFileSync()
        }
    }

    @Benchmark
    fun asyncReadInCoroutine(fileHolder: FileHolder): String = runBlocking {
        DoInCoroutine.collectStrings(TASK_COUNT) {
            fileHolder.readFileAsync()
        }
    }

    @Benchmark
    fun doWorkInCoroutine(fileHolder: FileHolder, randomHolder: RandomHolder): String = runBlocking {
        DoInCoroutine.collectStrings(TASK_COUNT) {
            gcLoadWork(randomHolder.random)
        }
    }

    @Benchmark
    fun asyncReadAndDoWorkInCoroutine(fileHolder: FileHolder, randomHolder: RandomHolder): String = runBlocking {
        DoInCoroutine.collectStrings(TASK_COUNT) {
            fileHolder.readFileAsync() + gcLoadWork(randomHolder.random)
        }
    }

    @Benchmark
    fun syncReadAndDoWorkInFuture(fileHolder: FileHolder, randomHolder: RandomHolder): String {
        return DoInFuture.collectStrings(TASK_COUNT) {
            fileHolder.readFileSync() + gcLoadWork(randomHolder.random)
        }
    }

    private fun gcLoadWork(random: Random): String {
        var result = 0.0
        repeat(5_000) {
            result *= BigDecimal(random.nextDouble()).hashCode()
        }
        return result.toString()
    }

}

fun main(args: Array<String>) {
    var count = 100
//    while (count <= Runtime.getRuntime().availableProcessors() * 2) {
    val options = OptionsBuilder()
            .include(BenchmarkOfIO::class.java.simpleName)
            .threads(8)
            .jvmArgs("-Xmx2g", "-Dcount=$count")
            .build()
    Runner(options).run()
    count *= 2
//    }
}