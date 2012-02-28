package com.skymobi.sns.cache;

/**
 * Created by IntelliJ IDEA.
 * User: liweijing
 * Date: 11-9-13
 * Time: 下午4:51
 * To change this template use File | Settings | File Templates.
 */
public interface Counter {
    long incr(String key);
    long decr(String key);
    long incr(String key, int value, int initValue);
    long decr(String key, int value, int initValue);
    long get(String key);
    void set(String key, long value);
    void remove(String key);
    boolean exists(String key);
    void setPrefix(String prefix);
    String getPrefix();

}
