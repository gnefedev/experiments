package com.gnefedev.flux.cats

import com.gnefedev.flux.DatasourceConfig
import com.gnefedev.flux.cats.model.Cat
import kotlinx.coroutines.experimental.CoroutineDispatcher
import kotlinx.coroutines.experimental.Runnable
import kotlinx.coroutines.experimental.async
import mu.KLogging
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.support.TransactionTemplate
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlin.coroutines.experimental.CoroutineContext

class AsyncCatsService(
    jdbcTemplate: JdbcTemplate,
    private val transactionTemplate: TransactionTemplate
) : BaseCatsService(jdbcTemplate) {
    private companion object : KLogging()

    private val baseThreadPool = ThreadPoolExecutor(
        DatasourceConfig.maxConnections,
        DatasourceConfig.maxConnections,
        0,
        TimeUnit.MILLISECONDS,
        SynchronousQueue<Runnable>(),
        ThreadPoolExecutor.AbortPolicy()
    )

    private val fallbackThreadPool = Executors.newCachedThreadPool()

    private val coroutineContext: CoroutineDispatcher =
        object : CoroutineDispatcher() {
            override fun dispatch(context: CoroutineContext, block: Runnable) {
                try {
                    baseThreadPool.execute(block)
                } catch (e: RejectedExecutionException) {
                    fallbackThreadPool.execute(block)
                }
            }
        }
//        newFixedThreadPoolContext(DatasourceConfig.maxConnections, "databaseThreadPool")

    suspend fun incrementRating(catId: Int): Int = inTransaction { incrementRatingInternal(catId) }

    suspend fun decrementRating(catId: Int): Int = inTransaction { decrementRatingInternal(catId) }

    suspend fun getCats(ownerId: Int): List<Cat> = inTransaction {
        getCatsInternal(ownerId)
    }

    private suspend fun <T> inTransaction(body: () -> T): T =
        async(context = coroutineContext) {
            transactionTemplate.execute {
                body()
            }!!
        }.await()
}