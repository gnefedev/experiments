package com.gnefedev.coroutine.benchmark

import com.gnefedev.coroutine.AsyncPgsql
import com.gnefedev.coroutine.await
import com.gnefedev.coroutine.get
import com.gnefedev.coroutine.helper.RandomHolder
import com.gnefedev.coroutine.helper.SpringHolder
import com.gnefedev.coroutine.peopleCount
import kotlinx.coroutines.experimental.runBlocking
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.OptionsBuilder
import org.springframework.jdbc.core.JdbcTemplate
import kotlin.math.absoluteValue

//4-core
//Benchmark                   Mode  Cnt     Score     Error  Units
//BenchmarkOfSql.asyncQuery  thrpt   20  2346.152 ± 108.606  ops/s
//BenchmarkOfSql.syncQuery   thrpt   20  2453.531 ± 100.639  ops/s
//8-core 100_000 PEOPLE
//Benchmark                   Mode  Cnt     Score    Error  Units
//BenchmarkOfSql.asyncQuery  thrpt   20  1094.403 ± 10.465  ops/s
//BenchmarkOfSql.syncQuery   thrpt   20  1209.474 ± 49.961  ops/s
//8-core 500_000 PEOPLE
//Benchmark                   Mode  Cnt    Score   Error  Units
//BenchmarkOfSql.asyncQuery  thrpt   20  250.495 ± 1.965  ops/s
//BenchmarkOfSql.syncQuery   thrpt   20  248.031 ± 0.485  ops/s
@Fork(1)
@Threads(Threads.MAX)
class BenchmarkOfSql {
    @Benchmark
    fun syncQuery(beansHolder: BeansHolder, randomHolder: RandomHolder): String {
        return beansHolder.jdbcTemplate.queryForObject(
                "SELECT FIRST_NAME FROM PEOPLE WHERE ROW_NUM = ${randomHolder.random.nextInt(peopleCount).absoluteValue}",
                String::class.java
        )
    }

    @Benchmark
    fun asyncQuery(beansHolder: BeansHolder, randomHolder: RandomHolder): String = runBlocking {
        beansHolder
                .asyncPgsql
                .connectionPool
                .sendQuery("SELECT FIRST_NAME FROM PEOPLE WHERE ROW_NUM = ${randomHolder.random.nextInt(peopleCount).absoluteValue}")
                .await()
                .rows().get()[0]["FIRST_NAME"] as String
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
