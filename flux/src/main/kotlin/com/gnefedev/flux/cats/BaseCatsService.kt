package com.gnefedev.flux.cats

import com.gnefedev.flux.cats.model.Cat
import mu.KLogging
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import java.util.*
import kotlin.math.absoluteValue

const val ownersCount = 100_000

abstract class BaseCatsService(
    private val jdbcTemplate: JdbcTemplate
) {
    private companion object : KLogging()


//    @PostConstruct
    fun initDb() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS CATS")
        jdbcTemplate.execute("DROP TABLE IF EXISTS OWNER")
        jdbcTemplate.execute(
            """CREATE TABLE OWNER(
                                   ID                  SERIAL  PRIMARY KEY,
                                   NAME                TEXT    NOT NULL
)"""
        )
        jdbcTemplate.execute(
            """CREATE TABLE CATS(
                                   ID                  SERIAL  PRIMARY KEY,
                                   NAME                TEXT    NOT NULL,
                                   RATING              INT     NOT NULL,
                                   OWNER_ID            INT     NOT NULL
)"""
        )
        jdbcTemplate.execute(
            """CREATE INDEX ON cats (owner_id)"""
        )
        jdbcTemplate.execute(
            """ALTER TABLE cats ADD CONSTRAINT "fk_owner_id" FOREIGN KEY (owner_id) REFERENCES owner (id)"""
        )

        val random = Random()
        for (batch in (0..ownersCount).chunked(500)) {
            jdbcTemplate.batchUpdate(
                "INSERT INTO OWNER (NAME) VALUES  (?)",
                batch.map { arrayOf(random.nextDouble().toString()) }
            )
        }

        val ids = jdbcTemplate.queryForList("SELECT ID FROM OWNER", Int::class.java)

        for (batch in ids.chunked(500)) {
            jdbcTemplate.batchUpdate(
                "INSERT INTO CATS (NAME, RATING, OWNER_ID) VALUES (?, ?, ?)",
                batch.flatMap { ownerId ->
                    val catsCount = random.nextInt(4).absoluteValue + 1
                    (0..catsCount).map {
                        arrayOf(random.nextDouble().toString(), random.nextInt(300).absoluteValue, ownerId)
                    }
                }
            )
        }

        val maxCatId = jdbcTemplate.queryForObject("SELECT MAX(ID) FROM CATS", Long::class.java)
        logger.info { "!!!!! $maxCatId !!!!" }
    }

    protected fun incrementRatingInternal(catId: Int): Int = jdbcTemplate.queryForObject(
        "UPDATE cats SET rating = rating + 1 WHERE id = ? RETURNING owner_id",
        Int::class.java,
        catId
    )

    protected fun decrementRatingInternal(catId: Int): Int = jdbcTemplate.queryForObject(
        "UPDATE cats SET rating = rating - 1 WHERE id = ? RETURNING owner_id",
        Int::class.java,
        catId
    )

    protected fun getCatsInternal(ownerId: Int): List<Cat> = jdbcTemplate.query(
        "SELECT id, name, rating, owner_id FROM cats WHERE owner_id = ?",
        RowMapper<Cat> { rs, _ ->
            Cat(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getInt("rating"),
                rs.getInt("owner_id")
            )
        },
        ownerId
    )
}