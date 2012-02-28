package com.skymobi.sns.cache;

import com.skymobi.sns.cache.annotation.*;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * Created by IntelliJ IDEA.
 * User: liweijing
 * Date: 11-8-3
 * Time: 下午3:07
 * To change this template use File | Settings | File Templates.
 */
public interface CacheInterceptor extends MethodInterceptor {
    Object doSimpleCache(SimpleCache annotation, Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable;

    Object doCache(Cache annotation, Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable;

    Object doListCache(ListedCache annotation, Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable;

    Object writeListCache(WriteListCache annotation, Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable;

    Object writeCache(WriteCache annotation, Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable;

    Object removeCache(RemoveCache annotation, Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable;
}
