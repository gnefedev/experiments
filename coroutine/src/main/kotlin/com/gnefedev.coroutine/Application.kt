package com.gnefedev.coroutine

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.launch
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.web.context.request.async.DeferredResult


val peopleCount = 10_000

@SpringBootApplication
class Application

fun main(args: Array<String>) {
    SpringApplication(Application::class.java).run()
}

fun <R> asyncResponse(body: suspend CoroutineScope.() -> R): DeferredResult<R> {
    val result = DeferredResult<R>()
    launch(CommonPool) {
        try {
            result.setResult(body.invoke(this))
        } catch (e: Exception) {
            result.setErrorResult(e)
        }
    }
    return result
}

