package com.skymobi.sns.cache.redis;

import redis.clients.jedis.BinaryJedisCommands;
import redis.clients.jedis.JedisCommands;

/**
 * Created by IntelliJ IDEA.
 * User: liweijing
 * Date: 11-7-29
 * Time: 下午1:53
 * To change this template use File | Settings | File Templates.
 */

public  interface BinaryJedisRunnable<T>{
    T  run(BinaryJedisCommands jedis);
}
