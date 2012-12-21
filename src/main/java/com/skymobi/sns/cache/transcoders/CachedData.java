/**
 * Copyright (C) 2006-2009 Dustin Sallings
 * Copyright (C) 2009-2011 Couchbase, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALING
 * IN THE SOFTWARE.
 */

package com.skymobi.sns.cache.transcoders;

import com.skymobi.sns.cache.redis.transcoders.TranscoderUtils;

import java.util.Arrays;

/**
 * Cached data with its attributes.
 */
public final class CachedData {

    private static final TranscoderUtils tu = new TranscoderUtils(true);
    /**
     * The maximum size that should be considered storing in a server.
     */
  /*
   * though memcached no longer has a maximum size, rather than remove this
   * entirely just bump it up for now
   */
    public static final int MAX_SIZE = 20 * 1024 * 1024;

    private final int flags;
    private final byte[] data;
    private final byte[] fullData;

    public CachedData(byte[] fullData) {
        this.fullData = fullData;
        flags = tu.decodeInt(Arrays.copyOfRange(fullData, 0, 4));
        data = Arrays.copyOfRange(fullData, 4, fullData.length);
    }

    /**
     * Get a CachedData instance for the given flags and byte array.
     *
     * @param f       the flags
     * @param d       the data
     * @param maxSize the maximum allowable size.
     */
    public CachedData(int f, byte[] d, int maxSize) {
        super();
        if (d.length > maxSize) {
            throw new IllegalArgumentException("Cannot cache data larger than "
                    + maxSize + " bytes (you tried to cache a " + d.length
                    + " byte object)");
        }
        flags = f;
        data = d;
        fullData = new byte[4 + d.length];
        byte[] flagBytes = tu.encodeInt(f);
        System.arraycopy(flagBytes, 0, fullData, 0, flagBytes.length);
        System.arraycopy(data, 0, fullData, flagBytes.length, data.length);
    }


    public byte [] getFullData(){
        return fullData;
    }

    /**
     * Get the stored data.
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Get the flags stored along with this value.
     */
    public int getFlags() {
        return flags;
    }

    @Override
    public String toString() {
        return "{CachedData flags=" + flags + " data=" + Arrays.toString(data)
                + "}";
    }
}
