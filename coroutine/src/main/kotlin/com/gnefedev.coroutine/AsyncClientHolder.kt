package com.gnefedev.coroutine

import org.asynchttpclient.DefaultAsyncHttpClient
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.TearDown

@State(Scope.Benchmark)
class AsyncClientHolder {
    var client = DefaultAsyncHttpClient()

    @TearDown(Level.Trial)
    fun tearDown() {
        client.close()
    }
}