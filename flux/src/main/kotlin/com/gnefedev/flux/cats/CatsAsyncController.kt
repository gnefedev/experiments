package com.gnefedev.flux.cats

import com.gnefedev.flux.cats.model.Cat
import com.spotify.folsom.MemcacheClient
import kotlinx.coroutines.experimental.future.await
import kotlinx.coroutines.experimental.reactor.mono
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
    @PostMapping("/rating/{catId}/increment/")
    fun incrementRating(@PathVariable catId: Int): Mono<Unit> = mono {
        val ownerId = catsService.incrementRating(catId)
        memcacheClient.delete("owner_$ownerId").await()
        Unit
    }

    @PostMapping("/rating/{catId}/decrement/")
    fun decrementRating(@PathVariable catId: Int): Mono<Unit> = mono {
        val ownerId = catsService.decrementRating(catId)
        memcacheClient.delete("owner_$ownerId").await()
        Unit
    }

    @GetMapping("/cats/list/{ownerId}")
    fun getCats(@PathVariable ownerId: Int): Mono<List<Cat>> = mono {
        @Suppress("UNCHECKED_CAST")
        val catsFromCache = memcacheClient.get("owner_$ownerId").await() as List<Cat>?
        return@mono when {
            catsFromCache != null -> catsFromCache
            else -> {
                val catsFromDb = catsService.getCats(ownerId)
                memcacheClient.add("owner_$ownerId", catsFromDb as Serializable, 60 * 60).await()
                catsFromDb
            }
        }
    }
}