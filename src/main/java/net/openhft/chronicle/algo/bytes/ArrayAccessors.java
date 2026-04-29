/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo.bytes;

import static net.openhft.chronicle.core.UnsafeMemory.MEMORY;

/**
 * Utility class for providing array accessors for various primitive array types.
 * This class includes enums that implement {@link Accessor.Full} for different primitive types,
 * facilitating access to elements of arrays.
 */
final class ArrayAccessors {

    // Base offsets for each primitive array type
    static final long BYTE_BASE;
    private static final long BOOLEAN_BASE;
    private static final long CHAR_BASE;
    private static final long SHORT_BASE;
    private static final long INT_BASE;
    private static final long LONG_BASE;

    static {
        try {
            // Initialize base offsets using UnsafeMemory
            BOOLEAN_BASE = MEMORY.arrayBaseOffset(boolean[].class);
            BYTE_BASE = MEMORY.arrayBaseOffset(byte[].class);
            CHAR_BASE = MEMORY.arrayBaseOffset(char[].class);
            SHORT_BASE = MEMORY.arrayBaseOffset(short[].class);
            INT_BASE = MEMORY.arrayBaseOffset(int[].class);
            LONG_BASE = MEMORY.arrayBaseOffset(long[].class);

        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    // Private constructor to prevent instantiation
    private ArrayAccessors() {
    }

    /**
     * Enum providing full Accessor implementation for boolean arrays.
     */
    enum Boolean implements Accessor.Full<boolean[], boolean[]> {
        INSTANCE;

        /**
         * Provides the access instance for boolean arrays.
         *
         * @return the {@link Access} instance for boolean arrays
         */
        @Override
        public Access<boolean[]> access() {
            return NativeAccess.instance();
        }

        /**
         * Returns the handle for the given boolean array.
         *
         * @param source the source boolean array
         * @return the handle for the given boolean array
         */
        @Override
        public boolean[] handle(boolean[] source) {
            return source;
        }

        /**
         * Computes the offset for the given index in the boolean array.
         *
         * @param source the source boolean array
         * @param index  the index in the boolean array
         * @return the offset for the given index
         */
        @Override
        public long offset(boolean[] source, long index) {
            return BOOLEAN_BASE + index;
        }
    }

    /**
     * Enum providing full Accessor implementation for byte arrays.
     */
    enum Byte implements Accessor.Full<byte[], byte[]> {
        INSTANCE;

        /**
         * Provides the access instance for byte arrays.
         *
         * @return the {@link Access} instance for byte arrays
         */
        @Override
        public Access<byte[]> access() {
            return NativeAccess.instance();
        }

        /**
         * Returns the handle for the given byte array.
         *
         * @param source the source byte array
         * @return the handle for the given byte array
         */
        @Override
        public byte[] handle(byte[] source) {
            return source;
        }

        /**
         * Computes the offset for the given index in the byte array.
         *
         * @param source the source byte array
         * @param index  the index in the byte array
         * @return the offset for the given index
         */
        @Override
        public long offset(byte[] source, long index) {
            return BYTE_BASE + index;
        }
    }

    /**
     * Enum providing full Accessor implementation for char arrays.
     */
    enum Char implements Accessor.Full<char[], char[]> {
        INSTANCE;

        /**
         * Provides the access instance for char arrays.
         *
         * @return the {@link Access} instance for char arrays
         */
        @Override
        public Access<char[]> access() {
            return NativeAccess.instance();
        }

        /**
         * Returns the handle for the given char array.
         *
         * @param source the source char array
         * @return the handle for the given char array
         */
        @Override
        public char[] handle(char[] source) {
            return source;
        }

        /**
         * Computes the offset for the given index in the char array.
         *
         * @param source the source char array
         * @param index  the index in the char array
         * @return the offset for the given index
         */
        @Override
        public long offset(char[] source, long index) {
            return CHAR_BASE + (index * 2L);
        }

        /**
         * Converts the size in the source domain to size in bytes.
         *
         * @param size size in the source type domain
         * @return number of bytes corresponding to the given size in the source type domain
         */
        @Override
        public long size(long size) {
            return size * 2L;
        }
    }

    /**
     * Enum providing full Accessor implementation for short arrays.
     */
    enum Short implements Accessor.Full<short[], short[]> {
        INSTANCE;

        /**
         * Provides the access instance for short arrays.
         *
         * @return the {@link Access} instance for short arrays
         */
        @Override
        public Access<short[]> access() {
            return NativeAccess.instance();
        }

        /**
         * Returns the handle for the given short array.
         *
         * @param source the source short array
         * @return the handle for the given short array
         */
        @Override
        public short[] handle(short[] source) {
            return source;
        }

        /**
         * Computes the offset for the given index in the short array.
         *
         * @param source the source short array
         * @param index  the index in the short array
         * @return the offset for the given index
         */
        @Override
        public long offset(short[] source, long index) {
            return SHORT_BASE + (index * 2L);
        }

        /**
         * Converts the size in the source domain to size in bytes.
         *
         * @param size size in the source type domain
         * @return number of bytes corresponding to the given size in the source type domain
         */
        @Override
        public long size(long size) {
            return size * 2L;
        }
    }

    /**
     * Enum providing full Accessor implementation for int arrays.
     */
    enum Int implements Accessor.Full<int[], int[]> {
        INSTANCE;

        /**
         * Provides the access instance for int arrays.
         *
         * @return the {@link Access} instance for int arrays
         */
        @Override
        public Access<int[]> access() {
            return NativeAccess.instance();
        }

        /**
         * Returns the handle for the given int array.
         *
         * @param source the source int array
         * @return the handle for the given int array
         */
        @Override
        public int[] handle(int[] source) {
            return source;
        }

        /**
         * Computes the offset for the given index in the int array.
         *
         * @param source the source int array
         * @param index  the index in the int array
         * @return the offset for the given index
         */
        @Override
        public long offset(int[] source, long index) {
            return INT_BASE + (index * 4L);
        }

        /**
         * Converts the size in the source domain to size in bytes.
         *
         * @param size size in the source type domain
         * @return number of bytes corresponding to the given size in the source type domain
         */
        @Override
        public long size(long size) {
            return size * 4L;
        }
    }

    /**
     * Enum providing full Accessor implementation for long arrays.
     */
    enum Long implements Accessor.Full<long[], long[]> {
        INSTANCE;

        /**
         * Provides the access instance for long arrays.
         *
         * @return the {@link Access} instance for long arrays
         */
        @Override
        public Access<long[]> access() {
            return NativeAccess.instance();
        }

        /**
         * Returns the handle for the given long array.
         *
         * @param source the source long array
         * @return the handle for the given long array
         */
        @Override
        public long[] handle(long[] source) {
            return source;
        }

        /**
         * Computes the offset for the given index in the long array.
         *
         * @param source the source long array
         * @param index  the index in the long array
         * @return the offset for the given index
         */
        @Override
        public long offset(long[] source, long index) {
            return LONG_BASE + (index * 8L);
        }

        /**
         * Converts the size in the source domain to size in bytes.
         *
         * @param size size in the source type domain
         * @return number of bytes corresponding to the given size in the source type domain
         */
        @Override
        public long size(long size) {
            return size * 8L;
        }
    }
}
