package com.gnefedev.mmt;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.beans.Introspector;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by gerakln on 06.11.16.
 */
public class ApiClientRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {

    private ResourceLoader resourceLoader;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry) {
        Set<Class<?>> apiClients = getApiClients(annotationMetadata);

        for (Class<?> apiClientInterface : apiClients) {
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(ApiClientFactory.class);

            builder.addConstructorArgValue(apiClientInterface);
            registry.registerBeanDefinition(
                    Introspector.decapitalize(apiClientInterface.getSimpleName()),
                    builder.getBeanDefinition()
            );

        }
    }

    private Set<Class<?>> getApiClients(AnnotationMetadata annotationMetadata) {
        String[] packagesToScan = (String[]) annotationMetadata.
                getAnnotationAttributes(EnableMmtClient.class.getName())
                .get("basePackages");
        ClassPathScanningCandidateComponentProvider scanner = new InterfaceAwareScanner(true);
        scanner.setResourceLoader(resourceLoader);
        scanner.addIncludeFilter(new AnnotationTypeFilter(ApiClient.class));

        Set<Class<?>> result = new HashSet<>();

        for (String basePackage : packagesToScan) {
            Set<BeanDefinition> beanDefinitions = scanner.findCandidateComponents(basePackage);
            for (BeanDefinition beanDefinition : beanDefinitions) {
                try {
                    result.add(Class.forName(beanDefinition.getBeanClassName()));
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return result;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * Custom extension of {@link ClassPathScanningCandidateComponentProvider} to make sure interfaces to not get dropped
     * from scanning results.
     *
     * @author Oliver Gierke
     */
    private static class InterfaceAwareScanner extends ClassPathScanningCandidateComponentProvider {

        private final boolean considerInterfaces;

        public InterfaceAwareScanner(boolean considerInterfaces) {
            super(false);
            this.considerInterfaces = considerInterfaces;
        }

        /*
         * (non-Javadoc)
         * @see org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider#isCandidateComponent(org.springframework.beans.factory.annotation.AnnotatedBeanDefinition)
         */
        @Override
        protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
            return super.isCandidateComponent(beanDefinition)
                    || considerInterfaces && beanDefinition.getMetadata().isInterface();
        }
    }

}
