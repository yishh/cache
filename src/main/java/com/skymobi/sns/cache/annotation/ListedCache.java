package com.skymobi.sns.cache.annotation;

import java.lang.annotation.*;

/**
 * Created by IntelliJ IDEA.
 * User: liweijing
 * Date: 11-7-29
 * Time: 下午1:15
 * To change this template use File | Settings | File Templates.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface ListedCache {
    CacheKey key();
    String type() default "";
//    String[] parameterNames();
    int offsetIndex() default 1;
    int limitIndex() default 2;
//    public boolean layerd() default false;
}
