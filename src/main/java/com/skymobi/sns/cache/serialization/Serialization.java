package com.skymobi.sns.cache.serialization;

/**
 * User: thor
 * Date: 12-12-18
 * Time: 下午7:18
 */
public interface Serialization {
    static final byte[] NULL_BYTE = new byte[]{};

    public byte[] serialize(java.lang.Object object);

    public java.lang.Object deserialize(byte[] bytes);
}
