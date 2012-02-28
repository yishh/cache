package com.skymobi.sns.cache.memcached;

import com.skymobi.sns.cache.AbstractInterceptor;
import com.skymobi.sns.cache.NullCachedObject;
import com.skymobi.sns.cache.annotation.ListedCache;
import com.skymobi.sns.cache.annotation.WriteListCache;
import net.sf.cglib.proxy.MethodProxy;
import net.spy.memcached.MemcachedClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: liweijing
 * Date: 11-9-27
 * Time: 下午5:32
 * To change this template use File | Settings | File Templates.
 */
public class MemcachedInterceptor extends AbstractInterceptor {
    Logger logger = LoggerFactory.getLogger(MemcachedInterceptor.class);
    protected MemcachedClient memcachedClient;

    public MemcachedInterceptor(MemcachedClient client) {
        this.memcachedClient = client;
    }


    @Override
    public Object doListCache(ListedCache annotation, Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object writeListCache(WriteListCache annotation, Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        throw new UnsupportedOperationException();
    }


    @Override
    public Object loadFromCache(String key, Class<?> c, String cacheType) {
        return memcachedClient.get(key);
    }

    @Override
    public void writeToCache(String cacheKey, Object value, int expire, boolean cacheNull, String cacheType) {
        if (value != null) {
            memcachedClient.set(cacheKey, expire, value);
        }else{
            if(cacheNull){
                memcachedClient.set(cacheKey, expire, new NullCachedObject());
            }
        }
    }

    @Override
    public void writeListToCache(String cacheKey, Object value, boolean append, int max, String cacheType) {

    }

    @Override
    public List<?> loadListFromCache(String key, int offset, int limit, Class<?> c, String cacheType) {
        return null;
    }

    @Override
    public void removeFromCache(String key, String cacheType) {
        memcachedClient.delete(key);
    }


}
