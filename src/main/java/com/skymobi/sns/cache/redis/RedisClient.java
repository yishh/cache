package com.skymobi.sns.cache.redis;


import com.google.common.collect.Lists;
import com.google.common.primitives.Chars;
import com.skymobi.sns.cache.route.KeyRouter;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.springframework.util.ClassUtils;
import org.springframework.util.SerializationUtils;
import redis.clients.jedis.BinaryClient.LIST_POSITION;
import redis.clients.jedis.*;
import redis.clients.util.Pool;
import redis.clients.util.SafeEncoder;

import java.util.*;


public class RedisClient {
    private List<JedisShardInfo> shards;
    private List<Jedis> jedis;
    private ShardedJedisPool defaultPool;

    private Map<String, JedisPool> jedisPoolMap = new HashMap<String, JedisPool>();

    private KeyRouter keyRouter;

    public RedisClient() {
    }

    public RedisClient(String addresses) {
        this(addresses, null);
    }

    public RedisClient(String addresses, GenericObjectPool.Config poolConfig) {
        shards = new ArrayList<JedisShardInfo>();
        String[] addressArray = addresses.split(" ");
        jedis = new ArrayList<Jedis>();
        for (String address : addressArray) {
            String[] parts = address.split(":");
            JedisShardInfo si = new JedisShardInfo(parts[0], Integer.parseInt(parts[1]));
            shards.add(si);
            jedis.add(new Jedis(parts[0], Integer.parseInt(parts[1])));
        }
        if (poolConfig == null) {
            poolConfig = getDefaultPoolConfig();
        }
        defaultPool = new ShardedJedisPool(poolConfig, shards);

    }

    public RedisClient(KeyRouter keyRouter) {
       this(keyRouter, null);
    }

    public RedisClient(KeyRouter keyRouter, GenericObjectPool.Config poolConfig) {
        this.keyRouter = keyRouter;
        if (poolConfig == null) {
            poolConfig = getDefaultPoolConfig();
        }
        for (String host : this.keyRouter.getHosts()) {
            JedisPool pool = getJedisPool(poolConfig, host);
            jedisPoolMap.put(host, pool);
        }
        if (!jedisPoolMap.containsKey(keyRouter.getDefaultHost())) {
            jedisPoolMap.put(keyRouter.getDefaultHost(), getJedisPool(poolConfig, keyRouter.getDefaultHost()));
        }
    }


    private JedisPool getJedisPool(GenericObjectPool.Config poolConfig, String host) {
        JedisPool pool;
        int passwordSplit = host.indexOf("/");
        if (passwordSplit == -1) {
            String[] parts = host.split(":");
            pool = new JedisPool(poolConfig, parts[0], Integer.parseInt(parts[1]));
        } else {
            String[] parts = host.substring(0, passwordSplit).split(":");
            String password = host.substring(passwordSplit + 1);
            pool = new JedisPool(poolConfig, parts[0], Integer.parseInt(parts[1]), Protocol.DEFAULT_TIMEOUT, password);
        }
        return pool;
    }

    private GenericObjectPool.Config getDefaultPoolConfig() {
        GenericObjectPool.Config poolConfig = new GenericObjectPool.Config();
        poolConfig.testWhileIdle = true;
        poolConfig.minEvictableIdleTimeMillis = 60000;
        poolConfig.timeBetweenEvictionRunsMillis = 30000;
        poolConfig.numTestsPerEvictionRun = -1;
        return poolConfig;
    }

    public ShardedJedisPool getPool() {
        return defaultPool;
    }
    //在队列尾部增加
    public <T> long rpush(String key, T o) {
        final byte[] keyByte = toByte(key, true);
        final byte[] value = toByte(o, false);
        return execTask(key, new BinaryJedisRunnable<Long>() {
            @Override
            public Long run(BinaryJedisCommands jedis) {
                return jedis.rpush(keyByte, value);
            }
        });
    }

    public <T> String setex(String key, int time, T o) {
        final byte[] keyByte = toByte(key, true);
        final byte[] value = toByte(o, false);
        final int alive = time;
        return execTask(key, new BinaryJedisRunnable<String>() {
            @Override
            public String run(BinaryJedisCommands jedis) {

                return jedis.setex(keyByte, alive, value);
            }
        });
    }

    public long remove(final String key) {
        if (jedisPoolMap.isEmpty() && keyRouter == null) {
            ShardedJedis shardedJedis = defaultPool.getResource();
            try {
                return shardedJedis.del(key);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                defaultPool.returnResource(shardedJedis);
            }
        } else {
            String host = keyRouter.getHost(key);
            JedisPool pool = jedisPoolMap.get(host);
            Jedis jedis = pool.getResource();
            try {
                return jedis.del(key);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                pool.returnResource(jedis);
            }
        }
        return -1;
    }

    public boolean exists(final String key) {
        final byte[] keyByte = toByte(key, true);
        return execTask(key, new BinaryJedisRunnable<Boolean>() {
            @Override
            public Boolean run(BinaryJedisCommands jedis) {
                return jedis.exists(keyByte);
            }
        });

    }

    //再头部添加元素
    public <T> long lpush(String key, T o) {
        final byte[] keyByte = toByte(key, true);
        final byte[] value = toByte(o, false);
        return execTask(key, new BinaryJedisRunnable<Long>() {
            @Override
            public Long run(BinaryJedisCommands jedis) {
//            	jedis.
                return jedis.lpush(keyByte, value);
            }
        });
    }
    //  add by whz  出队列  添加  丛尾部溢出元素
    public <T> T rpop(String key, final Class<T> c) {
        final byte[] keyByte = toByte(key, true);
        return execTask(key, new BinaryJedisRunnable<T>() {
            @Override
            public T run(BinaryJedisCommands jedis) {

                byte[] value = jedis.rpop(keyByte);
                return (T) fromByte(value, c);
            }
        });
    }
    
    //头部溢出元素
    public <T> T lpop(String key, final Class<T> c) {
        final byte[] keyByte = toByte(key, true);
        return execTask(key, new BinaryJedisRunnable<T>() {
            @Override
            public T run(BinaryJedisCommands jedis) {

                byte[] value = jedis.lpop(keyByte);
                return (T) fromByte(value, c);
            }
        });
    }
    

    //添加元素set中
    public <T> long sadd(String key, T o) {
        final byte[] keyByte = toByte(key, true);
        final byte[] value = toByte(o, false);
        return execTask(key, new BinaryJedisRunnable<Long>() {
            @Override 
            public Long run(BinaryJedisCommands jedis) {
                return jedis.sadd(keyByte, value);
            }
        });
    }
    
    public <T> long hset(String key,Integer hash, T o) {
        final byte[] keyByte = toByte(key, true);
        final byte[] value = toByte(o, false);
        final byte[] hashByte = toByte(hash, false);
        return execTask(key, new BinaryJedisRunnable<Long>() {
            @Override 
            public Long run(BinaryJedisCommands jedis) {
                return jedis.hset(keyByte, hashByte, value);
            }
        });
    }
    
    
    public <T> T hget(String key,Integer hash, final Class<T> c) {
        final byte[] keyByte = toByte(key, true);
        final byte[] hashByte = toByte(hash, false);
        return execTask(key, new BinaryJedisRunnable<T>() {
            @Override
            public T run(BinaryJedisCommands jedis) {
                byte[] value = jedis.hget(keyByte,hashByte);
                return (T) fromByte(value, c);
            }
        });
    }
    
    public <T> long zadd(String key, T o,final double order) {
        final byte[] keyByte = toByte(key, true);
        final byte[] value = toByte(o, false);
        return execTask(key, new BinaryJedisRunnable<Long>() {
            @Override 
            public Long run(BinaryJedisCommands jedis) {
                return jedis.zadd(keyByte,order,value);
            }
        });
    }
    
    
    //删除指定key  元素
    public <T> long srem(String key, T o) {
        final byte[] keyByte = toByte(key, true);
        final byte[] value = toByte(o, false);
        return execTask(key, new BinaryJedisRunnable<Long>() {
            @Override 
            public Long run(BinaryJedisCommands jedis) {
                return jedis.srem(keyByte, value);
            }
        });
    }
    
    
    public long hdel(String key,int hash) {
        final byte[] keyByte = toByte(key, true);
        final byte[] hashByte = toByte(hash, false);
        return execTask(key, new BinaryJedisRunnable<Long>() {
            @Override 
            public Long run(BinaryJedisCommands jedis) {
                return jedis.hdel(keyByte, hashByte);
            }
        });
    }
    
    //删除指定key  元素
    public <T> long zrem(String key, T o) {
        final byte[] keyByte = toByte(key, true);
        final byte[] value = toByte(o, false);
        return execTask(key, new BinaryJedisRunnable<Long>() {
            @Override 
            public Long run(BinaryJedisCommands jedis) {
                return jedis.zrem(keyByte, value);
            }
        });
    }
     
    //返回所有元素
    public <T> List<T> smembers(String key, final Class<T> c) {
        final byte[] keyByte = toByte(key, true);
        return execTask(key, new BinaryJedisRunnable<List<T>>() {
            @Override
            public List<T> run(BinaryJedisCommands jedis) {
                Set<byte[]> set = jedis.smembers(keyByte);
                List<byte[]> list =  new ArrayList<byte[]>();
                list.addAll(set);
                return fromByte(list, c);
            }
        });
    }
    
    public <T> List<T> hgetAll(String key, final Class<T> c) {
        final byte[] keyByte = toByte(key, true);
        return execTask(key, new BinaryJedisRunnable<List<T>>() {
            @Override
            public List<T> run(BinaryJedisCommands jedis) {
                Map<byte[],byte[]> map = jedis.hgetAll(keyByte);
                List<byte[]> list =  new ArrayList<byte[]>();
                list.addAll(map.values());
                return fromByte(list, c);
            }
        });
    }
    
    
    public <T> List<T> zrevrange(String key, final Class<T> c,final int start,final int end) {
        final byte[] keyByte = toByte(key, true);
        return execTask(key, new BinaryJedisRunnable<List<T>>() {
            @Override
            public List<T> run(BinaryJedisCommands jedis) {
                Set<byte[]> set = jedis.zrevrange(keyByte, start, end);
                List<byte[]> list =  new ArrayList<byte[]>();
                list.addAll(set);
                return fromByte(list, c);
            }
        });
    }
    
//    /**
//     * blpop:阻塞
//     * @param  @param key
//     * @param  @param c
//     * @param  @return    设定文件
//     * @return List<T>    DOM对象
//     * @throws
//     * @since  CodingExample　Ver 1.1
//    */
//    public <T> T blpop(String key, final Class<T> c) {
//        final byte[] keyByte = toByte(key, true);
//        List<byte []> list = jedis.get(0).blpop(0, keyByte);
//        return (T) fromByte(list.get(1), c);
//    }
    
    
    //查看set长度
    public long scard(String key) {
        final byte[] keyByte = toByte(key, true);
        return execTask(key, new BinaryJedisRunnable<Long>() {
            @Override 
            public Long run(BinaryJedisCommands jedis) {
                return jedis.scard(keyByte);
            }
        });
    }
    
    
    public long zcard(String key) {
        final byte[] keyByte = toByte(key, true);
        return execTask(key, new BinaryJedisRunnable<Long>() {
            @Override 
            public Long run(BinaryJedisCommands jedis) {
                return jedis.zcard(keyByte);
            }
        });
    }
    
    
    public long hlen(String key) {
        final byte[] keyByte = toByte(key, true);
        return execTask(key, new BinaryJedisRunnable<Long>() {
            @Override 
            public Long run(BinaryJedisCommands jedis) {
                return jedis.hlen(keyByte);
            }
        });
    }
    
    
    
    //end  add by whz

    public String ltrim(String key, final int start, final int end) {
        final byte[] keyByte = toByte(key, true);
        return execTask(key, new BinaryJedisRunnable<String>() {
            @Override
            public String run(BinaryJedisCommands jedis) {
                return jedis.ltrim(keyByte, start, end);
            }
        });
    }

    public <T> long lrem(String key, final int count, T o) {
        final byte[] keyByte = toByte(key, true);
        final byte[] value = toByte(o, false);
        return execTask(key, new BinaryJedisRunnable<Long>() {
            @Override
            public Long run(BinaryJedisCommands jedis) {
                return jedis.lrem(keyByte, count, value);
            }
        });
    }

    public long incrBy(String key, final long count) {
        final byte[] keyByte = toByte(key, true);
        return execTask(key, new BinaryJedisRunnable<Long>() {
            @Override
            public Long run(BinaryJedisCommands jedis) {
                return jedis.incrBy(keyByte, count);
            }
        });
    }

    public long decrBy(String key, final long count) {
        final byte[] keyByte = toByte(key, true);
        return execTask(key, new BinaryJedisRunnable<Long>() {
            @Override
            public Long run(BinaryJedisCommands jedis) {
                return jedis.decrBy(keyByte, count);
            }
        });
    }


    public void pipelinePushAndTrim(final Map<String, Collection<Object>> kv, final int start, final int end) {
        for (Map.Entry<String, Collection<Object>> entry : kv.entrySet()) {
            String key = entry.getKey();
            final Collection values = entry.getValue();
            final byte[] keyByte = toByte(key, true);
            execTask(key, new PipelineRunnable() {
                @Override
                public List<Response> run(Jedis jedis) {
                    Pipeline pipeline = jedis.pipelined();
                    for (Object v : values) {
                        byte[] value = toByte(v, false);
                        pipeline.lpush(keyByte, value);
                    }

                    pipeline.ltrim(keyByte, start, end);
                    pipeline.sync();
                    return null;
                }
            });
        }
    }

    public <T> List<Object> lPushAndTrim(String key, T o, final int start, final int end) {
        final byte[] keyByte = toByte(key, true);
        final byte[] value = toByte(o, false);
        List<Response> responses =
                execTask(key, new PipelineRunnable() {
                    @Override
                    public List<Response> run(Jedis jedis) {
                        Pipeline pipeline =
                                jedis.pipelined();
                        Response response1 = pipeline.lpush(keyByte, value);
                        Response response2 = pipeline.ltrim(keyByte, start, end);
                        pipeline.sync();
                        return Lists.newArrayList(response1, response2);

                    }
                });
        List<Object> objects = new ArrayList<Object>();
        for (Response response : responses) {
            objects.add(response.get());
        }
        return objects;

    }

    public <T> List<T> lrange(String key, final int offset, final int limit, final Class<T> c) {
        final byte[] keyByte = toByte(key, true);
        return execTask(key, new BinaryJedisRunnable<List<T>>() {
            @Override
            public List<T> run(BinaryJedisCommands jedis) {
                List<byte[]> list = jedis.lrange(keyByte, offset, offset + limit - 1);
                return fromByte(list, c);
            }
        });
    }

    public long expire(String key, final int seconds) {
        final byte[] keyByte = toByte(key, true);
        return execTask(key, new BinaryJedisRunnable<Long>() {
            @Override
            public Long run(BinaryJedisCommands jedis) {
                return jedis.expire(keyByte, seconds);

            }
        });
    }

    public <T> String set(String key, T o) {
        final byte[] keyByte = toByte(key, true);
        final byte[] value = toByte(o, false);
        return execTask(key, new BinaryJedisRunnable<String>() {
            @Override
            public String run(BinaryJedisCommands jedis) {
                return jedis.set(keyByte, value);
            }
        });
    }

    public <T> T get(String key, final Class<T> c) {
        final byte[] keyByte = toByte(key, true);
        return execTask(key, new BinaryJedisRunnable<T>() {
            @Override
            public T run(BinaryJedisCommands jedis) {

                byte[] value = jedis.get(keyByte);
                return (T) fromByte(value, c);
            }
        });
    }

    public <T> long linsert(String key, T opivot, T ovalue) {
        final byte[] keyByte = toByte(key, true);
        final byte[] pivot = toByte(opivot, false);
        final byte[] value = toByte(ovalue, false);
        return execTask(key, new BinaryJedisRunnable<Long>() {
            @Override
            public Long run(BinaryJedisCommands jedis) {
                return jedis.linsert(keyByte, LIST_POSITION.AFTER, pivot, value);
            }
        });
    }

    public <T> String lset(String key, int index, T ovalue) {
        final byte[] keyByte = toByte(key, true);
        final byte[] value = toByte(ovalue, false);
        final int indexFinal = index;
        return execTask(key, new BinaryJedisRunnable<String>() {
            @Override
            public String run(BinaryJedisCommands jedis) {
                return jedis.lset(keyByte, indexFinal, value);
            }
        });
    }

    public static <T> byte[] toByte(T o, boolean isKey) {
        if (isKey || o instanceof String) {
            return SafeEncoder.encode((String) o);
        }
        if (o == null) {
            return new byte[]{};
        }

        Class c = o.getClass();
        if (ClassUtils.isPrimitiveOrWrapper(c)) {
            if (c.equals(Long.class) || c.equals(long.class)) {
                return SafeEncoder.encode(Long.toString((Long) o));
            }
            if (c.equals(Boolean.class) || c.equals(boolean.class)) {
                Boolean b = (Boolean) o;
                if (b) {
                    return new byte[]{1};
                } else {
                    return new byte[]{0};
                }
            }
            if (c.equals(Byte.class) || c.equals(byte.class)) {
                byte[] b = new byte[1];
                b[0] = (Byte) o;
                return b;
            }
            if (c.equals(Character.class) || c.equals(char.class)) {
                return Chars.toByteArray((Character) o);
            }
            if (c.equals(Integer.class) || c.equals(int.class)) {
                return SafeEncoder.encode(Integer.toString((Integer) o));
            }
            if (c.equals(Short.class) || c.equals(short.class)) {
                return SafeEncoder.encode(Short.toString((Short) o));
            }
        }
        return SerializationUtils.serialize(o);
    }

    <T> List<T> fromByte(Collection<byte[]> buffers, Class<T> c) {
        List<T> result = new ArrayList<T>();
        for (byte[] buffer : buffers) {
            T o = (T) fromByte(buffer, c);
            result.add(o);
        }
        return result;

    }


    Object fromByte(byte[] bytes, Class c) {

        if (ClassUtils.isPrimitiveOrWrapper(c)) {
            if (c.equals(Long.class) || c.equals(long.class)) {
                if (bytes == null || bytes.length == 0) {
                    return null;
                }
                return Long.parseLong(SafeEncoder.encode(bytes));
            }
            if (c.equals(Boolean.class) || c.equals(boolean.class)) {
                if (bytes == null || bytes.length == 0) {
                    return null;
                }
                return bytes[0] == 1;
            }
            if (c.equals(Byte.class) || c.equals(byte.class)) {
                if (bytes == null || bytes.length == 0) {
                    return null;
                }
                return bytes[0];
            }
            if (c.equals(Character.class) || c.equals(char.class)) {
                if (bytes == null || bytes.length == 0) {
                    return null;
                }
                return Chars.fromByteArray(bytes);
            }
            if (c.equals(Integer.class) || c.equals(int.class)) {
                if (bytes == null || bytes.length == 0) {
                    return null;
                }
                return Integer.parseInt(SafeEncoder.encode(bytes));
            }
            if (c.equals(Short.class) || c.equals(short.class)) {
                if (bytes == null || bytes.length == 0) {
                    return null;
                }
                return Short.parseShort(SafeEncoder.encode(bytes));
            }
        }
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        if (c.equals(String.class)) {
            return SafeEncoder.encode(bytes);
        }

        return SerializationUtils.deserialize(bytes);
    }

    public List<Response> execTask(String key, PipelineRunnable task) {
        Jedis jedis;
        Pool pool;
        if (jedisPoolMap.isEmpty() && keyRouter == null) {
            pool = defaultPool;
            ShardedJedis shardedJedis = defaultPool.getResource();
            jedis = shardedJedis.getShard(key);
            try {
                return task.run(jedis);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                pool.returnResource(shardedJedis);
            }
        } else {
            String host = keyRouter.getHost(key);
            pool = jedisPoolMap.get(host);
            jedis = (Jedis) pool.getResource();
            try {
                return task.run(jedis);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                pool.returnResource(jedis);
            }

        }
        return null;
    }

    public <T> T execTask(String key, JedisRunnable<T> task) {
        JedisCommands jedis;
        Pool pool;
        if (jedisPoolMap.isEmpty() && keyRouter == null) {
            pool = defaultPool;
        } else {
            String host = keyRouter.getHost(key);
            pool = jedisPoolMap.get(host);
        }
        jedis = (JedisCommands) pool.getResource();
        try {
            return task.run(jedis);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.returnResource(jedis);
        }
        return null;
    }

    public <T> T execTask(String key, BinaryJedisRunnable<T> task) {
        BinaryJedisCommands jedis;
        Pool pool;
        if (jedisPoolMap.isEmpty() && keyRouter == null) {
            pool = defaultPool;
        } else {
            String host = keyRouter.getHost(key);
            pool = jedisPoolMap.get(host);
        }
        jedis = (BinaryJedisCommands) pool.getResource();
        try {
            return task.run(jedis);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.returnResource(jedis);
        }
        return null;
    }

    public void destroy() {
        if (defaultPool != null) {
            defaultPool.destroy();
        }
    }


    public Long zadd(String key, String member, final double value) {
        final byte[] keyByte = toByte(key, true);
        final byte[] memberKey = toByte(member, true);
        return execTask(key, new BinaryJedisRunnable<Long>() {
            @Override
            public Long run(BinaryJedisCommands jedis) {
                return jedis.zadd(keyByte, value, memberKey);
            }
        });
    }

    public double zscore(String key, String member) {
        final byte[] keyByte = toByte(key, true);
        final byte[] memberKey = toByte(member, true);
        return execTask(key, new BinaryJedisRunnable<Double>() {
            @Override
            public Double run(BinaryJedisCommands jedis) {
                return jedis.zscore(keyByte, memberKey);
            }
        });
    }

    //新增人气
    public Double zincrby(String key, String group, final double value) {
        final byte[] keyByte = toByte(key, true);
        final byte[] groupKey = toByte(group, true);
        return execTask(key, new BinaryJedisRunnable<Double>() {
            @Override
            public Double run(BinaryJedisCommands jedis) {
                return jedis.zincrby(keyByte, value, groupKey);
            }
        });
    }

    //获取hot group排名
    public List<String> zrevrange(String key, final int start, final int end) {
        final byte[] keyByte = toByte(key, true);
        if (start > end) {
            return null;
        }
        Set<byte[]> sets = execTask(key, new BinaryJedisRunnable<Set<byte[]>>() {
            @Override
            public Set<byte[]> run(BinaryJedisCommands jedis) {
                return jedis.zrevrange(keyByte, start, end);
            }
        });

        return fromByte(sets, String.class);

    }


    //根据GroupId获取排名

    public Long zrevrank(String key, String group) {
        final byte[] keyByte = toByte(key, true);
        final byte[] groupKey = toByte(group, true);
        return execTask(key, new BinaryJedisRunnable<Long>() {
            @Override
            public Long run(BinaryJedisCommands jedis) {
                return jedis.zrevrank(keyByte, groupKey);
            }
        });
    }

    public static String byteToStr(byte[] b) {
        return new String(b);

    }


    public KeyRouter getKeyRouter() {
        return keyRouter;
    }


    public Map<String, JedisPool> getJedisPoolMap() {
        return jedisPoolMap;
    }


}
