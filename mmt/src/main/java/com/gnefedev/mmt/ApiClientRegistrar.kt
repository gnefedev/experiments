package com.gnefedev.mmt

import com.gnefedev.specific.getMmtImplementationClass
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.context.ResourceLoaderAware
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar
import org.springframework.core.io.ResourceLoader
import org.springframework.core.type.AnnotationMetadata
import org.springframework.core.type.filter.AnnotationTypeFilter

/**
 * Created by gerakln on 06.11.16.
 */
internal class ApiClientRegistrar : ImportBeanDefinitionRegistrar, ResourceLoaderAware {
    private lateinit var resourceLoader: ResourceLoader

    override fun registerBeanDefinitions(annotationMetadata: AnnotationMetadata, registry: BeanDefinitionRegistry) {
        val apiClients = getApiClients(annotationMetadata)

        for (apiClientInterface in apiClients) {
            registerApiClient(registry, apiClientInterface)

            registerMmtClient(registry, apiClientInterface)
        }
    }

    private fun registerMmtClient(registry: BeanDefinitionRegistry, apiClientInterface: Class<*>) {
        val mmtImplementationClass = getMmtImplementationClass(apiClientInterface)
        val builder = BeanDefinitionBuilder.rootBeanDefinition(MmtImplFabric::class.java)
        builder.addConstructorArgValue(mmtImplementationClass)

        registry.registerBeanDefinition(mmtImplementationClass.getBeanName(), builder.beanDefinition)
    }


    private fun registerApiClient(registry: BeanDefinitionRegistry, apiClientInterface: Class<*>) {
        val builder = BeanDefinitionBuilder.rootBeanDefinition(ApiClientFactory::class.java)

        builder.addConstructorArgValue(apiClientInterface)
        registry.registerBeanDefinition(apiClientInterface.getBeanName(), builder.beanDefinition)
    }

    private fun getApiClients(annotationMetadata: AnnotationMetadata): Set<Class<*>> {
        val annotationAttributes = annotationMetadata.getAnnotationAttributes(EnableMmtClient::class.java.name)
        @Suppress("UNCHECKED_CAST")
        val packagesToScan = annotationAttributes["basePackages"] as Array<String>
        val scanner = InterfaceAwareScanner(true)
        scanner.resourceLoader = resourceLoader
        scanner.addIncludeFilter(AnnotationTypeFilter(ApiClient::class.java))

        return packagesToScan
                .flatMap { scanner.findCandidateComponents(it) }
                .map { it.beanClassName }
                .map { Class.forName(it) }
                .toSet()
    }

    override fun setResourceLoader(resourceLoader: ResourceLoader) {
        this.resourceLoader = resourceLoader
    }

    /**
     * Custom extension of [ClassPathScanningCandidateComponentProvider] to make sure interfaces to not get dropped
     * from scanning results.

     * @author Oliver Gierke
     */
    private class InterfaceAwareScanner(private val considerInterfaces: Boolean) : ClassPathScanningCandidateComponentProvider(false) {

        /*
         * (non-Javadoc)
         * @see org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider#isCandidateComponent(org.springframework.beans.factory.annotation.AnnotatedBeanDefinition)
         */
        override fun isCandidateComponent(beanDefinition: AnnotatedBeanDefinition): Boolean {
            return super.isCandidateComponent(beanDefinition) || considerInterfaces && beanDefinition.metadata.isInterface
        }
    }
}
