package com.gnefedev.coroutine

import mu.KLogging
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.async.DeferredResult

@RestController
class DbBenchmarkController(
        private val jdbcTemplate: JdbcTemplate,
        private val asyncPgsql: AsyncPgsql
) {
    private companion object : KLogging()

    @GetMapping("/sync/{rowNum}")
    fun sync(@PathVariable(name = "rowNum") rowNum: Int): Result {
        val firstName = jdbcTemplate.queryForObject(
                "SELECT FIRST_NAME FROM PEOPLE WHERE ROW_NUM = $rowNum",
                String::class.java
        )
        val id = jdbcTemplate.queryForObject(
                "SELECT ID FROM PEOPLE WHERE ROW_NUM = $rowNum",
                Int::class.java
        )
        val payments = jdbcTemplate.queryForObject(
                "SELECT SUM(PAYMENT) as payment FROM PAYMENTS WHERE PEOPLE_ID = $id",
                Int::class.java
        )
        return Result(firstName, payments)
    }

    @GetMapping("/async/{rowNum}")
    fun async(@PathVariable(name = "rowNum") rowNum: Int): DeferredResult<Result> = asyncResponse {
        val firstName = asyncPgsql
                .connectionPool
                .sendQuery("SELECT FIRST_NAME FROM PEOPLE WHERE ROW_NUM = $rowNum")
                .await()
                .rows().get()[0]["FIRST_NAME"] as String
        val id = asyncPgsql
                .connectionPool
                .sendQuery("SELECT ID FROM PEOPLE WHERE ROW_NUM = $rowNum")
                .await()
                .rows().get()[0]["ID"] as Int
        val payments = asyncPgsql
                .connectionPool
                .sendQuery("SELECT SUM(PAYMENT) as payment FROM PAYMENTS WHERE PEOPLE_ID = $id")
                .await()
                .rows().get()[0]["payment"] as Long
        Result(firstName, payments.toInt())
    }
}

data class Result(
        val firstName: String,
        val payments: Int
)