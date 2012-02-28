package com.skymobi.sns.cache.tools;

import redis.clients.jedis.Jedis;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: liweijing
 * Date: 11-11-25
 * Time: 下午3:22
 * To change this template use File | Settings | File Templates.
 */
public class RedisCleanTool {

    public static void clean(String host, String keyPattern) {
        Jedis jedis = new Jedis(host.split(":")[0], Integer.parseInt(host.split(":")[1]), 60000);
        Set<String> keys = jedis.keys(keyPattern);
        System.out.println("total:" + keys.size());
        for (String key : keys) {
            jedis.del(key);
            System.out.println("delete [" + key + "] , from " + host);
        }

    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("java -classpath cache.jar com.skymobi.sns.cache.tools.RedisCleanTool host  key-pattern");
            return;
        }
        String host = args[0];
        String keyPattern = args[1];
        clean(host, keyPattern);
    }
}
