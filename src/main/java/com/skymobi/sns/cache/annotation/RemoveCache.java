package com.skymobi.sns.cache.annotation;

import java.lang.annotation.*;

/**
 * Created by IntelliJ IDEA.
 * User: liweijing
 * Date: 11-8-5
 * Time: 下午2:29
 * To change this template use File | Settings | File Templates.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface RemoveCache {
    //    String simpleKey() default "";
    CacheKey key();

    String type() default "";

//    public boolean layerd() default true;

    boolean markAsNull() default false;

    int expire() default 0;

    String expireTime() default "";

}
