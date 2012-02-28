package cache;

import com.skymobi.sns.cache.redis.RedisClient;
import com.skymobi.sns.cache.redis.RedisCounter;
import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Created by IntelliJ IDEA.
 * User: liweijing
 * Date: 11-9-13
 * Time: 下午5:16
 * To change this template use File | Settings | File Templates.
 */
public class TestRedisCounter extends TestCase {

    public void testCounter() {
        String host = "127.0.0.1:8080/34dfe";
        int passwordSplit = host.indexOf("/");
        String[] parts = host.substring(0, passwordSplit ).split(":");
        String password = host.substring(passwordSplit + 1);
        System.out.println(parts[0]);
         System.out.println(parts[1]);
         System.out.println(password);
//        RedisClient client = new RedisClient("172.16.3.214:6379");
//        RedisCounter counter = new RedisCounter();
//        counter.setClient(client);
//        counter.setPrefix("TESTCOUNTER");
//        String key = "TEST";
//        counter.remove(key);
//
//        long v = counter.decr(key);
//        Assert.assertEquals(0, v);
//        v = counter.incr(key);
//        Assert.assertEquals(1, v);
//        v = counter.incr(key, 2, 1);
//        Assert.assertEquals(3, v);
//        v = counter.decr(key, 4, 0);
//        Assert.assertEquals(0, v);
//        v = counter.get(key);
//        Assert.assertEquals(0, v);
    }

}
