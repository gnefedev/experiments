package com.gnefedev.coroutine

import com.gnefedev.coroutine.helper.FileHolder
import com.gnefedev.coroutine.helper.executeAsync
import kotlinx.coroutines.experimental.runBlocking
import org.asynchttpclient.DefaultAsyncHttpClient
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class BenchmarkController(
        private val jdbcTemplate: JdbcTemplate,
        private val asyncPgsql: AsyncPgsql
) {
    private val httpClient = DefaultAsyncHttpClient()
    private val fileHolder = FileHolder()

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
        val dataFromYandex = httpClient.prepareGet("https://yandex.ru/search/?text=$firstName&lr=$firstName")
                .execute()
                .get()
                .responseBody
        return Result(
                firstName,
                payments,
                dataFromYandex,
                shuffle(fileHolder.readFileSync())
        )
    }

    @GetMapping("/async/{rowNum}")
    fun async(@PathVariable(name = "rowNum") rowNum: Int): Result {
        return runBlocking {
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

            val dataFromYandex = httpClient.prepareGet("https://yandex.ru/search/?text=$firstName&lr=$firstName")
                    .executeAsync()
                    .responseBody
            Result(
                    firstName,
                    payments.toInt(),
                    dataFromYandex,
                    shuffle(fileHolder.readFileAsync())
            )
        }
    }
}

private fun shuffle(formFile: String) = formFile.toCharArray().toList().shuffled().joinToString()
