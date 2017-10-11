package com.gnefedev.coroutine

import com.gnefedev.coroutine.helper.executeAsync
import com.gnefedev.coroutine.helper.future
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import mu.KLogging
import org.asynchttpclient.DefaultAsyncHttpClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import org.springframework.web.context.request.async.DeferredResult
import java.util.concurrent.Callable
import java.util.concurrent.ForkJoinPool

@RestController
class HttpBenchmarkController {
    private companion object : KLogging()

    private val syncHttpClient = RestTemplate()
    private val asyncHttpClient = DefaultAsyncHttpClient()

    @GetMapping("/http/serial/sync/{count}/{delay}")
    fun serialSync(
            @PathVariable(name = "count") count: Int,
            @PathVariable(name = "delay") delay: Int
    ): String {
        return (0 until count)
                .map {
                    syncHttpClient.getForEntity("http://localhost:8081/stub/$delay", String::class.java).body
                }
                .joinToString(",")
    }

    @GetMapping("/http/batch/sync/{count}/{delay}")
    fun batchSync(
            @PathVariable(name = "count") count: Int,
            @PathVariable(name = "delay") delay: Int
    ): String {
        return (0 until count)
                .map {
                    Callable {
                        syncHttpClient.getForEntity("http://localhost:8081/stub/$delay", String::class.java).body
                    }
                }
                .let { ForkJoinPool.commonPool().invokeAll(it) }
                .map { it.get() }
                .joinToString(",")
    }

    @GetMapping("/http/serial/async/{count}/{delay}")
    fun serialAsync(
            @PathVariable(name = "count") count: Int,
            @PathVariable(name = "delay") delay: Int
    ): DeferredResult<String> = asyncResponse {
        (0 until count)
                .map { asyncHttpClient.prepareGet("http://localhost:8081/stub/$delay").executeAsync() }
                .map { it.responseBody }
                .joinToString(",")
    }

    @GetMapping("/http/batch/async_v1/{count}/{delay}")
    fun batchAsyncV1(
            @PathVariable(name = "count") count: Int,
            @PathVariable(name = "delay") delay: Int
    ): DeferredResult<String> = asyncResponse {
        (0 until count)
                .map {
                    async(CommonPool) {
                        asyncHttpClient.prepareGet("http://localhost:8081/stub/$delay").executeAsync()
                    }
                }
                .map { it.await() }
                .map { it.responseBody }
                .joinToString(",")
    }

    @GetMapping("/http/batch/async_v2/{count}/{delay}")
    fun batchAsyncV2(
            @PathVariable(name = "count") count: Int,
            @PathVariable(name = "delay") delay: Int
    ): DeferredResult<String> = asyncResponse {
        (0 until count)
                .map {
                    asyncHttpClient.prepareGet("http://localhost:8081/stub/$delay").future(CommonPool)
                }
                .map { it.await() }
                .map { it.responseBody }
                .joinToString(",")
    }
}