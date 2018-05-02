package com.gnefedev.flux.cats

import com.gnefedev.flux.cats.model.Cat
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.annotation.Transactional

@Transactional
class SyncCatsService(
    jdbcTemplate: JdbcTemplate
) : BaseCatsService(jdbcTemplate) {
    fun incrementRating(catId: Int): Int = incrementRatingInternal(catId)

    fun decrementRating(catId: Int): Int = decrementRatingInternal(catId)

    fun getCats(ownerId: Int): List<Cat> = getCatsInternal(ownerId)
}