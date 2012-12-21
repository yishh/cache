package com.skymobi.sns.cache.redis.netty.command;

import com.skymobi.sns.cache.redis.transcoders.StringTranscoder;

/**
 * User: thor
 * Date: 12-12-21
 * Time: 下午1:59
 */
public class EchoCommand extends StringArgsCommand<String>{
    public EchoCommand(String command, String message) {
        super(new StringTranscoder(), command, message);
    }
}
