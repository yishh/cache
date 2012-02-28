package com.skymobi.sns.cache.annotation;

import java.lang.annotation.*;

/**
 * Created by IntelliJ IDEA.
 * User: liweijing
 * Date: 11-8-1
 * Time: 下午3:15
 * To change this template use File | Settings | File Templates.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheProxy {
    String type() default "";
}
