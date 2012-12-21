package com.skymobi.sns.cache.redis.netty.command;

import com.google.common.base.Charsets;
import com.skymobi.sns.cache.redis.netty.reply.Reply;
import com.skymobi.sns.cache.redis.transcoders.SerializingTranscoder;
import com.skymobi.sns.cache.redis.transcoders.Transcoder;
import com.skymobi.sns.cache.redis.transcoders.TranscoderUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

/**
 * User: thor
 * Date: 12-12-20
 * Time: 上午10:19
 */
public abstract class BaseCommand<T> implements Command {
    protected String command;
    protected List<byte[]> args;
    protected ReplyFutureTask<T> future;
    final static TranscoderUtils tu = new TranscoderUtils(true);
    final static Transcoder<Object> SERIALIZING_TRANSCODER = new SerializingTranscoder();
    Transcoder transcoder;

    public BaseCommand() {
    }


    protected void init(byte[]... args) {
        this.args = new ArrayList<byte[]>();
        this.args.add(command.getBytes(Charsets.US_ASCII));
        Collections.addAll(this.args, args);
        future = new ReplyFutureTask<T>();
    }

    @Override
    public List<byte[]> getArgs() {
        return args;
    }


    public void setTranscoder(Transcoder transcoder) {
        this.transcoder = transcoder;
    }

    public Transcoder getTranscoder() {
        return transcoder;
    }

    @Override
    public void setResult(Reply reply) {
        @SuppressWarnings("unchecked")
        T result = (T) reply.decode(transcoder);
        future.setResultAndAWake(result);
    }

    @Override
    public Future<T> getReply() {
        return future;
    }
}
