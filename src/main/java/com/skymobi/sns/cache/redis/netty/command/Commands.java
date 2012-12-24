package com.skymobi.sns.cache.redis.netty.command;

import com.skymobi.sns.cache.redis.transcoders.Transcoder;

import java.util.Map;

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
    },

    //keys
    DEL {
        @Override
        public <T> Command getCommand(Transcoder<T> transcoder, Object... args) {
            String[] keys = new String[args.length];
            for(int i = 0; i< args.length; i++){
                keys[i] = String.valueOf(args[i]);
            }
            return new StringArgsCommand<String>(transcoder, name(), keys);
        }
    },
    EXISTS {
        @Override
        public <T> Command getCommand(Transcoder<T> transcoder, Object... args) {
            return new StringArgsCommand<String>(transcoder, name(), (String) args[0]);
        }
    }
    //strings
    , SET {
        @Override
        public Command getCommand(Transcoder transcoder, Object... args) {
            return new StringArgsCommand<String>(transcoder, name(), (String) args[0], args[1]);
        }
    },MSET {
        @Override
        public <T> Command getCommand(Transcoder<T> transcoder, Object... args) {
            //noinspection unchecked
            return new MsetCommand(transcoder, name(), (Map<String,?>) args[0]);
        }
    }, SETNX {
        @Override
        public Command getCommand(Transcoder transcoder, Object... args) {
            return new StringArgsCommand<String>(transcoder, name(), (String) args[0], args[1]);
        }
    },GET {
        @Override
        public <T> Command getCommand(Transcoder<T> transcoder, Object... args) {
            return new StringArgsCommand<T>(transcoder, name(), (String) args[0]);
        }
    }
    ;
    public abstract <T> Command getCommand(Transcoder<T> transcoder,  Object... args);
}
