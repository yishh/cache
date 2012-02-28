#!/usr/bin/env groovy
@Grapes([
@Grab('redis.clients:jedis:2.0.0'),
@GrabConfig(systemClassLoader=true)
])

def args = this.args
//println "clean key from redis"

if( args.size() < 3 ) {
    println "usage: ./redis_clean.groovy host port key-pattern"
    return
}

def host = args[0]
def port = args[1]
def keyPattern =  args[2]

import redis.clients.jedis.*

Jedis jedis = new Jedis(host, port, 60000)

def keys = jedis.keys(keyPattern)
println "total: ${keys.size()}"
keys.each{ key ->
    def type = jedis.type(key)
    if(!"string".equals(type)){
        println "[${key}] not simple type, don't delete"
    }else{
        jedis.del(key)
        println "delete [${key}]"
    }
}