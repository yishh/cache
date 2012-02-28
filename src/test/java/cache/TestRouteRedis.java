package cache;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.skymobi.sns.cache.redis.RedisClient;
import com.skymobi.sns.cache.route.*;
import com.skymobi.sns.cache.tools.RedisReplicationTool;
import junit.framework.TestCase;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: liweijing
 * Date: 11-11-23
 * Time: 下午8:26
 * To change this template use File | Settings | File Templates.
 */
public class TestRouteRedis extends TestCase {
    public void testRoute() {

        KeyRouter keyRouter = new DefaultKeyRouter("172.16.3.214:6379",
                ImmutableMap.of("SNS/TEST1/.*", "172.16.3.214:6379",
                        "SNS/TEST2/.*", "172.16.3.214:6389"
                ));
        assertEquals("172.16.3.214:6379", keyRouter.getHost("SNS/TEST1/34343"));
        assertEquals("172.16.3.214:6389", keyRouter.getHost("SNS/TEST2/34343"));
    }
    
    public void testFunctionRoute(){
        Function function = new ModFunction(4);
        FunctionRouter keyRouter = new FunctionRouter(
                ImmutableMap.of(
                        "SNS/TEST1/(\\d+)", function
        ));
        keyRouter.setHosts(Lists.newArrayList(
                "172.16.3.214:6379", "172.16.3.215:6379" ,"172.16.3.216:6379" ,"172.16.3.217:6379"
        ));
        keyRouter.setDefaultHost("172.16.3.214:6379"); 
        String host = keyRouter.getHost("SNS/TEST1/123456");
        assertEquals("172.16.3.214:6379", host);
        host = keyRouter.getHost("SNS/TEST1/123457");
        assertEquals("172.16.3.215:6379", host);
        host = keyRouter.getHost("SNS/TEST1/123458");
        assertEquals("172.16.3.216:6379", host);
        host = keyRouter.getHost("SNS/TEST1/123459");
        assertEquals("172.16.3.217:6379", host);
        host = keyRouter.getHost("SNS/TEST1/123460");
        assertEquals("172.16.3.214:6379", host);
        RedisClient client = new RedisClient(keyRouter);
        client.set("SNS/TEST1/123456", 1);
        int v = client.get("SNS/TEST1/123456", Integer.class);
        assertEquals(1, v);
        try{
            client.set("SNS/TEST1/123457", 1);
        }catch (Exception e){
            assertEquals(JedisConnectionException.class,e.getClass());
        }

    }

    public void testRouteRedis() {
        RedisClient client1 = new RedisClient("172.16.3.214:6379");
        RedisClient client2 = new RedisClient("172.16.3.214:6389");
        client1.remove("SNS/TEST1/1");
        client2.remove("SNS/TEST1/1");
        client1.remove("SNS/TEST2/2");
        client2.remove("SNS/TEST2/2");
        KeyRouter keyRouter = new DefaultKeyRouter("172.16.3.214:6379",
                ImmutableMap.of("SNS/TEST1/.*", "172.16.3.214:6379",
                        "SNS/TEST2/.*", "172.16.3.214:6389"
                ));
        RedisClient client = new RedisClient(keyRouter);
        client.set("SNS/TEST1/1", 1);
        client.set("SNS/TEST2/2", 2);
        int v = client.get("SNS/TEST1/1", Integer.class);
        assertEquals(1, v);
        v = client.get("SNS/TEST2/2", Integer.class);
        assertEquals(2, v);

//        RedisClient client1 = new RedisClient("172.16.3.214:6379");
        assertFalse(client1.exists("SNS/TEST2/2"));
//        RedisClient client2 = new RedisClient("172.16.3.214:6389");
        assertFalse(client2.exists("SNS/TEST1/1"));

        RedisReplicationTool.copy("172.16.3.214:6379", "172.16.3.214:6389", "SNS/TEST1/*");
        RedisReplicationTool.copy("172.16.3.214:6389", "172.16.3.214:6379", "SNS/TEST2/*");
        KeyRouter keyRouter2 = new DefaultKeyRouter("172.16.3.214:6379",
                ImmutableMap.of("SNS/TEST2/.*", "172.16.3.214:6379",
                        "SNS/TEST1/.*", "172.16.3.214:6389"
                ));
        RedisClient clientx = new RedisClient(keyRouter2);
        v = clientx.get("SNS/TEST1/1", Integer.class);
        assertEquals(1, v);
        v = clientx.get("SNS/TEST2/2", Integer.class);
        assertEquals(2, v);
        assertFalse(client2.exists("SNS/TEST2/2"));
        assertFalse(client1.exists("SNS/TEST1/1"));
    }


    public void testListCopy() {
        RedisClient client1 = new RedisClient("172.16.3.214:6379");
        RedisClient client2 = new RedisClient("172.16.3.214:6389");
        client1.remove("SNS/TEST1/1");
        client2.remove("SNS/TEST1/1");
        client1.remove("SNS/TEST2/2");
        client2.remove("SNS/TEST2/2");
        KeyRouter keyRouter = new DefaultKeyRouter("172.16.3.214:6379",
                ImmutableMap.of("SNS/TEST1/.*", "172.16.3.214:6379",
                        "SNS/TEST2/.*", "172.16.3.214:6389"
                ));
        RedisClient client = new RedisClient(keyRouter);
        client.lpush("SNS/TEST1/1", 1);
        client.lpush("SNS/TEST1/1", 2);
        client.lpush("SNS/TEST1/1", 3);
        client.lpush("SNS/TEST2/2", 2);
        client.lpush("SNS/TEST2/2", 1);
        client.lpush("SNS/TEST2/2", 3);
        List<Integer> v = client.lrange("SNS/TEST1/1", 0, 1, Integer.class);
        assertEquals(3, v.get(0).intValue());
        v = client.lrange("SNS/TEST2/2", 0, 1, Integer.class);
        assertEquals(3, v.get(0).intValue());

//        RedisClient client1 = new RedisClient("172.16.3.214:6379");
        assertFalse(client1.exists("SNS/TEST2/2"));
//        RedisClient client2 = new RedisClient("172.16.3.214:6389");
        assertFalse(client2.exists("SNS/TEST1/1"));

        RedisReplicationTool.copy("172.16.3.214:6379", "172.16.3.214:6389", "SNS/TEST1/*");
        RedisReplicationTool.copy("172.16.3.214:6389", "172.16.3.214:6379", "SNS/TEST2/*");
        KeyRouter keyRouter2 = new DefaultKeyRouter("172.16.3.214:6379",
                ImmutableMap.of("SNS/TEST2/.*", "172.16.3.214:6379",
                        "SNS/TEST1/.*", "172.16.3.214:6389"
                ));
        RedisClient clientx = new RedisClient(keyRouter2);
        v = clientx.lrange("SNS/TEST1/1", 0, 3, Integer.class);
        assertEquals(3, v.get(0).intValue());
        assertEquals(1, v.get(2).intValue());
        v = clientx.lrange("SNS/TEST2/2", 0, 3, Integer.class);
        assertEquals(3, v.get(0).intValue());
        assertEquals(2, v.get(2).intValue());
        assertFalse(client2.exists("SNS/TEST2/2"));
        assertFalse(client1.exists("SNS/TEST1/1"));
    }

    public void testZSetCopy() {
        RedisClient client1 = new RedisClient("172.16.3.214:6379");
        RedisClient client2 = new RedisClient("172.16.3.214:6389");
        client1.remove("SNS/TEST1/1");
        client2.remove("SNS/TEST1/1");
        client1.remove("SNS/TEST2/2");
        client2.remove("SNS/TEST2/2");
        KeyRouter keyRouter = new DefaultKeyRouter("172.16.3.214:6379",
                ImmutableMap.of("SNS/TEST1/.*", "172.16.3.214:6379",
                        "SNS/TEST2/.*", "172.16.3.214:6389"
                ));
        RedisClient client = new RedisClient(keyRouter);
        client.zadd("SNS/TEST1/1", "t1", 1.0);
        client.zadd("SNS/TEST1/1", "t2", 2.0);
        client.zadd("SNS/TEST1/1", "t3", 3.0);


        client.zadd("SNS/TEST2/2", "t1", 10.0);
        client.zadd("SNS/TEST2/2", "t2", 20.0);
        client.zadd("SNS/TEST2/2", "t3", 30.0);

        long v = client.zrevrank("SNS/TEST1/1", "t2");
        assertEquals(1, v);
        v = client.zrevrank("SNS/TEST2/2", "t3");
        assertEquals(0, v);

//        RedisClient client1 = new RedisClient("172.16.3.214:6379");
        assertFalse(client1.exists("SNS/TEST2/2"));
//        RedisClient client2 = new RedisClient("172.16.3.214:6389");
        assertFalse(client2.exists("SNS/TEST1/1"));

        RedisReplicationTool.copy("172.16.3.214:6379", "172.16.3.214:6389", "SNS/TEST1/*");
        RedisReplicationTool.copy("172.16.3.214:6389", "172.16.3.214:6379", "SNS/TEST2/*");
        KeyRouter keyRouter2 = new DefaultKeyRouter("172.16.3.214:6379",
                ImmutableMap.of("SNS/TEST2/.*", "172.16.3.214:6379",
                        "SNS/TEST1/.*", "172.16.3.214:6389"
                ));
        RedisClient clientx = new RedisClient(keyRouter2);
        v = clientx.zrevrank("SNS/TEST1/1", "t2");
        assertEquals(1, v);
        v = clientx.zrevrank("SNS/TEST2/2", "t3");
        assertEquals(0, v);
        assertFalse(client2.exists("SNS/TEST2/2"));
        assertFalse(client1.exists("SNS/TEST1/1"));
    }

}
