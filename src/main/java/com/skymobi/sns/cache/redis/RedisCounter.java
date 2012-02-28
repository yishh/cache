package com.skymobi.sns.cache.redis;

import com.skymobi.sns.cache.AbstractCounter;

/**
 * Created by IntelliJ IDEA.
 * User: liweijing
 * Date: 11-9-13
 * Time: 下午4:58
 * To change this template use File | Settings | File Templates.
 */
public class RedisCounter extends AbstractCounter {
    private RedisClient client;


    @Override
    public long incr(String key) {
        return incr(key, 1, 1);
    }

    @Override
    public long decr(String key) {
        return decr(key, 1, 0);
    }

    @Override
    public long incr(String key, int value, int initValue) {
        key = makeKey(key);
        long v = client.incrBy(key, value);
        if (initValue > 0 && v < 1) {
            return client.incrBy(key, initValue);
        }
        return v;
    }

    @Override
    public long decr(String key, int value, int initValue) {
        key = makeKey(key);
        long v = client.decrBy(key, value);
        if (initValue > 0 || v <= 0) {
            client.set(key, initValue);
            if (initValue > value)
                client.decrBy(key, value);
            v = initValue;
        }
        return v;
    }

    @Override
    public long get(String key) {
        key = makeKey(key);
        return client.get(key, Long.class);
    }

    @Override
    public void set(String key, long value) {
        key = makeKey(key);
        client.set(key, value);
    }

    @Override
    public void remove(String key) {
        key = makeKey(key);
        client.remove(key);
    }

    @Override
    public boolean exists(String key) {

        key = makeKey(key);
        return client.exists(key);
    }


    public RedisClient getClient() {
        return client;
    }

    public void setClient(RedisClient client) {
        this.client = client;
    }
}
