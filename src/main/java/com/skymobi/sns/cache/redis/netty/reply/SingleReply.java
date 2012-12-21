package com.skymobi.sns.cache.redis.netty.reply;

import com.skymobi.sns.cache.redis.transcoders.Transcoder;

/**
 * User: thor
 * Date: 12-12-20
 * Time: 下午2:14
 */
public class SingleReply implements Reply {

    final String buffer;
    public SingleReply(String buffer){
        this.buffer = buffer;
    }

    @Override
    public byte[] get() {
        return buffer.getBytes();
    }

    @Override
    public Object decode(Transcoder transcoder) {
        return buffer;
    }


}
