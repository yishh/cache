package com.skymobi.sns.cache.redis.netty.reply;

import com.skymobi.sns.cache.redis.transcoders.Transcoder;
import com.skymobi.sns.cache.transcoders.CachedData;

/**
 * User: thor
 * Date: 12-12-20
 * Time: 下午4:59
 */
public class BulkReply implements Reply<Object> {


    final byte[] result;

    public BulkReply(byte[] result) {
        this.result = result;
    }


    @Override
    public byte[] get() {
        return result;
    }

    @Override
    public Object decode(Transcoder transcoder) {
        return transcoder.decode(result);
    }


}
