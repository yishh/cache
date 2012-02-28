package com.skymobi.sns.cache.annotation;

/**
 * Created by IntelliJ IDEA.
 * User: liweijing
 * Date: 11-7-29
 * Time: 下午3:40
 * To change this template use File | Settings | File Templates.
 */
public @interface  CacheKeyEl {
    boolean simple() default false;
    String el();
}
