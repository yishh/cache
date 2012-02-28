package com.skymobi.sns.cache.annotation;

import java.lang.annotation.*;

/**
 * Created by IntelliJ IDEA.
 * User: liweijing
 * Date: 11-7-29
 * Time: 下午5:44
 * To change this template use File | Settings | File Templates.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface WriteCache {
    CacheKey key();
    String type() default "";
//    String[] parameterNames();
    int expire() default 0;
    String expireTime() default "";
    boolean cacheNull() default false;
    int parameterIndex() default 0;
    boolean writeParameter() default false;
    boolean writeReturn() default true;
//    public boolean layerd() default true;
}
