package com.skymobi.sns.cache.redis.netty;

import com.skymobi.sns.cache.redis.AsyncRedisClient;
import com.skymobi.sns.cache.redis.netty.command.Command;
import com.skymobi.sns.cache.redis.netty.command.Commands;
import com.skymobi.sns.cache.redis.netty.reply.Reply;
import com.skymobi.sns.cache.redis.transcoders.IntegerTranscoder;
import com.skymobi.sns.cache.redis.transcoders.Transcoder;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.*;

/**
 * User: thor
 * Date: 12-12-19
 * Time: 上午10:37
 */
public class NettyRedisClient extends SimpleChannelHandler implements AsyncRedisClient {
    Logger logger = LoggerFactory.getLogger(NettyRedisClient.class);

    final String address;
    final int db;
    final String password;
    final String host;
    final int port;

    NioClientSocketChannelFactory nioClientSocketChannelFactory;
    Channel channel;


    public NettyRedisClient(String address, int db, String password) {
        this.address = address;
        this.db = db;
        this.password = password;
        String[] parts = address.split(":");
        host = parts[0];
        port = Integer.parseInt(parts[1]);
        connect();
        if (password != null) {
            logger.info("try auth command:");
            auth(password);
            try {
                String authReply = auth(password).get();
                logger.info("auth db reply: {}", authReply);
            } catch (InterruptedException e) {
                throw new RedisException(e);
            } catch (ExecutionException e) {
                throw new RedisException(e);
            }

        }
        if (db > 0) {
            logger.debug("try to select db: {} ", db);
            try {
                String selectReply = select(db).get();
                logger.info("select db reply: {}", selectReply);
            } catch (InterruptedException e) {
                throw new RedisException(e);
            } catch (ExecutionException e) {
                throw new RedisException(e);
            }
        }
    }


    public void connect() {
        nioClientSocketChannelFactory = new NioClientSocketChannelFactory(
                Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool());
        ClientBootstrap bootstrap =
                new ClientBootstrap(nioClientSocketChannelFactory);
        ChannelPipeline pipeline = Channels.pipeline(new ReplyDecoder(), new CommandEncoder(), this);
        bootstrap.setPipeline(pipeline);
        ChannelFuture connectFuture = bootstrap.connect(new InetSocketAddress(host, port));
        channel = connectFuture.awaitUninterruptibly().getChannel();

    }

    public void close() throws InterruptedException {
        channel.close().await(500);
        nioClientSocketChannelFactory.releaseExternalResources();
    }


    private Future sendCommand(Commands commands, Transcoder transcoder,  Object... args){
        Command command = commands.getCommand(transcoder, args);
        channel.write(command);
        return command.getReply();
    }


    @Override
    public Future<String> auth(String password) {
        //noinspection unchecked
        return sendCommand(Commands.AUTH, null, password);
    }


    @Override
    public Future<String> echo(String message) {
        //noinspection unchecked
        return sendCommand(Commands.ECHO,  null,message);
    }

    @Override
    public Future<String> ping() {
        //noinspection unchecked
        return sendCommand(Commands.PING ,null);
    }

    @Override
    public Future<String> select(int db) {
        //noinspection unchecked
        return sendCommand(Commands.SELECT, null, db);
    }

    @Override
    public Future<String> quit() {
        //noinspection unchecked
        return sendCommand(Commands.QUIT, null);
    }

    @Override
    public Future<Long> delete(String key) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Future<Long> delete(String... key) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Future<String> dump(String key) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Future<Boolean> exists(String key) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Future<Long> expire(String key, int seconds) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Future<Long> expireAt(String key, long timestamp) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Future<List<String>> keys(String pattern) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Future<String> migrate(String host, int port, String key, int db, int timeOut) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Future<Boolean> move(String key, int db) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Future<Boolean> presist(String key) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Future<String> set(String key, int o) {
        //noinspection unchecked
        return sendCommand(Commands.SET, null, key, String.valueOf(o));
    }

    @Override
    public Future<String> set(String key, long o) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Future<String> set(String key, double o) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Future<String> set(String key, Object o) {
        //noinspection unchecked
        return sendCommand(Commands.SET, null, key, o);
    }

    @Override
    public Future<Object> get(String key) {
        return null;
    }

    @Override
    public Future<Integer> getInt(String key) {
        //noinspection unchecked
        return sendCommand(Commands.GET, new IntegerTranscoder(), key);
    }

    final BlockingQueue<Command> commandQueue = new LinkedBlockingQueue<Command>();

    @Override
    public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        Command message = (Command) e.getMessage();
        commandQueue.put(message);
        ctx.sendDownstream(e);
    }


    @SuppressWarnings("unchecked")
    @Override
    public void messageReceived(
            ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        logger.debug("messageReceived");
        Reply reply = (Reply) e.getMessage();
        Command command = commandQueue.take();
        command.setResult(reply);
        ctx.sendUpstream(e);

    }

    @Override
    public synchronized void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        channel = ctx.getChannel();
        //连接断开后清空当前的commandQueue ,
        commandQueue.clear();
    }

    @Override
    public synchronized void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        commandQueue.clear();
    }
}
