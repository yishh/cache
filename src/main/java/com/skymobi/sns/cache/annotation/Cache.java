package com.skymobi.sns.cache.annotation;


import java.lang.annotation.*;

/**
 * Created by IntelliJ IDEA.
 * User: liweijing
 * Date: 11-7-29
 * Time: 下午1:12
 * To change this template use File | Settings | File Templates.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface Cache {
    //    String k
    CacheKey key();

    String type() default "";

    String expireTime() default "";

    int expire() default 0;

    boolean cacheNull() default false;
}
