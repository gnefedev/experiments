package com.gnefedev.mmt

import com.gnefedev.specific.getMmtImplementationClass
import org.springframework.aop.framework.ProxyFactory
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.BeanFactoryAware
import org.springframework.beans.factory.FactoryBean
import org.springframework.context.ResourceLoaderAware
import org.springframework.core.io.ResourceLoader

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
        proxyFactory.setInterfaces(apiClientInterface, *apiClientInterface.interfaces)

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

}
