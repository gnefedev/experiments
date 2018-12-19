package com.gnefedev.flux.cats

import com.gnefedev.flux.MemcachedConfig
import com.gnefedev.flux.cats.model.Cat
import com.spotify.folsom.MemcacheClient
import kotlinx.coroutines.experimental.future.await
import kotlinx.coroutines.experimental.reactor.mono
import mu.KLogging
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.io.Serializable

@RestController
class CatsAsyncController(
    private val catsService: AsyncCatsService,
    private val memcacheClient: MemcacheClient<Serializable>
) {
    companion object : KLogging()

    @PostMapping("/rating/{catId}/increment/")
    fun incrementRating(@PathVariable catId: Int): Mono<Unit> = mono {
        val ownerId = timer("incrementRating") {
            catsService.incrementRating(catId)
        }
        timer("incrementRating clean cache") {
            memcacheClient.delete("owner_$ownerId").await()
        }
        Unit
    }

    @PostMapping("/rating/{catId}/decrement/")
    fun decrementRating(@PathVariable catId: Int): Mono<Unit> = mono {
        val ownerId = timer("decrementRating") {
            catsService.decrementRating(catId)
        }
        timer("decrementRating clean cache") {
            memcacheClient.delete("owner_$ownerId").await()
        }
        Unit
    }

    @GetMapping("/cats/list/{ownerId}")
    fun getCats(@PathVariable ownerId: Int): Mono<List<Cat>> = mono {
        @Suppress("UNCHECKED_CAST")
        val catsFromCache = timer("cache get") {
            memcacheClient.get("owner_$ownerId").await() as List<Cat>?
        }
        return@mono when {
            catsFromCache != null -> {
                logger.info { "cache hit" }
                catsFromCache
            }
            else -> {
                logger.info { "cache miss" }
                val catsFromDb = timer("fetch from db") {
                    catsService.getCats(ownerId)
                }
                timer("add to cache") {
                    memcacheClient.add("owner_$ownerId", catsFromDb as Serializable, MemcachedConfig.ttl).await()
                }
                catsFromDb
            }
        }
    }

    private final inline fun <T> timer(message: String, body: () -> T): T {
        val begin = System.currentTimeMillis()
        val result = body()
        logger.info { "$message: ${System.currentTimeMillis() - begin} ms" }
        return result
    }

}
