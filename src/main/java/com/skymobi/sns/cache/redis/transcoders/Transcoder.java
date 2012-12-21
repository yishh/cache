package com.skymobi.sns.cache.redis.transcoders;

/**
 * User: thor
 * Date: 12-12-21
 * Time: 下午3:37
 */
public interface Transcoder<T> {
    byte[] encode(T v);
    T decode(byte[] v);
}
