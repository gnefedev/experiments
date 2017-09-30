package com.gnefedev.coroutine.benchmark

import com.gnefedev.coroutine.AsyncPgsql
import com.gnefedev.coroutine.await
import com.gnefedev.coroutine.get
import com.gnefedev.coroutine.helper.RandomHolder
import com.gnefedev.coroutine.helper.SpringHolder
import kotlinx.coroutines.experimental.runBlocking
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.OptionsBuilder
import org.springframework.jdbc.core.JdbcTemplate
import kotlin.math.absoluteValue

@Fork(1)
@Threads(Threads.MAX)
class BenchmarkOfSql {
    @Benchmark
    fun syncQuery(beansHolder: BeansHolder, randomHolder: RandomHolder): String {
        return beansHolder.jdbcTemplate.queryForObject(
                "SELECT SOME_TEXT FROM BENCHMARK WHERE ROW_NUM = ${(randomHolder.random.nextInt() % 10_000).absoluteValue}",
                String::class.java
        )
    }

    @Benchmark
    fun asyncQuery(beansHolder: BeansHolder, randomHolder: RandomHolder): String = runBlocking {
        beansHolder
                .asyncPgsql
                .connectionPool
                .sendQuery("SELECT SOME_TEXT FROM BENCHMARK WHERE ROW_NUM = ${(randomHolder.random.nextInt() % 10_000).absoluteValue}")
                .await()
                .rows().get()[0]["SOME_TEXT"] as String
    }

    @State(Scope.Benchmark)
    class BeansHolder {
        lateinit var jdbcTemplate: JdbcTemplate
        lateinit var asyncPgsql: AsyncPgsql

        @Setup
        fun setUp(springHolder: SpringHolder) {
            jdbcTemplate = springHolder.context.getBean(JdbcTemplate::class.java)
            asyncPgsql = springHolder.context.getBean(AsyncPgsql::class.java)
        }
    }
}

fun main(args: Array<String>) {
    val options = OptionsBuilder()
            .include(BenchmarkOfSql::class.java.simpleName)
            .jvmArgs("-Xmx2g")
            .build()
    Runner(options).run()
}
