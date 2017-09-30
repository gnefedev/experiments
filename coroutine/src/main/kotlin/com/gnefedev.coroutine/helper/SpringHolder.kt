package com.gnefedev.coroutine.helper

import com.gnefedev.coroutine.Application
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.TearDown
import org.springframework.boot.SpringApplication
import org.springframework.context.ConfigurableApplicationContext

@State(Scope.Benchmark)
class SpringHolder {
    lateinit var context: ConfigurableApplicationContext

    @Setup
    fun setUp() {
        context = SpringApplication.run(Application::class.java)
    }

    @TearDown
    fun tearDown() {
        context.stop()
    }
}
