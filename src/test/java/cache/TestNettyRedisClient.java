package cache;

import com.skymobi.sns.cache.redis.netty.NettyRedisClient;
import junit.framework.TestCase;

import java.util.concurrent.ExecutionException;

/**
 * User: thor
 * Date: 12-12-20
 * Time: 上午11:36
 */
public class TestNettyRedisClient extends TestCase{

    NettyRedisClient client = new NettyRedisClient("172.16.3.214:6379", 1, null);


    public void testEcho() throws ExecutionException, InterruptedException {
        String result = client.echo("hello").get();
        assertEquals("hello", result);
    }

    public void testPing() throws ExecutionException, InterruptedException {
        String result = client.ping().get();
        assertEquals("PONG", result);
    }



    public void testSet() throws ExecutionException, InterruptedException {
        String result = client.set("TEST_KEY1", 1).get();
        assertEquals("OK", result);
    }
//    public void testQuit() throws ExecutionException, InterruptedException {
//        String result = client.quit().get();
//        assertEquals("OK", result);
//    }



}
