package com.skymobi.sns.cache.redis;

import java.util.List;
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
    Future<Long> delete(String key);

    Future<Long> delete(String... key);

    Future<String> dump(String key);

    Future<Boolean> exists(String key);

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

    Future<Object> get(String key);

    Future<Integer> getInt(String key);
}
