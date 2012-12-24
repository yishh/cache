package com.skymobi.sns.cache.redis.netty.command;

import com.skymobi.sns.cache.redis.transcoders.Transcoder;

import java.util.Collection;
import java.util.Map;

/**
 * User: thor
 * Date: 12-12-24
 * Time: 下午6:44
 */
public class MsetCommand<T> extends BaseCommand<T>{
    public MsetCommand(Transcoder transcoder, String command, Map<String, ?> map) {
        this.command = command;
        setTranscoder(transcoder);
        byte[][] byteArgs = new byte[map.size() * 2][];
        int i = 0;
        for(String key : map.keySet()){
            byteArgs[i] =  tu.encodeString(key);
            //noinspection unchecked
            byteArgs[i + 1] =  getTranscoder().encode(map.get(key));
            i += 2;
        }
        init(byteArgs);
    }
}
