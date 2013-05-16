package cache;

import com.skymobi.sns.cache.annotation.Cache;
import com.skymobi.sns.cache.annotation.CacheKey;
import com.skymobi.sns.cache.annotation.RemoveCache;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * User: thor
 * Date: 13-5-15
 * Time: 下午4:31
 */
public class TestAnno {
    @RemoveCache(key = {@CacheKey(template = "ddd", simple = true)})
    @Cache(key = @CacheKey(template = "ddd", simple = true))
    public void testM1(){

    }

    @Cache(key = @CacheKey(template = "ddd", simple = true))
    @RemoveCache(key = @CacheKey(template = "ddd", simple = true))
    public void testM2(){

    }
    public static void main(String[] args) throws NoSuchMethodException {
        TestAnno anno = new TestAnno();
        Method m1 = anno.getClass().getMethod("testM1");
        Method m2 = anno.getClass().getMethod("testM2");
        for(Annotation a: m1.getAnnotations()){
            System.out.println(a.toString());
        }
        System.out.println("-----");
        for(Annotation a: m2.getAnnotations()){
            System.out.println(a.toString());
        }

    }
}
