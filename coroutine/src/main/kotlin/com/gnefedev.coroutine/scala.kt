package com.gnefedev.coroutine

import com.github.mauricio.async.db.RowData
import kotlinx.coroutines.experimental.CancellableContinuation
import kotlinx.coroutines.experimental.suspendCancellableCoroutine
import scala.collection.GenSeqLike
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import java.util.concurrent.ForkJoinPool

suspend fun <T> Future<T>.await(): T = suspendCancellableCoroutine { cont: CancellableContinuation<T> ->
    onComplete({
        if (it.isSuccess) {
            cont.resume(it.get())
        } else {
            cont.resumeWithException(it.failed().get())
        }
    }, ExecutionContext.fromExecutor(ForkJoinPool.commonPool()))
}

operator fun <A, Repr> GenSeqLike<A, Repr>.get(int: Int): A = apply(int)

operator fun RowData.get(columnName: String): Any = apply(columnName.toLowerCase())
