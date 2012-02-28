package com.skymobi.sns.cache.tools;

import com.skymobi.sns.cache.redis.RedisClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: liweijing
 * Date: 11-11-23
 * Time: 下午5:44
 * To change this template use File | Settings | File Templates.
 */
public class RedisReplicationTool {
    private static void copyString(String key, Jedis fromJedis, Jedis toJedis) {
        try {

            byte[] keyByte = RedisClient.toByte(key, true) ;
            byte[] vByte =  fromJedis.get(keyByte);
            toJedis.set(keyByte, vByte);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void copyList(String key, Jedis fromJedis, Jedis toJedis) {
        try {
            String v = fromJedis.lpop(key);
            while (v != null) {
                toJedis.rpush(key, v);
                v = fromJedis.lpop(key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void copySet(String key, Jedis fromJedis, Jedis toJedis) {
        try {
            Set<String> members = fromJedis.smembers(key);
            for (String m : members) {
                toJedis.sadd(key, m);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void copyHash(String key, Jedis fromJedis, Jedis toJedis) {
        try {
            Map<String, String> hashs = fromJedis.hgetAll(key);
            for (String k : hashs.keySet()) {
                toJedis.hset(key, k, hashs.get(k));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void copyZSet(String key, Jedis fromJedis, Jedis toJedis) {
        try {
            Set<String> members = fromJedis.zrange(key, 0, -1);
            for (String m : members) {
                double v = fromJedis.zscore(key, m);
                toJedis.zadd(key, v, m);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void copy(String fromHost, String toHost, String keyPattern) {
        Jedis fromJedis = new Jedis(fromHost.split(":")[0], Integer.parseInt(fromHost.split(":")[1]));
        Jedis toJedis = new Jedis(toHost.split(":")[0], Integer.parseInt(toHost.split(":")[1]));

        for (String key : fromJedis.keys(keyPattern)) {
            String type = fromJedis.type(key);
            System.out.println("copy  [" + key + "] type [" + type + "], from " + fromHost + "  to  " + toHost);
            if ("string".equals(type)) {
                copyString(key, fromJedis, toJedis);
            } else if ("list".equals(type)) {
                copyList(key, fromJedis, toJedis);
            } else if ("set".equals(type)) {
                copySet(key, fromJedis, toJedis);
            } else if ("hash".equals(type)) {
                copyHash(key, fromJedis, toJedis);
            } else if ("zset".equals(type)) {
                copyZSet(key, fromJedis, toJedis);
            } else {
                System.err.println("unknow data type: " + type);
            }

            fromJedis.del(key);
            System.out.println("delete [" + key + "] type [" + type + "], from " + fromHost );
        }

    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("java -classpath cache.jar com.skymobi.sns.cache.tools.RedisReplicationTool fromhost tohost key-pattern");
            return;
        }
        String fromHost = args[0];
        String toHost = args[1];
        String keyPattern = args[2];
        copy(fromHost, toHost, keyPattern);
    }
}
