package com.gnefedev.mmt

import com.gnefedev.sprcific.getMmtImplementationClass
import com.gnefedev.sprcific.getSyncResult
import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation
import org.springframework.aop.framework.ProxyFactory
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.BeanFactoryAware
import org.springframework.beans.factory.FactoryBean
import org.springframework.context.ResourceLoaderAware
import org.springframework.core.io.ResourceLoader
import java.lang.reflect.Method

/**
 * Created by gerakln on 06.11.16.
 */
internal class ApiClientFactory<T>(private val apiClientInterface: Class<T>) : FactoryBean<T>, BeanFactoryAware, ResourceLoaderAware {
    private lateinit var beanFactory: BeanFactory
    private lateinit var classLoader: ClassLoader

    @Throws(Exception::class)
    override fun getObject(): T {
        val proxyFactory = ProxyFactory()
        proxyFactory.setTarget(apiClientInterface)
        proxyFactory.setInterfaces(*apiClientInterface.interfaces)

        val mmtImplementation = beanFactory.getBean(getMmtImplementationClass(apiClientInterface))

        proxyFactory.addAdvice(MmtInterceptor(mmtImplementation, apiClientInterface))

        @Suppress("UNCHECKED_CAST")
        return proxyFactory.getProxy(classLoader) as T
    }

    override fun getObjectType(): Class<*> = apiClientInterface

    override fun isSingleton(): Boolean = true

    override fun setBeanFactory(beanFactory: BeanFactory) {
        this.beanFactory = beanFactory
    }

    override fun setResourceLoader(resourceLoader: ResourceLoader) {
        classLoader = resourceLoader.classLoader
    }

    private class MmtInterceptor internal constructor(private val mmtImplementation: Any, clientInterface: Class<*>) : MethodInterceptor {
        private val delegates: Map<Method, Method> = clientInterface
                .methods
                .map { Pair(it, getDelegate(it)) }
                .toMap()

        @Throws(Throwable::class)
        override fun invoke(methodInvocation: MethodInvocation): Any? {
            val delegate = delegates[methodInvocation.method]!!
            val proxy = delegate.invoke(
                    mmtImplementation,
                    *methodInvocation.arguments
            )
            return getSyncResult(proxy)
        }

        private fun getDelegate(method: Method): Method {
            return mmtImplementation.javaClass.getMethod(
                    method.name,
                    *method.parameterTypes
            )
        }
    }
}
