package com.gnefedev.flux.cats

import com.gnefedev.flux.MemcachedConfig
import com.gnefedev.flux.cats.model.Cat
import com.spotify.folsom.MemcacheClient
import mu.KLogging
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.io.Serializable
import java.util.concurrent.TimeUnit

@RestController
class CatsSyncController(
    private val catsService: SyncCatsService,
    private val memcacheClient: MemcacheClient<Serializable>
) {
    private companion object : KLogging()
    @PostMapping("/rating/{catId}/increment/")
    fun incrementRating(@PathVariable catId: Int) {
        val ownerId = catsService.incrementRating(catId)
        memcacheClient.delete("owner_$ownerId").toCompletableFuture().get(1, TimeUnit.MINUTES)
    }

    @PostMapping("/rating/{catId}/decrement/")
    fun decrementRating(@PathVariable catId: Int) {
        val ownerId = catsService.decrementRating(catId)
        memcacheClient.delete("owner_$ownerId").toCompletableFuture().get(1, TimeUnit.MINUTES)
    }

    @GetMapping("/cats/list/{ownerId}")
    fun getCats(@PathVariable ownerId: Int): List<Cat> {
        @Suppress("UNCHECKED_CAST")
        val catsFromCache = memcacheClient.get("owner_$ownerId").toCompletableFuture().get(1, TimeUnit.MINUTES) as List<Cat>?
        return when {
            catsFromCache != null -> {
                logger.info { "cache hit" }
                catsFromCache
            }
            else -> {
                logger.info { "cache miss" }
                val catsFromDb = catsService.getCats(ownerId)
                memcacheClient.add("owner_$ownerId", catsFromDb as Serializable, MemcachedConfig.ttl).toCompletableFuture().get(1, TimeUnit.MINUTES)
                catsFromDb
            }
        }
    }
}