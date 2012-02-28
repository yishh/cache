#!/usr/bin/env groovy

@Grapes([
@Grab('redis.clients:jedis:2.0.0'),
@GrabConfig(systemClassLoader = true)
])


import redis.clients.jedis.Jedis
import redis.clients.util.SafeEncoder


def copyString(key, fromJedis, toJedis) {
    try {
        byte[] keyByte = SafeEncoder.encode((String) o)
        byte[] vByte = fromJedis.get(keyByte)
        toJedis.set(keyByte, vByte)
    } catch (Exception e) {
        e.printStackTrace()
    }
}
def copyList(key, fromJedis, toJedis) {
    try {
        String v = fromJedis.lpop(key)
        while (v != null) {
            toJedis.rpush(key, v)
            v = fromJedis.lpop(key)
        }
    } catch (Exception e) {
        e.printStackTrace()
    }
}

def copySet(key, fromJedis, toJedis) {
    try {
        Set<String> members = fromJedis.smembers(key)
        for (String m: members) {
            toJedis.sadd(key, m)
        }
    } catch (Exception e) {
        e.printStackTrace()
    }
}

def copyHash(key, fromJedis, toJedis) {
    try {
        Map<String, String> hashs = fromJedis.hgetAll(key)
        for (String k: hashs.keySet()) {
            toJedis.hset(key, k, hashs.get(k))
        }
    } catch (Exception e) {
        e.printStackTrace()
    }
}

def copyZSet(key, fromJedis, toJedis) {
    try {
        Set<String> members = fromJedis.zrange(key, 0, -1)
        for (String m: members) {
            double v = fromJedis.zscore(key, m)
            toJedis.zadd(key, v, m)
        }
    } catch (Exception e) {
        e.printStackTrace()
    }
}
def copy(fromHost,  toHost, keyPattern) {
    Jedis fromJedis = new Jedis(fromHost.split(":")[0], Integer.parseInt(fromHost.split(":")[1]))
    Jedis toJedis = new Jedis(toHost.split(":")[0], Integer.parseInt(toHost.split(":")[1]))
    def keys = fromJedis.keys(keyPattern)
    println "total: ${keys.size()}"
    for (String key: keys) {
        String type = fromJedis.type(key)
        println "copy  [${key}] type [${type} ], from ${fromHost}   to   ${toHost}"
        if ("string".equals(type)) {
            copyString(key, fromJedis, toJedis)
        } else if ("list".equals(type)) {
            copyList(key, fromJedis, toJedis)
        } else if ("set".equals(type)) {
            copySet(key, fromJedis, toJedis)
        } else if ("hash".equals(type)) {
            copyHash(key, fromJedis, toJedis)
        } else if ("zset".equals(type)) {
            copyZSet(key, fromJedis, toJedis)
        } else {
            println  "unknow data type: ${type}"
        }

        fromJedis.del(key);
        println "delete  [${key}] type [${type} ], from ${fromHost}"

    }

}


if(this.args.size() <3){
    println "./redis_copy.groovy fromhost tohost key-pattern"
    return
}

String fromHost = this.args[0]
String toHost = this.args[1]
String keyPattern = this.args[2]
copy(fromHost, toHost, keyPattern)
