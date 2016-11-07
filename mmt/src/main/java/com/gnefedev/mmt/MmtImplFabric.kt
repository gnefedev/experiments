package com.gnefedev.mmt

import com.gnefedev.specific.MmtProxyFactory
import org.springframework.beans.factory.FactoryBean
import org.springframework.beans.factory.annotation.Autowired

/**
 * Created by gerakln on 07.11.16.
 */
internal class MmtImplFabric(val mmtImplClass: Class<*>) : FactoryBean<Any> {
    @Autowired
    private lateinit var mmtProxyFactory: MmtProxyFactory

    override fun getObject(): Any {
        return mmtProxyFactory.createImpl(mmtImplClass)
    }

    override fun isSingleton() = true

    override fun getObjectType() = mmtImplClass
}
