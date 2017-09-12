package com.gnefedev.kotlin

import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.channels.Channel
import java.util.concurrent.atomic.AtomicInteger

interface Counter {
    fun count(): Int
    fun increment()
}

object AtomicCounter : Counter {
    private val count = AtomicInteger(0)

    override fun count() = count.get()

    override fun increment() {
        count.getAndUpdate { prev -> prev + generateNewValue() }
    }

}

object BrokenCounter : Counter {

    private var count = 0
    override fun count() = count

    override fun increment() {
        count += generateNewValue()
    }
}

object OneThreadCoroutineCounter : Counter {
    private val counterContext = newSingleThreadContext("CounterContext")
    private var count = 0

    override fun count() = runBlocking {
        async(counterContext) {
            count
        }.await()
    }

    override fun increment() {
        launch(counterContext) {
            count += generateNewValue()
        }
    }
}

class ChannelCoroutineCounter(
        private val capacity: Int = 0
) : Counter {
    private val counterContext = CommonPool
    private val channel = Channel<Int>(capacity)
    private var count = 0
    val isEmpty get() = channel.isEmpty

    init {
        launch(counterContext) {
            while (true) {
                channel.receive()
                count += generateNewValue()
            }
        }
    }

    override fun count(): Int = runBlocking {
        async(counterContext) {
            count
        }.await()
    }

    override fun increment() {
        if (capacity == 0) {
            runBlocking {
                channel.send(1)
            }
        } else {
            launch(counterContext) {
                channel.send(1)
            }
        }
    }
}

private fun generateNewValue(): Int {
    //слегка нагружаем проц, иначе ++ выходит атомарным
    (0..1000).filter { it == 2000 }.forEach { print("") }
    return 1
}
