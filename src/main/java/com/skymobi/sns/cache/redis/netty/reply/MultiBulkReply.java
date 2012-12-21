package com.skymobi.sns.cache.redis.netty.reply;

import com.skymobi.sns.cache.redis.transcoders.Transcoder;

import java.util.ArrayList;
import java.util.List;

/**
 * User: thor
 * Date: 12-12-21
 * Time: 上午10:04
 */
public class MultiBulkReply implements Reply<List<Object>>{
    final List<byte[]> result;

    final int fullSize;

    public MultiBulkReply(int size) {
        fullSize = size;
        if (size > 0)
            result = new ArrayList<byte[]>(size);
        else
            result = null;
    }

    public boolean isReady() {
        return fullSize == -1 || result.size() == fullSize;
    }

    public void add(byte[] object) {
        result.add(object);
    }

    @Override
    public Object get() {
        return result;
    }

    @Override
    public List<Object> decode(Transcoder transcoder) {
        List<Object> decodeResult = new ArrayList<Object>(fullSize);
        for(byte[] buffer: result){
            decodeResult.add(transcoder.decode(buffer));
        }
        return decodeResult;
    }
}
