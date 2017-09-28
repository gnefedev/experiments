package com.gnefedev.coroutine

import kotlinx.coroutines.experimental.nio.aRead
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import java.io.File
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousFileChannel
import java.nio.charset.Charset
import java.nio.file.Path

@State(Scope.Benchmark)
class FileHolder {
    private val file = File(BenchmarkOfIO::class.java.classLoader.getResource("toRead.txt").toURI())
    private val filePath: Path = file.toPath()
    private val size = file.length().toInt()

    suspend fun readFileAsync(): String {
        val channel = AsynchronousFileChannel.open(filePath)
        val bytes = ByteArray(size)
        val byteBuffer = ByteBuffer.wrap(bytes)
        channel.aRead(byteBuffer, 0L)
        channel.close()
        return bytes.toString(Charset.forName("UTF-8"))
    }

    fun readFileSync() = file.inputStream().use {
        it.readBytes(size).toString(Charset.forName("UTF-8"))
    }
}