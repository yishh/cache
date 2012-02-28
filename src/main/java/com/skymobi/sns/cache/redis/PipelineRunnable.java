package com.skymobi.sns.cache.redis;

import redis.clients.jedis.BinaryJedisCommands;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Response;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: liweijing
 * Date: 11-11-23
 * Time: 下午5:00
 * To change this template use File | Settings | File Templates.
 */
public interface PipelineRunnable {
    List<Response> run(Jedis jedis);
}
