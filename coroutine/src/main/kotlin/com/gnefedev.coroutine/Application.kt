package com.gnefedev.coroutine

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import java.util.*
import javax.annotation.PostConstruct
import kotlin.math.absoluteValue

@SpringBootApplication
class Application

fun main(args: Array<String>) {
    SpringApplication(Application::class.java).run()
}

@Component
class BaseSetup(
        private val jdbcTemplate: JdbcTemplate
) {
    @PostConstruct
    fun init() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS PEOPLE")
        jdbcTemplate.execute("DROP TABLE IF EXISTS PAYMENTS")
        jdbcTemplate.execute("""CREATE TABLE PEOPLE(
                                   ID                  SERIAL  PRIMARY KEY,
                                   FIRST_NAME                TEXT    NOT NULL,
                                   ROW_NUM             INT     NOT NULL
)""")
        jdbcTemplate.execute("""CREATE TABLE PAYMENTS(
                                   ID                  SERIAL  PRIMARY KEY,
                                   PAYMENT             INT     NOT NULL,
                                   PEOPLE_ID           INT     NOT NULL
)""")

        val random = Random()
        for (count in 0..10_000) {
            val id = jdbcTemplate.queryForObject(
                    "INSERT INTO PEOPLE (FIRST_NAME, ROW_NUM) VALUES  (?, ?) RETURNING ID",
                    Int::class.java,
                    random.nextDouble().toString(),
                    count
            )
            repeat(random.nextInt(4).absoluteValue + 1) {
                jdbcTemplate.queryForObject(
                        "INSERT INTO PAYMENTS (PAYMENT, PEOPLE_ID) VALUES (?, ?) RETURNING ID",
                        Int::class.java,
                        random.nextInt(3000).absoluteValue,
                        id
                )
            }
        }
    }
}

data class Result(
        val firstName: String,
        val payments: Int,
        val dataFromYandex: String,
        val randomFromFile: String
)
