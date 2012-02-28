package com.skymobi.sns.cache.annotation;

import java.lang.annotation.*;

/**
 * Created by IntelliJ IDEA.
 * User: liweijing
 * Date: 11-7-29
 * Time: 下午3:16
 * To change this template use File | Settings | File Templates.
 */

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheKey {
    String template();
    String[] els() default {};
    boolean simple() default false;
}
