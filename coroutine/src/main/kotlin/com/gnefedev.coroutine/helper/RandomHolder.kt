package com.gnefedev.coroutine.helper

import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import java.util.*

@State(Scope.Thread)
class RandomHolder {
    val random = Random(System.nanoTime())
}
