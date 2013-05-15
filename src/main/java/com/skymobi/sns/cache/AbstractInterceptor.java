package com.skymobi.sns.cache;

import com.skymobi.sns.cache.annotation.*;
import net.sf.cglib.proxy.MethodProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Stack;

/**
 *
 * User: liweijing
 * Date: 11-11-25
 * Time: 上午9:21
 *
 */
public abstract class AbstractInterceptor implements CacheInterceptor {
//    protected  com.google.common.cache.Cache<String, Object> localCache = CacheBuilder.newBuilder()
//            .concurrencyLevel(4)
//            .weakKeys()
//            .maximumSize(10000)
//            .expireAfterWrite(30, TimeUnit.MINUTES)
//            .build(new CacheLoader<String, Object>() {
//                @Override
//                public Object load(String key) throws Exception {
//                    return loadFromCache(key,);
//                }
//            });

    private boolean enableLocalCache;

    Logger logger = LoggerFactory.getLogger(AbstractInterceptor.class);


    protected Object getParameterValue(int index, Object[] parameters) {
//        localCache.get

        return parameters[index];
    }

    public abstract Object loadFromCache(String key, Class<?> c, String cacheType);

    public abstract void writeToCache(String cacheKey, Object value, int expire, boolean cacheNull, String cacheType);

    public abstract void writeListToCache(String cacheKey, Object value, boolean append, int max, String cacheType);

    public abstract List<?> loadListFromCache(String key, int offset, int limit, Class<?> c, String cacheType);

    public abstract void removeFromCache(String key, String cacheType);

    private Object simpleCache(String key, boolean cacheNull, int expire, Object obj, String cacheType, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        Object cached = null;
        try {
            logger.debug("try load  [{}] from cache", key);
            cached = loadFromCache(key, method.getReturnType(), cacheType);
        } catch (Exception e) {
            logger.info("load from cache error:" + e.getCause());
        }
        if (cached == null) {
            logger.debug("load cache [{}] fail,invoke super", key);
            cached = proxy.invokeSuper(obj, args);
            try {
                Object cacheObj = cached;
                if (cached == null) {
                    if (cacheNull) {
                        cacheObj = new NullCachedObject();
                    }
                }
                logger.debug("write cache [{}]", key);
                writeToCache(key, cacheObj, expire, false, cacheType);
            } catch (Exception e) {
                logger.info("set to cache error:" + e.getCause());
            }
        }
        if (cached instanceof NullCachedObject) {
            cached = null;
            logger.debug("cached [{}] is null", key);
        }

        return cached;
    }

    public Object doSimpleCache(SimpleCache annotation, Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        String key = annotation.key();
        boolean cacheNull = annotation.cacheNull();
        int expire = CacheUtils.getExpire(annotation.expireTime(), annotation.expire());
        return simpleCache(key, cacheNull, expire, obj, annotation.type(), method, args, proxy);
    }

    public Object doCache(Cache annotation, Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        CacheKey key = annotation.key();
        String cacheKey = CacheUtils.parseCacheKey(key, args);
        boolean cacheNull = annotation.cacheNull();
        int expire = CacheUtils.getExpire(annotation.expireTime(), annotation.expire());
        return simpleCache(cacheKey, cacheNull, expire, obj, annotation.type(), method, args, proxy);
    }

    public Object doListCache(ListedCache annotation, Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        CacheKey key = annotation.key();
        String cacheKey = CacheUtils.parseCacheKey(key, args);
        Integer offset = (Integer) getParameterValue(annotation.offsetIndex(), args);
        Integer limit = (Integer) getParameterValue(annotation.limitIndex(), args);
        List<?> result = null;
        try {
            logger.debug(String.format("try load list cache %s:%s,%s", cacheKey, offset, limit));
            result = loadListFromCache(cacheKey, offset, limit, Object.class, annotation.type());
            logger.debug("local list cache:" + result.size());
        } catch (Exception e) {
            logger.info("load list cache error:" + e.getCause());
        }
        if (result != null && result.size() == limit) {
            return result;
        } else {
            if (result != null) {
                offset = offset + result.size();
                limit = limit - result.size();
            }
            args[annotation.offsetIndex()] = offset;
            args[annotation.limitIndex()] = limit;
            List dbResult = (List) proxy.invokeSuper(obj, args);
            if (result == null) {
                return dbResult;
            }
            result.addAll(dbResult);
        }
        return result;
    }

    public Object writeListCache(WriteListCache annotation, Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        CacheKey key = annotation.key();
        String cacheKey = CacheUtils.parseCacheKey(key, args);
        try {
            if (annotation.writeParameter()) {
                Object value = getParameterValue(annotation.parameterIndex(), args);
                writeListToCache(cacheKey, value, annotation.append(), annotation.max(), annotation.type());
            }
        } catch (Exception e) {
            logger.info("write parm cache error:" + e.getCause());
        }
        Object returnVal = proxy.invokeSuper(obj, args);
        try {
            if (annotation.writeReturn()) {
                writeListToCache(cacheKey, returnVal, annotation.append(), annotation.max(), annotation.type());
            }
        } catch (Exception e) {
            logger.info("write return cache error:" + e.getCause());
        }
        return returnVal;
    }

    public Object writeCache(WriteCache annotation, Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        CacheKey key = annotation.key();
        String cacheKey = CacheUtils.parseCacheKey(key, args);
        int expire = CacheUtils.getExpire(annotation.expireTime(), annotation.expire());
        try {
            if (annotation.writeParameter()) {
                Object value = getParameterValue(annotation.parameterIndex(), args);
                writeToCache(cacheKey, value, expire, annotation.cacheNull(), annotation.type());
            }
        } catch (Exception e) {
            logger.info("write listed parm cache error:" + e.getCause());
        }
        Object returnVal = proxy.invokeSuper(obj, args);
        try {
            if (annotation.writeReturn()) {
                writeToCache(cacheKey, returnVal, expire, annotation.cacheNull(), annotation.type());
            }
        } catch (Exception e) {
            logger.info("write listed return cache error:" + e.getCause());
        }
        return returnVal;
    }


//    private Object invokeSuper(Object obj, Object[] args, MethodProxy proxy) throws Throwable {
//        return proxy.invokeSuper(obj, args);
//    }

    @Override
    public void removeCache(RemoveCache annotation, Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        CacheKey[] keys = annotation.key();
        for (CacheKey key : keys) {
            String cacheKey = CacheUtils.parseCacheKey(key, args);
            int expire = CacheUtils.getExpire(annotation.expireTime(), annotation.expire());
            if (annotation.markAsNull()) {
                writeToCache(cacheKey, new NullCachedObject(), expire, false, annotation.type());
            } else {
                removeFromCache(cacheKey, annotation.type());
            }
        }
    }

    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        boolean pushed;
        pushed = !(firstInvoke.get().isEmpty() || firstInvoke.get().peek());
        firstInvoke.get().push(pushed);
        try {
            Annotation[] annotations = method.getAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation instanceof SimpleCache) {
                    SimpleCache a = (SimpleCache) annotation;
                    return doSimpleCache(a, obj, method, args, proxy);
                } else if (annotation instanceof Cache) {
                    Cache a = (Cache) annotation;
                    return doCache(a, obj, method, args, proxy);
                } else if (annotation instanceof ListedCache) {
                    return doListCache((ListedCache) annotation, obj, method, args, proxy);
                } else if (annotation instanceof WriteListCache) {
                    return writeListCache((WriteListCache) annotation, obj, method, args, proxy);
                } else if (annotation instanceof WriteCache) {
                    return writeCache((WriteCache) annotation, obj, method, args, proxy);
                } else if (annotation instanceof RemoveCache) {
                     removeCache((RemoveCache) annotation, obj, method, args, proxy);
                }
            }
            return proxy.invokeSuper(obj, args);
        } finally {
            firstInvoke.get().pop();
        }


    }


    private static final ThreadLocal<Stack<Boolean>> firstInvoke = new ThreadLocal<Stack<Boolean>>() {
        @Override
        protected Stack<Boolean> initialValue() {
            return new Stack<Boolean>();
        }
    };

    public boolean isEnableLocalCache() {
        return enableLocalCache;
    }

    public void setEnableLocalCache(boolean enableLocalCache) {
        this.enableLocalCache = enableLocalCache;
    }
}
