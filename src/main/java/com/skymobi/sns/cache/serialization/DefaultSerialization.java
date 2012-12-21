package com.skymobi.sns.cache.serialization;

import org.springframework.util.ClassUtils;
import redis.clients.util.SafeEncoder;

/**
 * User: thor
 * Date: 12-12-18
 * Time: 下午7:20
 */
@SuppressWarnings("ALL")
public class DefaultSerialization implements Serialization {
    @Override
    public byte[] serialize(Object object) {
        if (object == null) {
            return NULL_BYTE;
        }

        if (object instanceof String) {
            return SerializationUtils.encode((String) object);
        }

        Class c = object.getClass();
        if (ClassUtils.isPrimitiveOrWrapper(c)) {
            if (c.equals(Long.class) || c.equals(long.class)) {
                return SerializationUtils.encode(Long.toString((Long) object));
            }
            if (c.equals(Boolean.class) || c.equals(boolean.class)) {
                Boolean b = (Boolean) object;
                if (b) {
                    return new byte[]{1};
                } else {
                    return new byte[]{0};
                }
            }
            if (c.equals(Byte.class) || c.equals(byte.class)) {
                byte[] b = new byte[1];
                b[0] = (Byte) object;
                return b;
            }
            if (c.equals(Character.class) || c.equals(char.class)) {
                return SerializationUtils.toByteArray((Character) object);
            }
            if (c.equals(Integer.class) || c.equals(int.class)) {
                return SerializationUtils.encode(Integer.toString((Integer) object));
            }
            if (c.equals(Short.class) || c.equals(short.class)) {
                return SerializationUtils.encode(Short.toString((Short) object));
            }
        }
        return SerializationUtils.serialize(object);
    }

    @Override
    public Object deserialize(byte[] bytes) {
        return null;
    }


    public static Object fromByte(byte[] bytes, Class c) {

        if (ClassUtils.isPrimitiveOrWrapper(c)) {
            if (c.equals(Long.class) || c.equals(long.class)) {
                if (bytes == null || bytes.length == 0) {
                    return null;
                }
                return Long.parseLong(SafeEncoder.encode(bytes));
            }
            if (c.equals(Boolean.class) || c.equals(boolean.class)) {
                if (bytes == null || bytes.length == 0) {
                    return null;
                }
                return bytes[0] == 1;
            }
            if (c.equals(Byte.class) || c.equals(byte.class)) {
                if (bytes == null || bytes.length == 0) {
                    return null;
                }
                return bytes[0];
            }
            if (c.equals(Character.class) || c.equals(char.class)) {
                if (bytes == null || bytes.length == 0) {
                    return null;
                }
                return SerializationUtils.fromByteArray(bytes);
            }
            if (c.equals(Integer.class) || c.equals(int.class)) {
                if (bytes == null || bytes.length == 0) {
                    return null;
                }
                return Integer.parseInt(SafeEncoder.encode(bytes));
            }
            if (c.equals(Short.class) || c.equals(short.class)) {
                if (bytes == null || bytes.length == 0) {
                    return null;
                }
                return Short.parseShort(SafeEncoder.encode(bytes));
            }
        }
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        if (c.equals(String.class)) {
            return SafeEncoder.encode(bytes);
        }

        return org.springframework.util.SerializationUtils.deserialize(bytes);
    }

}
