package com.skymobi.sns.cache.annotation;

import java.lang.annotation.*;

/**
 * Created by IntelliJ IDEA.
 * User: liweijing
 * Date: 11-7-29
 * Time: 下午2:58
 * To change this template use File | Settings | File Templates.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface SimpleCache {
    public String key();
    String type() default "";
    int expire();
    String expireTime() default "";
//    public boolean layerd() default true;
    boolean cacheNull() default false;
}
