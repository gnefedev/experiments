package com.gnefedev.coroutine

import kotlinx.coroutines.experimental.delay
import org.springframework.web.bind.annotation.*

@RestController
class UtilController(
        private val dataBaseSetup: DataBaseSetup
) {
    @PostMapping("/reloadDb")
    fun reloadDb(@RequestParam(name = "count") count: Int): String {
        dataBaseSetup.init(count)
        return "reloaded"
    }

    @GetMapping("/stub/{time}")
    fun stub(@PathVariable(name = "time") time: Long) = asyncResponse {
        delay(time)
        "delay for $time"
    }

    @GetMapping("/nothingToDo")
    fun nothingToDo() = "ok"
}