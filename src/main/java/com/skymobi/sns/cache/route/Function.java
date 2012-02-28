package com.skymobi.sns.cache.route;

/**
 * Created by IntelliJ IDEA.
 * User: liweijing
 * Date: 11-12-9
 * Time: 上午11:08
 * To change this template use File | Settings | File Templates.
 */
public interface Function{
    int locate(String key, Object... args);
}
