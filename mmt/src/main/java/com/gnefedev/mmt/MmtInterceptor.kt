package com.gnefedev.mmt

import com.gnefedev.sprcific.getAsyncResult
import com.gnefedev.sprcific.getSyncResult
import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation
import java.lang.reflect.Method
import java.util.*

internal class MmtInterceptor internal constructor(private val mmtImplementation: Any, clientInterface: Class<*>) : MethodInterceptor {
    private val delegates: Map<MethodReference, Method> = clientInterface
            .methods
            .map { MethodReference(it) }
            .map { Pair(it, getDelegate(it)) }
            .toMap()

    private val defaultTimeout: Int
    private val timeouts: Map<MethodReference, Int> = clientInterface
            .methods
            .filter { it.isAnnotationPresent(Timeout::class.java) }
            .map { Pair(MethodReference(it), it.getAnnotation(Timeout::class.java).value) }
            .toMap()

    init {
        if (clientInterface.isAnnotationPresent(Timeout::class.java)) {
            defaultTimeout = clientInterface.getAnnotation(Timeout::class.java).value
        } else {
            defaultTimeout = 30
        }
    }

    @Throws(Throwable::class)
    override fun invoke(methodInvocation: MethodInvocation): Any? {
        val methodReference = MethodReference(methodInvocation.method)
        val delegate = delegates[methodReference]!!
        val proxy = delegate.invoke(
                mmtImplementation,
                *methodInvocation.arguments
        )
        if (isAsync(methodReference)) {
            return getAsyncResult(proxy)
        } else {
            val timeout = timeouts.getOrElse(methodReference, { defaultTimeout })
            return getSyncResult(proxy, timeout)
        }
    }

    private fun getDelegate(reference: MethodReference): Method {
        val delegateMethodName: String
        if (isAsync(reference)) {
            delegateMethodName = reference.methodName.substring(0, reference.methodName.length - "Async".length)
        } else {
            delegateMethodName = reference.methodName
        }
        return mmtImplementation.javaClass.getMethod(
                delegateMethodName,
                *reference.parameterTypes
        )
    }

    private fun isAsync(methodReference: MethodReference): Boolean {
        return methodReference.methodName.endsWith("Async")
    }

    private data class MethodReference(
            val methodName: String,
            val parameterTypes: Array<Class<*>>
    ) {
        constructor(method: Method) : this(method.name, method.parameterTypes)

        override fun equals(other: Any?): Boolean {
            if (other !is MethodReference) {
                return false
            } else {
                return methodName == other.methodName && Arrays.equals(parameterTypes, other.parameterTypes)
            }
        }

        override fun hashCode(): Int {
            return methodName.hashCode() + 32 * Arrays.hashCode(parameterTypes)
        }
    }
}