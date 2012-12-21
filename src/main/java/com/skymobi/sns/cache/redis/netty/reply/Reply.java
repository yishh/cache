package com.skymobi.sns.cache.redis.netty.reply;

import com.skymobi.sns.cache.redis.transcoders.Transcoder;

/**
 * User: thor
 * Date: 12-12-20
 * Time: 下午2:10
 */
public interface Reply<T> {
    Object get();
    T decode(Transcoder transcoder);
}
