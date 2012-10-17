package cache;

import com.skymobi.sns.cache.redis.RedisClient;

/**
 * User: thor
 * Date: 12-10-17
 * Time: 下午2:10
 */
public class TestRedisGet {
    public static void main(String[] args){
        RedisClient client = new RedisClient("172.16.3.214:6379");
        Integer v = client.get("TEST1111", Integer.class);
        System.out.println(v);
    }
}
