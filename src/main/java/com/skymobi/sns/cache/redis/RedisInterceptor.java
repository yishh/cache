package com.skymobi.sns.cache.redis;

import com.skymobi.sns.cache.AbstractInterceptor;
import com.skymobi.sns.cache.NullCachedObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: liweijing
 * Date: 11-8-3
 * Time: 下午3:22
 * To change this template use File | Settings | File Templates.
 */
public class RedisInterceptor extends AbstractInterceptor {
    Logger logger = LoggerFactory.getLogger(RedisInterceptor.class);
    protected RedisClient masterClient;

    protected RedisClient slaveClient;

    public RedisInterceptor(RedisClient client) {
        this.masterClient = client;
        this.slaveClient = client;
    }


    public RedisInterceptor(RedisClient masterClient, RedisClient slaveClient) {
        this.masterClient = masterClient;
        this.slaveClient = slaveClient;
    }


    protected Object getParameterValue(int index, Object[] parameters) {

        return parameters[index];
    }

    @Override
    public Object loadFromCache(String key, Class<?> c, String cacheType) {
        return slaveClient.get(key, c);
    }

    private void write(String key, Object value, int expire) {
        logger.debug("set cache : " + key);
        masterClient.set(key, value);
        if (expire > 0) {
            masterClient.expire(key, expire);
        }
    }

    @Override
    public void writeToCache(String cacheKey, Object value, int expire, boolean cacheNull, String cacheType) {
        if (value != null) {
            write(cacheKey, value, expire);
        } else {
            if (cacheNull) {
                write(cacheKey, new NullCachedObject(), expire);
            }
        }
    }

    @Override
    public void writeListToCache(String cacheKey, Object value, boolean append, int max, String cacheType) {
        long curNum = 0;
        if (value instanceof Iterable) {
            for (Object o : (Iterable) value) {
                if (append) {
                    curNum = masterClient.rpush(cacheKey, o);
                } else {
                    masterClient.lpush(cacheKey, o);
                }
            }
        } else {
            if (append) {
                curNum = masterClient.rpush(cacheKey, value);
            } else {
                masterClient.lpush(cacheKey, value);
            }
        }
        if (append) {
            if (curNum > max)
                masterClient.ltrim(cacheKey, (int) (curNum - max), (int) curNum);
        } else {
            masterClient.ltrim(cacheKey, 0, max);
        }
    }

    @Override
    public List<?> loadListFromCache(String key, int offset, int limit, Class<?> c, String cacheType) {
        return slaveClient.lrange(key, offset, limit, c);
    }

    @Override
    public void removeFromCache(String key, String cacheType) {
        masterClient.remove(key);
    }


    public RedisClient getMasterClient() {
        return masterClient;
    }

    public void setMasterClient(RedisClient masterClient) {
        this.masterClient = masterClient;
    }

    public RedisClient getSlaveClient() {
        return slaveClient;
    }

    public void setSlaveClient(RedisClient slaveClient) {
        this.slaveClient = slaveClient;
    }

}
