package com.skymobi.sns.cache.annotation;

import java.lang.annotation.*;

/**
 * Created by IntelliJ IDEA.
 * User: liweijing
 * Date: 11-7-29
 * Time: 下午5:31
 * To change this template use File | Settings | File Templates.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface WriteListCache {
    CacheKey key();
    String type() default "";
//    String[] parameterNames();
    int parameterIndex() default 0;
    boolean writeParameter() default false;
    boolean writeReturn() default true;
    int max() default 1000;
    boolean append() default  false;
//    int expire();
//    boolean cacheNull() default false;
}
