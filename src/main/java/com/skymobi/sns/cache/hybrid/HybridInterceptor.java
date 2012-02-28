package com.skymobi.sns.cache.hybrid;

import com.skymobi.sns.cache.AbstractInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: liweijing
 * Date: 11-11-25
 * Time: 上午10:05
 * Hybrid cache interceptor
 */
public class HybridInterceptor extends AbstractInterceptor {
    Logger logger = LoggerFactory.getLogger(HybridInterceptor.class);

    private AbstractInterceptor defaultInterceptor;

    private Map<String, AbstractInterceptor> interceptors = new HashMap<String, AbstractInterceptor>();

    public AbstractInterceptor getInterceptor(String cacheType) {
        if (cacheType.equals("")) {
            return defaultInterceptor;
        }
        if (interceptors.containsKey(cacheType)) {
            return interceptors.get(cacheType);
        }
        return defaultInterceptor;
    }

    public Map<String, AbstractInterceptor> getInterceptors() {
        return interceptors;
    }

    public void setInterceptors(Map<String, AbstractInterceptor> interceptors) {
        this.interceptors = interceptors;
    }

    public AbstractInterceptor getDefaultInterceptor() {
        return defaultInterceptor;
    }

    public void setDefaultInterceptor(AbstractInterceptor defaultInterceptor) {
        this.defaultInterceptor = defaultInterceptor;
    }


    @Override
    public Object loadFromCache(String key, Class<?> c, String cacheType) {
        return getInterceptor(cacheType).loadFromCache(key, c, cacheType);

    }

    @Override
    public void writeToCache(String cacheKey, Object value, int expire, boolean cacheNull, String cacheType) {
        getInterceptor(cacheType).writeToCache(cacheKey, value, expire, cacheNull, cacheType);
    }

    @Override
    public void writeListToCache(String cacheKey, Object value, boolean append, int max, String cacheType) {
        getInterceptor(cacheType).writeListToCache(cacheKey, value, append, max, cacheType);
    }

    @Override
    public List<?> loadListFromCache(String key, int offset, int limit, Class<?> c, String cacheType) {
        return getInterceptor(cacheType).loadListFromCache(key, offset, limit, c, cacheType);
    }

    @Override
    public void removeFromCache(String key, String cacheType) {
        getInterceptor(cacheType).removeFromCache(key, cacheType);
    }
}
