package com.gnefedev.coroutine

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import java.util.*
import kotlin.math.absoluteValue

@Component
class DataBaseSetup(
        private val jdbcTemplate: JdbcTemplate
) {
    fun init(peopleCount: Int) {
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
        for (batch in (0..peopleCount).chunked(500)) {
            jdbcTemplate.batchUpdate(
                    "INSERT INTO PEOPLE (FIRST_NAME, ROW_NUM) VALUES  (?, ?) RETURNING ID",
                    batch.map { arrayOf(random.nextDouble().toString(), it) }
            )
        }

        val ids = jdbcTemplate.queryForList("SELECT ID FROM PEOPLE", Int::class.java)

        for (batch in ids.chunked(500)) {
            repeat(4) {
                jdbcTemplate.batchUpdate(
                        "INSERT INTO PAYMENTS (PAYMENT, PEOPLE_ID) VALUES (?, ?) RETURNING ID",
                        batch.map { arrayOf(random.nextInt(3000).absoluteValue, it) }
                )
            }
        }
    }
}