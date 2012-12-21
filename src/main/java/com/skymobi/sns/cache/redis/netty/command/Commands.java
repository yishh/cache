package com.skymobi.sns.cache.redis.netty.command;

import com.skymobi.sns.cache.redis.transcoders.Transcoder;

/**
 * User: thor
 * Date: 12-12-21
 * Time: 下午1:17
 */
public enum Commands {
    //Connection
    AUTH {
        @Override
        public  Command getCommand(Transcoder transcoder, Object... args) {
            return new StringArgsCommand<String>(transcoder, name(), (String) args[0]);
        }
    }, PING {
        @Override
        public  Command getCommand(Transcoder transcoder, Object... args) {
            return new StringArgsCommand<String>(transcoder, name());
        }
    }, SELECT {
        @Override
        public  Command getCommand(Transcoder transcoder, Object... args) {
            return new StringArgsCommand<String>(transcoder, name(), String.valueOf(args[0]));
        }
    }, ECHO {
        @Override
        public  Command getCommand(Transcoder transcoder, Object... args) {
            return new EchoCommand( name(), (String) args[0]);
        }
    }, QUIT {
        @Override
        public Command getCommand(Transcoder transcoder, Object... args) {
            return new StringArgsCommand<String>(transcoder, name());
        }
    }

    //keys
    , SET {
        @Override
        public Command getCommand(Transcoder transcoder, Object... args) {
            return new StringArgsCommand<String>(transcoder, name(), (String) args[0], args[1]);
        }
    }, GET {
        @Override
        public <T> Command getCommand(Transcoder<T> transcoder, Object... args) {
            return new StringArgsCommand<T>(transcoder, name(), (String) args[0]);
        }
    }
    ;
    public abstract <T> Command getCommand(Transcoder<T> transcoder,  Object... args);
}
