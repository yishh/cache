package com.skymobi.sns.cache.redis;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * User: thor
 * Date: 12-12-21
 * Time: 上午10:31
 */
public interface AsyncRedisClient {
    //connect commands
    Future<String> auth(String password);

    Future<String> echo(String message);

    Future<String> ping();

    Future<String> select(int db);

    Future<String> quit();

    //keys commands
    Future<Integer> delete(String key);

    Future<Integer> delete(String... key);

    Future<String> dump(String key);

    Future<Integer> exists(String key);

    Future<Long> expire(String key, int seconds);

    Future<Long> expireAt(String key, long timestamp);

    Future<List<String>> keys(String pattern);

    Future<String> migrate(String host, int port, String key, int db, int timeOut);

    /**
     * Move a key to another database
     *
     * @param key cached key
     * @param db  dest db index
     * @return success = true, fail = false
     */
    Future<Boolean> move(String key, int db);

    /**
     * Remove the existing timeout on key,
     * turning the key from volatile (a key with an expire set)
     * to persistent (a key that will never expire as no timeout is associated).
     *
     * @param key cached key
     * @return success = true, fail = false
     */
    Future<Boolean> presist(String key);

    //strings commands
    Future<String> set(String key, int o);

    Future<String> set(String key, long o);

    Future<String> set(String key, double o);

    Future<String> set(String key, Object o);

    /**
     * Set key to hold string value if key does not exist. In that case, it is equal to SET.
     * When key already holds a value, no operation is performed. SETNX is short for "SET if N ot e X ists".
     * @param key   cached key
     * @param o    value to set
     * @return  1 if the key was set ,0 if the key was not set
     */
    Future<Integer> setNx(String key, int o);

    Future<Integer> setNx(String key, long o);

    Future<Integer> setNx(String key, double o);

    Future<Integer> setNx(String key, Object o);

    Future<Integer> msetIntNx(Map<String, Integer> value);

    Future<Integer> msetLongNx(Map<String, Long> value);

    Future<Integer> msetDoubleNx(Map<String, Double> value);

    Future<Integer> msetObjectNx(Map<String, Object> value);

    /**
     * Set key to hold the string value and set key to timeout after a given number of seconds.
     * This command is equivalent to executing the following commands:
     SET mykey value
     EXPIRE mykey seconds
     SETEX is atomic, and can be reproduced by using the previous two commands inside an MULTI / EXEC block. It is provided as a faster alternative to the given sequence of operations, because this operation is very common when Redis is used as a cache.
     An error is returned when seconds is invalid.
     * @param key cached key
     * @param o  value to set
     * @param seconds   timeout seconds
     * @return status code
     */
    Future<String> setEx(String key, int o, int seconds);

    Future<String> setEx(String key, long o, int seconds);

    Future<String> setEx(String key, double o, int seconds);

    Future<String> setEx(String key, Object o, int seconds);

    Future<String> msetInt(Map<String, Integer> value);

    Future<String> msetLong(Map<String, Long> value);

    Future<String> msetDouble(Map<String, Double> value);

    Future<String> msetObject(Map<String, Object> value);




    Future<Object> get(String key);

    Future<Integer> getInt(String key);

    Future<Long> getLong(String key);

    Future<Double> getDouble(String key);



    Future<List<Object>> mget(String[] key);

    Future<List<Integer>> mgetInt(String[] key);

    Future<List<Long>> mgetLong(String[] key);

    Future<List<Double>> mgetDouble(String[] key);

    /**
     * Decrements the number stored at key by one.
     * If the key does not exist, it is set to 0 before performing the operation.
     * An error is returned if the key contains a value of the wrong type or contains a string that can not be represented as integer.
     * This operation is limited to 64 bit signed integers.
     * @param key  cached key
     * @return   the value of key after the decrement
     */
    Future<Long> decr(String key);

    Future<Long> decrBy(String key, int decrement);

    /**
     * Increments the number stored at key by one.
     * If the key does not exist, it is set to 0 before performing the operation.
     * An error is returned if the key contains a value of the wrong type or contains a string that can not be represented as integer.
     * This operation is limited to 64 bit signed integers.
     Note: this is a string operation because Redis does not have a dedicated integer type. The string stored at the key is interpreted as a base-10 64 bit signed integer to execute the operation.
     Redis stores integers in their integer representation, so for string values that actually hold an integer, there is no overhead for storing the string representation of the integer.
     * @param key  cached key
     * @return   the value of key after the increment
     */

    Future<Long> incr(String key);

    Future<Long> incrBy(String key, int decrement);

    /**
     * Atomically sets key to value and returns the old value stored at key. Returns an error when key exists but does not hold a string value.
     * @param key cached key
     * @param v   the value to set
     * @return  the old value stored at key, or nil when key did not exist.
     */
    Future<Long> getAndSet(String key, int v);
}
