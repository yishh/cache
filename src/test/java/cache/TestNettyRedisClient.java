package cache;

import com.skymobi.sns.cache.redis.netty.NettyRedisClient;
import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: thor
 * Date: 12-12-20
 * Time: 上午11:36
 */
public class TestNettyRedisClient extends TestCase {

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
        String result = client.set("TEST_KEY2", "CACHED").get();
        assertEquals("OK", result);
        String strCached = (String) client.get("TEST_KEY2").get();
        assertEquals("CACHED", strCached);
    }

    public void testSetInt() throws ExecutionException, InterruptedException {
        String result = client.set("TEST_KEY1", 1).get();
        assertEquals("OK", result);
        int cached = client.getInt("TEST_KEY1").get();
        assertEquals(1, cached);


    }

    public void testSetLongAndDouble() throws ExecutionException, InterruptedException {
        String result = client.set("TEST_KEY_LONG", 1l).get();
        assertEquals("OK", result);
        long cached = client.getLong("TEST_KEY_LONG").get();
        assertEquals(1l, cached);

        result = client.set("TEST_KEY_DOUBLE", 1.013).get();
        assertEquals("OK", result);
        double cachedDouble = client.getDouble("TEST_KEY_DOUBLE").get();
        assertEquals(1.013, cachedDouble);
    }

    public void testConcurrencyOpera() throws InterruptedException {
        int concurrencyCount = 20;
        ExecutorService executorService = Executors.newFixedThreadPool(concurrencyCount);
        final CountDownLatch latch = new CountDownLatch(concurrencyCount);
        final AtomicInteger successCount = new AtomicInteger(0);
        for (int i = 0; i < concurrencyCount; i++) {
            final int id = i;
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    String key = String.format("TESTKEY-%s", id);
                    String value = String.format("TESTVALUE-%s", id);
                    String result = null;
                    try {
                        result = client.set(key, value).get(1, TimeUnit.SECONDS);
                        assertEquals("OK", result);
                        String strCached = (String) client.get(key).get(1, TimeUnit.SECONDS);
//                        assert value.equals(strCached);
//                        assertEquals(value, strCached);
                        if (value.equals(strCached))
                            successCount.incrementAndGet();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (TimeoutException e) {
                        e.printStackTrace();
                    }
                    latch.countDown();

                }
            });
        }
        latch.await();
        assertEquals(concurrencyCount, successCount.get());
        executorService.shutdownNow();
    }

    public void testExistsAndDel() throws ExecutionException, InterruptedException {
        String key1 = "DEL_KEY1";
        String key2 = "DEL_KEY2";
        client.delete(key1, key2);
        client.set(key1, 1);
        client.set(key2, 2);
        int reply = client.exists(key1).get();
        assertEquals(1, reply);
        reply = client.exists(key2).get();
        assertEquals(1, reply);
        int number = client.delete(key1, key2).get();
        assertEquals(2, number);
        reply = client.exists(key1).get();
        assertEquals(0, reply);
        reply = client.exists(key2).get();
        assertEquals(0, reply);
    }

    public void testSetNx() throws ExecutionException, InterruptedException {
        String key1 = "NX_KEY1";
        client.delete(key1);
        int reply = client.setNx(key1, "sss").get();
        assertEquals(1, reply);

        reply = client.setNx(key1, 2).get();
        assertEquals(0, reply);

        String cached = (String) client.get(key1).get();
        assertEquals("sss", cached);

    }

    public void testMset() throws ExecutionException, InterruptedException {
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("MAP_KEY1", 1);
        map.put("MAP_KEY2", 2);
        String result = client.msetInt(map).get();
        assertEquals("OK", result);
        int cached = client.getInt("MAP_KEY1").get();
        assertEquals(1, cached);
    }
//    public void testQuit() throws ExecutionException, InterruptedException {
//        String result = client.quit().get();
//        assertEquals("OK", result);
//    }


}
