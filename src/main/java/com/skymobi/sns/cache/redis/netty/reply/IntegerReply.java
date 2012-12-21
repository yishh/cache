package com.skymobi.sns.cache.redis.netty.reply;

import com.skymobi.sns.cache.redis.transcoders.Transcoder;

/**
 * User: thor
 * Date: 12-12-20
 * Time: 下午4:30
 */
public class IntegerReply implements Reply<Integer>{
    private final int value;
    public IntegerReply(int value){
       this.value = value;
    }


    @Override
    public Integer get() {
        return value;
    }

    @Override
    public Integer decode(Transcoder transcoder) {
        return value;
    }


}
