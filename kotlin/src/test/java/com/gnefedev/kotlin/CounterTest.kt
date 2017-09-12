package com.gnefedev.kotlin

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

private val threadCount = Runtime.getRuntime().availableProcessors()

class CounterTest{

    @Test fun testAtomic() {
        val count = countParallel(AtomicCounter)

        assertEquals(threadCount, count)
    }

    @Test fun testCoroutine() {
        val count = countParallel(OneThreadCoroutineCounter)

        assertEquals(threadCount, count)
    }

    @Test fun testChannel() {
        val count = countParallel(ChannelCoroutineCounter(0))
        assertEquals(threadCount, count)
    }

    @Test fun testChannelWithCapacity() {
        val counter = ChannelCoroutineCounter(100)
        val count = countParallel(counter)
        for (i in 0..10) {
            if (!counter.isEmpty) {
                Thread.sleep(100)
            }
        }
        assertEquals(threadCount, count)
    }

    @Test fun fail() {
        val count = countParallel(BrokenCounter)

        assertNotEquals(threadCount, count)
    }

    private fun countParallel(counter: Counter): Int {
        val threadPool = Executors.newFixedThreadPool(threadCount)
        val barrier = CyclicBarrier(threadCount)

        for (i in 1..threadCount) {
            threadPool.execute {
                barrier.await()
                counter.increment()
            }
        }

        threadPool.shutdown()
        threadPool.awaitTermination(1, TimeUnit.SECONDS)

        return counter.count()
    }
}