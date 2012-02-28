package com.skymobi.sns.cache;

/**
 * Created by IntelliJ IDEA.
 * User: liweijing
 * Date: 11-9-13
 * Time: 下午4:58
 * To change this template use File | Settings | File Templates.
 */
public abstract  class AbstractCounter implements Counter{
    String prefix;

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String makeKey(String key){
        if(prefix==null){
            return  key;
        }
        return String.format("%s/%s", prefix, key);
    }
}
