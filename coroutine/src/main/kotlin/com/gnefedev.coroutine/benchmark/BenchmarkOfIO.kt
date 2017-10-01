package com.gnefedev.coroutine.benchmark

import com.gnefedev.coroutine.helper.FileHolder
import kotlinx.coroutines.experimental.runBlocking
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Threads
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.OptionsBuilder
import java.util.concurrent.Callable
import java.util.concurrent.ForkJoinPool

//Benchmark                            Mode  Cnt      Score     Error  Units
//BenchmarkOfIO.asyncReadInCoroutine  thrpt   20  16910.358 ±  95.522  ops/s
//BenchmarkOfIO.syncRead              thrpt   20  16875.323 ±  88.500  ops/s
//BenchmarkOfIO.syncReadInFuture      thrpt   20  16240.730 ± 128.356  ops/s
@Fork(1)
@Threads(Threads.MAX)
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
