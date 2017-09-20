package com.gnefedev.coroutine

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.nio.aRead
import kotlinx.coroutines.experimental.runBlocking
import java.io.File
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousFileChannel
import java.nio.channels.CompletionHandler
import java.nio.charset.Charset
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.ForkJoinPool

private val file = File(Server::class.java.classLoader.getResource("toRead.txt").toURI())
private val filePath = file.toPath()
val size = file.length().toInt()

interface Server {
    fun <T> doWork(work: () -> T): T
    fun <R, T> collect(count: Int, initVal: R, combine: (R, T) -> R, work: () -> T): R
    fun <T> withCallback(callback: () -> Any, work: () -> T)

    fun readSync(count: Int) = DoInFuture.collect(count, "", String::plus) {
        file.inputStream().use {
            val bytes = it.readBytes(size)
            return@collect bytes.toString(Charset.forName("UTF-8"))
        }
    }

    fun readAsync(count: Int): String

}

object DoInFuture : Server {
    private val executor: ExecutorService = ForkJoinPool.commonPool()

    override fun readAsync(count: Int) = DoInFuture.collect(count, "", String::plus) {
        AsynchronousFileChannel.open(filePath).use {
            var text: String? = null

            val bytes = ByteArray(size)
            val byteBuffer = ByteBuffer.wrap(bytes)
            val temp: CompletionHandler<Int, in Nothing?> = object : CompletionHandler<Int, Nothing?> {
                override fun completed(result: Int?, attachment: Nothing?) {
                    text = bytes.toString(Charset.forName("UTF-8"))
                }

                override fun failed(exc: Throwable?, attachment: Nothing?) {
                }
            }
            it.read(byteBuffer, 0L, null, temp)
            while (text == null) {
                Thread.yield()
            }
            return@collect text
        }
    }


    override fun <T> withCallback(callback: () -> Any, work: () -> T) {
        executor.submit {
            work.invoke()
            callback.invoke()
        }
    }

    override fun <R, T> collect(count: Int, initVal: R, combine: (R, T) -> R, work: () -> T): R {
        val tasks: MutableList<Callable<T>> = (0..count)
                .map { Callable { work.invoke() } }
                .toMutableList()
        return executor.invokeAll(tasks)
                .map { it.get()!! }
                .fold(initVal, combine)
    }

    override fun <T> doWork(work: () -> T): T = executor.submit(work).get()
}

object DoInCoroutine : Server {
    override fun readAsync(count: Int) = runBlocking {
        (0..count)
                .map {
                    async(CommonPool) {
                        val channel = AsynchronousFileChannel.open(filePath)
                        val bytes = ByteArray(size)
                        val byteBuffer = ByteBuffer.wrap(bytes)
                        channel.aRead(byteBuffer, 0L)
                        channel.close()
                        bytes.toString(Charset.forName("UTF-8"))
                    }
                }
                .map { it.await() }
                .fold("", String::plus)
    }

    override fun <T> withCallback(callback: () -> Any, work: () -> T) {
        async(CommonPool) {
            work.invoke()
            callback.invoke()
        }
    }

    override fun <R, T> collect(count: Int, initVal: R, combine: (R, T) -> R, work: () -> T): R {
        return runBlocking {
            (0..count)
                    .map {
                        async(CommonPool) {
                            work.invoke()
                        }
                    }
                    .map { it.await() }
                    .fold(initVal, combine)
        }
    }

    override fun <T> doWork(work: () -> T) = runBlocking {
        async(CommonPool) {
            work.invoke()
        }.await()
    }
}