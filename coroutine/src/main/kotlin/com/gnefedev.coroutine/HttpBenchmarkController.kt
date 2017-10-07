package com.gnefedev.coroutine

import com.gnefedev.coroutine.helper.executeAsync
import mu.KLogging
import org.asynchttpclient.DefaultAsyncHttpClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import org.springframework.web.context.request.async.DeferredResult

@RestController
class HttpBenchmarkController {
    private companion object : KLogging()
    private val syncHttpClient = RestTemplate()
    private val asyncHttpClient = DefaultAsyncHttpClient()

    @GetMapping("/http/sync/{count}/{delay}")
    fun sync(
            @PathVariable(name = "count") count: Int,
            @PathVariable(name = "delay") delay: Int
    ): String {
        return (0 until count)
                .map { syncHttpClient.getForEntity("http://localhost:8081/stub/$delay", String::class.java) }
                .joinToString(",")
    }

    @GetMapping("/http/async/{count}/{delay}")
    fun async(
            @PathVariable(name = "count") count: Int,
            @PathVariable(name = "delay") delay: Int
    ): DeferredResult<String> = asyncResponse {
        (0 until count)
                .map { asyncHttpClient.prepareGet("http://localhost:8081/stub/$delay").executeAsync() }
                .joinToString(",")
    }
}