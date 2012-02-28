package com.skymobi.sns.cache;

import com.skymobi.sns.cache.annotation.CacheProxy;
import net.sf.cglib.proxy.Enhancer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory;
import org.springframework.beans.factory.support.CglibSubclassingInstantiationStrategy;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.support.SimpleInstantiationStrategy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: liweijing
 * Date: 11-8-1
 * Time: 下午3:16
 * To change this template use File | Settings | File Templates.
 */
public class EasyCacheProxy extends SimpleInstantiationStrategy implements BeanFactoryPostProcessor {
    private CglibSubclassingInstantiationStrategy defaultInstantiationStrategy = new CglibSubclassingInstantiationStrategy();
    Logger logger = LoggerFactory.getLogger(EasyCacheProxy.class);


    public Map<String, CacheInterceptor> getInterceptors() {
        return interceptors;
    }

    public void setInterceptors(Map<String, CacheInterceptor> interceptors) {
        this.interceptors = interceptors;
    }

    private Map<String, CacheInterceptor> interceptors = new HashMap<String, CacheInterceptor>();

    static final String DEFAULT_INTERCEPTOR = "default";

    public EasyCacheProxy(CacheInterceptor interceptor) {
        interceptors.put(DEFAULT_INTERCEPTOR, interceptor);
    }


    public void postProcessBeanFactory(
            ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (beanFactory instanceof AbstractAutowireCapableBeanFactory) {
            ((AbstractAutowireCapableBeanFactory) beanFactory).setInstantiationStrategy(this);
        }
    }

    protected boolean proxyThis(RootBeanDefinition beanDefinition, String beanName) {
        Class<?> beanClass = beanDefinition.getBeanClass();
        Annotation annotation = beanClass.getAnnotation(CacheProxy.class);
        if (annotation != null && !beanDefinition.getMethodOverrides().isEmpty())
            throw new UnsupportedOperationException("Method Injection not supported!");
        return annotation != null;
    }

    @Override
    public Object instantiate(RootBeanDefinition beanDefinition,
                              String beanName, BeanFactory owner, Constructor constructor, Object[] args) {
        if (proxyThis(beanDefinition, beanName)) {
            return createProxy(beanDefinition.getBeanClass());
        } else {
            return this.defaultInstantiationStrategy.instantiate(beanDefinition, beanName, owner, constructor, args);
        }
    }

    @Override
    public Object instantiate(RootBeanDefinition beanDefinition,
                              String beanName, BeanFactory owner) {
        if (proxyThis(beanDefinition, beanName)) {
            return createProxy(beanDefinition.getBeanClass());
        } else {
            return this.defaultInstantiationStrategy.instantiate(beanDefinition, beanName, owner);
        }
    }

    protected Object createProxy(final Class<?> beanClass) {
        Enhancer enhancer = new Enhancer();
        CacheProxy annotation = beanClass.getAnnotation(CacheProxy.class);
        String cacheType = annotation.type();
        if(cacheType.equals("")){
            cacheType = DEFAULT_INTERCEPTOR;
        }
        CacheInterceptor interceptor = interceptors.get(cacheType);
        if(interceptor == null){
            throw new UnsupportedOperationException("cache type "+ cacheType + " not configured");
        }
        enhancer.setCallback(interceptor);
        enhancer.setSuperclass(beanClass);
//        enhancer.setNamingPolicy();
        return enhancer.create();
    }
}
