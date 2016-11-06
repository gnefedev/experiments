package com.gnefedev.mmt

import com.gnefedev.sprcific.getSyncResult
import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation
import java.lang.reflect.Method

internal class MmtInterceptor internal constructor(private val mmtImplementation: Any, clientInterface: Class<*>) : MethodInterceptor {
    private val delegates: Map<Method, Method> = clientInterface
            .methods
            .map { Pair(it, getDelegate(it)) }
            .toMap()

    private val defaultTimeout: Int
    private val timeouts: Map<Method, Int> = clientInterface
            .methods
            .filter { it.isAnnotationPresent(Timeout::class.java) }
            .map { Pair(it, it.getAnnotation(Timeout::class.java).value) }
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
        val delegate = delegates[methodInvocation.method]!!
        val proxy = delegate.invoke(
                mmtImplementation,
                *methodInvocation.arguments
        )
        val timeout = timeouts.getOrElse(methodInvocation.method, { defaultTimeout })
        return getSyncResult(proxy, timeout)
    }

    private fun getDelegate(method: Method): Method {
        return mmtImplementation.javaClass.getMethod(
                method.name,
                *method.parameterTypes
        )
    }
}