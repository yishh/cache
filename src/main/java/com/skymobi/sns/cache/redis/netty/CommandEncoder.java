package com.skymobi.sns.cache.redis.netty;

import com.skymobi.sns.cache.redis.netty.command.Command;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * User: thor
 * Date: 12-12-20
 * Time: 上午10:16
 */
public class CommandEncoder extends OneToOneEncoder {
    Logger logger = LoggerFactory.getLogger(CommandEncoder.class);
    final static byte[] CR_LF = "\r\n".getBytes();
    public static final byte DOLLAR_BYTE = '$';
    public static final byte ASTERISK_BYTE = '*';

    @Override
    protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
        if(!(msg instanceof Command)) return null;
        logger.debug("encode command start");
        List<byte[]> args = ((Command) msg).getArgs();
        ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
        buffer.writeByte(ASTERISK_BYTE);
        writeInt(buffer,  args.size());
        buffer.writeBytes(CR_LF);
        for(byte[] arg: args){
            buffer.writeByte(DOLLAR_BYTE);
            writeInt(buffer,  arg.length);
            buffer.writeBytes(CR_LF);
            buffer.writeBytes(arg);
            buffer.writeBytes(CR_LF);
        }
        logger.debug("encode command end");
        return buffer;
    }

    protected static void writeInt(ChannelBuffer buf, int value) {
        if (value < 10) {
            buf.writeByte('0' + value);
            return;
        }

        StringBuilder sb = new StringBuilder(8);
        while (value > 0) {
            int digit = value % 10;
            sb.append((char) ('0' + digit));
            value /= 10;
        }

        for (int i = sb.length() - 1; i >= 0; i--) {
            buf.writeByte(sb.charAt(i));
        }
    }
}
