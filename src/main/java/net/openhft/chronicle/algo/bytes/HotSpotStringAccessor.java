/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo.bytes;

import java.lang.reflect.Field;

import static net.openhft.chronicle.core.UnsafeMemory.MEMORY;

/**
 * Provides read access to the underlying value array of a String instance.
 * This class supports both Java 8 (char array) and Java 9+ (byte array) string implementations.
 *
 * @param <T> the type of the underlying value array, either char[] or byte[]
 */
final class HotSpotStringAccessor<T> implements Accessor.Read<String, T> {

    // Singleton instance for Java 8 (char array) string representation
    public static final HotSpotStringAccessor<char[]> JAVA8 = new HotSpotStringAccessor<>();

    // Singleton instance for Java 9+ (byte array) string representation
    public static final HotSpotStringAccessor<byte[]> JAVA9PLUS = new HotSpotStringAccessor<>();

    // Offset of the value field within the String class
    private static final long valueOffset;

    static {
        try {
            // Retrieve the offset of the value field within the String class
            Field valueField = String.class.getDeclaredField("value");
            valueOffset = MEMORY.objectFieldOffset(valueField);
        } catch (NoSuchFieldException e) {
            throw new AssertionError(e);
        }
    }

    // Private constructor to prevent instantiation
    private HotSpotStringAccessor() {
    }

    /**
     * Returns the access implementation for the underlying value array.
     *
     * @return the ReadAccess implementation for the underlying value array
     */
    @Override
    public ReadAccess<T> access() {
        return NativeAccess.instance();
    }

    /**
     * Retrieves the underlying value array from the given String instance.
     *
     * @param source the source String instance
     * @return the underlying value array
     */
    @SuppressWarnings("unchecked")
    @Override
    public T handle(String source) {
        return MEMORY.getObject(source, valueOffset);
    }

    /**
     * Converts the given index to an offset in the underlying value array.
     *
     * @param source the source String instance
     * @param index  the index in the String
     * @return the offset in the underlying value array
     */
    @Override
    public long offset(String source, long index) {
        return ArrayAccessors.Char.INSTANCE.offset(null, index);
    }

    /**
     * Converts the given size in characters to size in bytes.
     *
     * @param size the size in characters
     * @return the size in bytes
     */
    @Override
    public long size(long size) {
        return size * 2L;
    }
}
