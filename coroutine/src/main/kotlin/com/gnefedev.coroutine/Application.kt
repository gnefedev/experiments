package com.gnefedev.coroutine

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import java.util.*
import javax.annotation.PostConstruct

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
        jdbcTemplate.execute("DROP TABLE IF EXISTS BENCHMARK")
        jdbcTemplate.execute("""CREATE TABLE BENCHMARK(
                                   ID                  SERIAL  PRIMARY KEY,
                                   SOME_TEXT           TEXT    NOT NULL,
                                   ROW_NUM             INT     NOT NULL
)""")
        val random = Random()
        for(count in 0..10_000) {
            jdbcTemplate.queryForObject(
                    "INSERT INTO BENCHMARK (SOME_TEXT, ROW_NUM) VALUES  (?, ?) RETURNING ID",
                    Int::class.java,
                    random.nextDouble().toString(),
                    count
            )
        }
    }
}

