package com.gnefedev.mmt

import com.gnefedev.specific.MmtProxyFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Created by gerakln on 07.11.16.
 */
@Configuration
internal open class MmtConfig {
    @Bean
    open fun mmtProxyFactory() = MmtProxyFactory()
}