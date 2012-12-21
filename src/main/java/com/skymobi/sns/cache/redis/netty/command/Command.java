package com.skymobi.sns.cache.redis.netty.command;

import com.skymobi.sns.cache.redis.netty.reply.Reply;
import com.skymobi.sns.cache.redis.transcoders.Transcoder;

import java.util.List;
import java.util.concurrent.Future;

/**
 * User: thor
 * Date: 12-12-20
 * Time: 上午10:18
 */
public interface Command<T> {
    List<byte[]> getArgs();
    void setTranscoder(Transcoder transcoder);
    Transcoder getTranscoder();

    void setResult(Reply reply);
//    boolean decodeBySelf();
//    T decode(Reply reply);
    Future<T> getReply();
}
