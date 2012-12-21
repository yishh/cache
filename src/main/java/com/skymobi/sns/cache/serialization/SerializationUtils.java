package com.skymobi.sns.cache.serialization;

import com.google.common.annotations.GwtIncompatible;

import java.io.*;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * User: thor
 * Date: 12-12-18
 * Time: 下午7:31
 */
public class SerializationUtils {
    final static String CHARSET = "utf-8";

    public static byte[][] encodeMany(final String... strs) {
        byte[][] many = new byte[strs.length][];
        for (int i = 0; i < strs.length; i++) {
            many[i] = encode(strs[i]);
        }
        return many;
    }

    public static byte[] encode(final String str) {
        try {
            return str.getBytes(CHARSET);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String encode(final byte[] data) {
        try {
            return new String(data, CHARSET);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static byte[] serialize(Object object) {
        if (object == null) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            oos.flush();
        }
        catch (IOException ex) {
            throw new IllegalArgumentException("Failed to serialize object of type: " + object.getClass(), ex);
        }
        return baos.toByteArray();
    }

    /**
     * Deserialize the byte array into an object.
     * @param bytes a serialized object
     * @return the result of deserializing the bytes
     */
    public static Object deserialize(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        try {
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
            return ois.readObject();
        }
        catch (IOException ex) {
            throw new IllegalArgumentException("Failed to deserialize object", ex);
        }
        catch (ClassNotFoundException ex) {
            throw new IllegalStateException("Failed to deserialize object type", ex);
        }
    }



    public static byte[] toByteArray(char value) {
        return new byte[] {
                (byte) (value >> 8),
                (byte) value};
    }

    /**
     * Returns the {@code char} value whose big-endian representation is
     * stored in the first 2 bytes of {@code bytes}; equivalent to {@code
     * ByteBuffer.wrap(bytes).getChar()}. For example, the input byte array
     * {@code {0x54, 0x32}} would yield the {@code char} value {@code '\\u5432'}.
     *
     * <p>Arguably, it's preferable to use {@link java.nio.ByteBuffer}; that
     * library exposes much more flexibility at little cost in readability.
     *
     * @throws IllegalArgumentException if {@code bytes} has fewer than 2
     *     elements
     */

    public static char fromByteArray(byte[] bytes) {
        return fromBytes(bytes[0], bytes[1]);
    }

    public static char fromBytes(byte b1, byte b2) {
        return (char) ((b1 << 8) | (b2 & 0xFF));
    }
}
