package com.gnefedev.flux.cats

import com.gnefedev.flux.DatasourceConfig
import com.gnefedev.flux.cats.model.Cat
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.newFixedThreadPoolContext
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.support.TransactionTemplate

class AsyncCatsService(
    jdbcTemplate: JdbcTemplate,
    private val transactionTemplate: TransactionTemplate
) : BaseCatsService(jdbcTemplate) {
    private val coroutineContext = newFixedThreadPoolContext(DatasourceConfig.maxConnections, "databaseThreadPool")

    suspend fun incrementRating(catId: Int): Int = inTransaction { incrementRatingInternal(catId) }

    suspend fun decrementRating(catId: Int): Int = inTransaction { decrementRatingInternal(catId) }

    suspend fun getCats(ownerId: Int): List<Cat> = inTransaction { getCatsInternal(ownerId) }

    private suspend fun <T> inTransaction(body: () -> T): T =
        async(context = coroutineContext) {
            transactionTemplate.execute {
                body()
            }!!
        }.await()
}