package com.skymobi.sns.cache.redis;

import redis.clients.jedis.BinaryJedisCommands;
import redis.clients.jedis.JedisCommands;

/**
 * Created by IntelliJ IDEA.
 * User: liweijing
 * Date: 11-11-23
 * Time: 下午5:25
 * To change this template use File | Settings | File Templates.
 */
public interface JedisRunnable<T> {
      T  run(JedisCommands jedis);
}
