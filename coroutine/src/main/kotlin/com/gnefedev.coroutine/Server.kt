package com.gnefedev.coroutine

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.ForkJoinPool

object DoInFuture {
    private val executor: ExecutorService = ForkJoinPool.commonPool()

    fun collectStrings(count: Int, work: () -> String) = collect(count, "", String::plus, work)

    private fun <R, T> collect(count: Int, initVal: R, combine: (R, T) -> R, work: () -> T): R {
        val tasks: MutableList<Callable<T>> = (0..count)
                .map { Callable { work.invoke() } }
                .toMutableList()
        return executor.invokeAll(tasks)
                .map { it.get()!! }
                .fold(initVal, combine)
    }
}

object DoInCoroutine {
    suspend fun collectStrings(count: Int, work: suspend () -> String) = collect(count, "", String::plus, work)

    private suspend fun <R, T> collect(count: Int, initVal: R, combine: (R, T) -> R, work: suspend () -> T) =
            (0..count)
                    .map {
                        async(CommonPool) {
                            work.invoke()
                        }
                    }
                    .map { it.await() }
                    .fold(initVal, combine)
}