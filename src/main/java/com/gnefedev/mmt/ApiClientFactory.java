package com.gnefedev.mmt;

import com.gnefedev.api.Api;
import com.gnefedev.api.MmtImplementation;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by gerakln on 06.11.16.
 */
public class ApiClientFactory<T> implements FactoryBean<T>, BeanFactoryAware, ResourceLoaderAware {

    private final Class<T> apiClientInterface;
    private BeanFactory beanFactory;
    private ClassLoader classLoader;

    public ApiClientFactory(Class<T> apiClientInterface) {
        this.apiClientInterface = apiClientInterface;
    }

    @Override
    public T getObject() throws Exception {
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setTarget(apiClientInterface);
        proxyFactory.setInterfaces(apiClientInterface.getInterfaces());

        Object mmtImplementation = beanFactory.getBean(getMmtImplementationClass());

        proxyFactory.addAdvice(new MmtInterceptor(mmtImplementation, apiClientInterface));

        @SuppressWarnings("unchecked")
        T proxy = (T) proxyFactory.getProxy(classLoader);
        return proxy;
    }

    private Class<?> getMmtImplementationClass() {
        try {
            return Class.forName(getApiInterface().getPackage().getName() + ".MmtImplementation");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private Class<?> getApiInterface() {
        for (Class<?> apiInterface : apiClientInterface.getInterfaces()) {
            if (apiInterface.isAnnotationPresent(Api.class)) {
                return apiInterface;
            }
        }
        throw new IllegalArgumentException(apiClientInterface + " не наследует @Api интеофейса");
    }

    @Override
    public Class<?> getObjectType() {
        return apiClientInterface;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        classLoader = resourceLoader.getClassLoader();
    }

    private static class MmtInterceptor implements MethodInterceptor {
        private final Map<Method, Method> delegates;

        private final Object mmtImplementation;

        private MmtInterceptor(Object mmtImplementation, Class<?> clientInterface) {
            this.mmtImplementation = mmtImplementation;
            delegates = new HashMap<>();
            for (Method method : clientInterface.getMethods()) {
                delegates.put(method, getDelegate(method));
            }
        }

        @Override
        public Object invoke(MethodInvocation methodInvocation) throws Throwable {
            Method delegate = delegates.get(methodInvocation.getMethod());
            MmtImplementation.Proxy proxy = (MmtImplementation.Proxy) delegate.invoke(
                    mmtImplementation,
                    methodInvocation.getArguments()
            );
            return proxy.getResult();
        }

        private Method getDelegate(Method method) {
            try {
                return mmtImplementation.getClass().getMethod(
                                method.getName(),
                                method.getParameterTypes()
                        );
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
