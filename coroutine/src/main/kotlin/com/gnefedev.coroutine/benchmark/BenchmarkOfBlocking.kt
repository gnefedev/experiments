package com.gnefedev.coroutine.benchmark

import com.gnefedev.coroutine.helper.FileHolder
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

private val random = ThreadLocal.withInitial { Random() }

private val availableProcessors = Runtime.getRuntime().availableProcessors()
private val goal = availableProcessors * 400

private val warmUpIterations = 10
private val measurementsIterations = 30
private val threeOfForPartsIsIo = (1..goal * 3).map { random.get().nextInt(4) != 1 }
private val halfPartIsIo = (1..goal * 8).map { random.get().nextInt(2) == 1 }
private val fourthPartIsIo = (1..goal * 32).map { random.get().nextInt(4) == 1 }
private val eightPartIsIo = (1..goal * 64).map { random.get().nextInt(8) == 1 }

//Tree of For parts is io:
//Blocked: time: 117 ms, calculations: 497, calculations per ms: 4.244235695986337
//Not blocked: time: 116 ms, calculations: 584, calculations per ms: 5.039919586444572
//time difference: 0.991175633361799
//calculations per ms difference: 0.8421236932830602
//Half part is io:
//Blocked: time: 128 ms, calculations: 1522, calculations per ms: 11.89198334200937
//Not blocked: time: 127 ms, calculations: 1652, calculations per ms: 12.948537095088819
//time difference: 0.9963560645497137
//calculations per ms difference: 0.9184036200135548
//Fourth part is io:
//Blocked: time: 163 ms, calculations: 4958, calculations per ms: 30.392317123007764
//Not blocked: time: 164 ms, calculations: 4960, calculations per ms: 30.233238520926452
//time difference: 1.0057212913771965
//calculations per ms difference: 1.0052617122697989
//Eight part is io:
//Blocked: time: 230 ms, calculations: 11381, calculations per ms: 49.285508083140876
//Not blocked: time: 238 ms, calculations: 11495, calculations per ms: 48.164106145251395
//time difference: 1.033487297921478
//calculations per ms difference: 1.0232829388446991
fun main(args: Array<String>) {
    repeat(warmUpIterations) {
        blocked(halfPartIsIo)
    }
    repeat(warmUpIterations) {
        notBlocked(halfPartIsIo)
    }

    println("Tree of For parts is io:")
    benchmark(threeOfForPartsIsIo)
    println("Half part is io:")
    benchmark(halfPartIsIo)
    println("Fourth part is io:")
    benchmark(fourthPartIsIo)
    println("Eight part is io:")
    benchmark(eightPartIsIo)
}

private fun benchmark(taskTypes: List<Boolean>) {
    val totalBlocked = (1..measurementsIterations).map {
        blocked(taskTypes)
    }.reduce(Results::plus)
    println("Blocked: ${stat(totalBlocked)}")

    val totalNotBlocked = (1..measurementsIterations).map {
        notBlocked(taskTypes)
    }.reduce(Results::plus)
    println("Not blocked: ${stat(totalNotBlocked)}")

    println("time difference: ${totalNotBlocked.time.toDouble() / totalBlocked.time.toDouble()}")
    println("calculations per ms difference: ${totalBlocked.calculationsPerMs / totalNotBlocked.calculationsPerMs}")
}

private fun stat(total: Results) = "time: ${total.time / measurementsIterations} ms, " +
        "calculations: ${total.calculations / measurementsIterations}, " +
        "calculations per ms: ${total.calculationsPerMs}"

private val fileHolder = FileHolder()

fun blocked(taskDescriptors: List<Boolean>): Results {

    val counter = AtomicInteger(0)
    val calculations = AtomicInteger(0)

    val end = AtomicLong(0)

    val start = Date().time
    val futures = taskDescriptors
            .map { isIoTask ->
                Callable<Any> {
                    if (counter.get() < goal) {
                        if (isIoTask) {
                            blackHole {
                                fileHolder.readFileSync()
                            }
                            counter.incrementAndGet()
                        } else {
                            blackHole {
                                gcLoadWork(random.get())
                            }
                            calculations.incrementAndGet()
                        }
                    } else {
                        end.compareAndSet(0, Date().time)
                    }
                }
            }.map { ForkJoinPool.commonPool().submit(it) }
    //waiting for remained
    blackHole {
        futures.map { it.get() }
    }
    return Results(end.get() - start, calculations.get())
}

fun notBlocked(taskDescriptors: List<Boolean>): Results {
    return runBlocking {
        val counter = AtomicInteger(0)
        val calculations = AtomicInteger(0)

        val end = AtomicLong(0)

        val start = Date().time
        val tasks = taskDescriptors
                .map { isIoTask ->
                    async<Any>(CommonPool) {
                        if (counter.get() < goal) {
                            if (isIoTask) {
                                blackHole {
                                    fileHolder.readFileAsync()
                                }
                                counter.incrementAndGet()
                            } else {
                                blackHole {
                                    gcLoadWork(random.get())
                                }
                                calculations.incrementAndGet()
                            }
                        } else {
                            end.compareAndSet(0, Date().time)
                        }
                    }
                }

        //waiting for remained
        blackHole {
            tasks.map { it.await() }
        }
        Results(end.get() - start, calculations.get())
    }
}

inline fun blackHole(body: () -> Any) {
    if (body.invoke().hashCode() == 0) {
        print("")
    }
}

data class Results(
        val time: Long,
        val calculations: Int
) {
    val calculationsPerMs = calculations.toDouble() / time.toDouble()

    operator fun plus(another: Results) = Results(time + another.time, calculations + another.calculations)
}

private fun gcLoadWork(random: Random): String {
    var result = 0.0
    repeat(50) {
        result *= BigDecimal(random.nextDouble()).hashCode()
    }
    return result.toString()
}
