/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo.hashing;

import net.openhft.chronicle.algo.bytes.ReadAccess;

import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static net.openhft.chronicle.algo.hashing.LongHashFunction.NATIVE_LITTLE_ENDIAN;

/**
 * Adapted version of xxHash implementation from
 * https://github.com/Cyan4973/xxHash/releases/tag/r39, which is fully compatible with r40 though.
 * This implementation provides endian-independent hash values,
 * but it's slower on big-endian platforms.
 */
class XxHash_r39 {
    private static final XxHash_r39 INSTANCE = new XxHash_r39();

    // Primes if treated as unsigned
    private static final long P1 = -7046029288634856825L;
    private static final long P2 = -4417276706812531889L;
    private static final long P3 = 1609587929392839161L;
    private static final long P4 = -8796714831421723037L;
    private static final long P5 = 2870177450012600261L;

    // Private constructor to prevent instantiation
    private XxHash_r39() {
    }

    private static XxHash_r39 nativeXx() {
        return NATIVE_LITTLE_ENDIAN ? INSTANCE : BigEndian.INSTANCE;
    }

    /**
     * Final mixing step used by xxHash64.
     */
    private static long finalize(long hash) {
        hash ^= hash >>> 33;
        hash *= P2;
        hash ^= hash >>> 29;
        hash *= P3;
        hash ^= hash >>> 32;
        return hash;
    }

    /**
     * Returns a seedless xxHash64 {@link LongHashFunction}.
     */
    public static LongHashFunction asLongHashFunctionWithoutSeed() {
        return AsLongHashFunction.SEEDLESS_INSTANCE;
    }

    /**
     * Returns a seeded xxHash64 {@link LongHashFunction}.
     */
    public static LongHashFunction asLongHashFunctionWithSeed(long seed) {
        return new AsLongHashFunctionSeeded(seed);
    }

    /**
     * Fetches a 64-bit value from the given ReadAccess at the specified offset.
     *
     * @param access The ReadAccess instance
     * @param in     The input handle
     * @param off    The offset within the input
     * @param <T>    The type of the input handle
     * @return The 64-bit value read from the input
     */
    <T> long fetch64(ReadAccess<T> access, T in, long off) {
        return access.readLong(in, off);
    }

    /**
     * Fetches a 32-bit unsigned value from the given ReadAccess at the specified offset.
     *
     * @param access The ReadAccess instance
     * @param in     The input handle
     * @param off    The offset within the input
     * @param <T>    The type of the input handle
     * @return The 32-bit unsigned value read from the input
     */
    <T> long fetch32(ReadAccess<T> access, T in, long off) {
        return access.readUnsignedInt(in, off);
    }

    /**
     * Fetches an 8-bit unsigned value from the given ReadAccess at the specified offset.
     *
     * @param access The ReadAccess instance
     * @param in     The input handle
     * @param off    The offset within the input
     * @param <T>    The type of the input handle
     * @return The 8-bit unsigned value read from the input
     */
    <T> int fetch8(ReadAccess<T> access, T in, long off) {
        return access.readUnsignedByte(in, off);
    }

    /**
     * Converts the given 64-bit value to little-endian byte order.
     *
     * @param v The value to convert
     * @return The value in little-endian byte order
     */
    long toLittleEndian(long v) {
        return v;
    }

    /**
     * Converts the given 32-bit value to little-endian byte order.
     *
     * @param v The value to convert
     * @return The value in little-endian byte order
     */
    int toLittleEndian(int v) {
        return v;
    }

    /**
     * Converts the given 16-bit value to little-endian byte order.
     *
     * @param v The value to convert
     * @return The value in little-endian byte order
     */
    short toLittleEndian(short v) {
        return v;
    }

    /**
     * Computes the xxHash64 value for the given input using the specified seed.
     *
     * @param seed   The seed value for the hash function
     * @param input  The input handle
     * @param access The ReadAccess instance
     * @param off    The offset within the input
     * @param length The length of the input data
     * @param <T>    The type of the input handle
     * @return The computed hash value
     */
    public <T> long xxHash64(long seed, T input, ReadAccess<T> access, long off, long length) {
        long hash;
        long remaining = length;

        // Process 32-byte chunks
        if (remaining >= 32) {
            long v1 = seed + P1 + P2;
            long v2 = seed + P2;
            long v3 = seed;
            long v4 = seed - P1;

            do {
                v1 += fetch64(access, input, off) * P2;
                v1 = Long.rotateLeft(v1, 31);
                v1 *= P1;

                v2 += fetch64(access, input, off + 8) * P2;
                v2 = Long.rotateLeft(v2, 31);
                v2 *= P1;

                v3 += fetch64(access, input, off + 16) * P2;
                v3 = Long.rotateLeft(v3, 31);
                v3 *= P1;

                v4 += fetch64(access, input, off + 24) * P2;
                v4 = Long.rotateLeft(v4, 31);
                v4 *= P1;

                off += 32;
                remaining -= 32;
            } while (remaining >= 32);

            hash = Long.rotateLeft(v1, 1)
                    + Long.rotateLeft(v2, 7)
                    + Long.rotateLeft(v3, 12)
                    + Long.rotateLeft(v4, 18);

            v1 *= P2;
            v1 = Long.rotateLeft(v1, 31);
            v1 *= P1;
            hash ^= v1;
            hash = hash * P1 + P4;

            v2 *= P2;
            v2 = Long.rotateLeft(v2, 31);
            v2 *= P1;
            hash ^= v2;
            hash = hash * P1 + P4;

            v3 *= P2;
            v3 = Long.rotateLeft(v3, 31);
            v3 *= P1;
            hash ^= v3;
            hash = hash * P1 + P4;

            v4 *= P2;
            v4 = Long.rotateLeft(v4, 31);
            v4 *= P1;
            hash ^= v4;
            hash = hash * P1 + P4;
        } else {
            hash = seed + P5;
        }
        hash += length;

        // Process remaining 8-byte chunks
        while (remaining >= 8) {
            long k1 = fetch64(access, input, off);
            k1 *= P2;
            k1 = Long.rotateLeft(k1, 31);
            k1 *= P1;
            hash ^= k1;
            hash = Long.rotateLeft(hash, 27) * P1 + P4;
            off += 8;
            remaining -= 8;
        }

        // Process remaining 4-byte chunk
        if (remaining >= 4) {
            hash ^= fetch32(access, input, off) * P1;
            hash = Long.rotateLeft(hash, 23) * P2 + P3;
            off += 4;
            remaining -= 4;
        }

        // Process remaining bytes
        while (remaining != 0) {
            hash ^= fetch8(access, input, off) * P5;
            hash = Long.rotateLeft(hash, 11) * P1;
            --remaining;
            ++off;
        }

        // Finalize the hash value
        return finalize(hash);
    }

    /**
     * Inner class providing BigEndian-specific implementation of the xxHash algorithm.
     */
    private static class BigEndian extends XxHash_r39 {
        private static final BigEndian INSTANCE = new BigEndian();

        // Private constructor to prevent instantiation
        private BigEndian() {
        }

        @Override
        <T> long fetch64(ReadAccess<T> access, T in, long off) {
            // Reverse bytes for big-endian compatibility
            return Long.reverseBytes(super.fetch64(access, in, off));
        }

        @Override
        <T> long fetch32(ReadAccess<T> access, T in, long off) {
            // Reverse bytes for big-endian compatibility
            return Integer.reverseBytes(access.readInt(in, off)) & 0xFFFFFFFFL;
        }

        // fetch8 is not overloaded, because endianness doesn't matter for single byte

        @Override
        long toLittleEndian(long v) {
            // Reverse bytes for big-endian compatibility
            return Long.reverseBytes(v);
        }

        @Override
        int toLittleEndian(int v) {
            // Reverse bytes for big-endian compatibility
            return Integer.reverseBytes(v);
        }

        @Override
        short toLittleEndian(short v) {
            // Reverse bytes for big-endian compatibility
            return Short.reverseBytes(v);
        }
    }

    /**
     * Provides a LongHashFunction implementation using the xxHash algorithm without a seed.
     */
    private static class AsLongHashFunction extends LongHashFunction {
        public static final AsLongHashFunction SEEDLESS_INSTANCE = new AsLongHashFunction();
        private static final long serialVersionUID = 0L;

        // Ensure a singleton instance upon deserialization
        private Object readResolve() {
            return SEEDLESS_INSTANCE;
        }

        /**
         * Returns the seed value for the hash function.
         *
         * @return The seed value
         */
        public long seed() {
            return 0L;
        }

        @Override
        public long hashLong(long input) {
            // Convert input to little-endian and compute hash
            input = nativeXx().toLittleEndian(input);
            input *= P2;
            input = Long.rotateLeft(input, 31);
            input *= P1;
            long hash = seed() + P5 + 8;
            hash ^= input;
            hash = Long.rotateLeft(hash, 27) * P1 + P4;
            return XxHash_r39.finalize(hash);
        }

        @Override
        public long hashInt(int input) {
            // Convert input to little-endian and compute hash
            input = nativeXx().toLittleEndian(input);
            long hash = seed() + P5 + 4;
            hash ^= Primitives.unsignedInt(input) * P1;
            hash = Long.rotateLeft(hash, 23) * P2 + P3;
            return XxHash_r39.finalize(hash);
        }

        @Override
        public long hashShort(short input) {
            // Convert input to little-endian and compute hash
            input = nativeXx().toLittleEndian(input);
            long hash = seed() + P5 + 2;
            hash ^= Primitives.unsignedByte(input) * P5;
            hash = Long.rotateLeft(hash, 11) * P1;
            hash ^= Primitives.unsignedByte(input >> 8) * P5;
            hash = Long.rotateLeft(hash, 11) * P1;
            return XxHash_r39.finalize(hash);
        }

        @Override
        public long hashChar(char input) {
            return hashShort((short) input);
        }

        @Override
        public long hashByte(byte input) {
            // Compute hash for single byte
            long hash = seed() + P5 + 1;
            hash ^= Primitives.unsignedByte(input) * P5;
            hash = Long.rotateLeft(hash, 11) * P1;
            return XxHash_r39.finalize(hash);
        }

        @Override
        public long hashVoid() {
            return XxHash_r39.finalize(P5);
        }

        @Override
        public <T> long hash(T input, ReadAccess<T> access, long off, long len) {
            // Compute hash based on byte order of the input
            long seed = seed();
            if (access.byteOrder(input) == LITTLE_ENDIAN) {
                return XxHash_r39.INSTANCE.xxHash64(seed, input, access, off, len);
            } else {
                return BigEndian.INSTANCE.xxHash64(seed, input, access, off, len);
            }
        }
    }

    /**
     * Provides a LongHashFunction implementation using the xxHash algorithm with a seed.
     */
    private static class AsLongHashFunctionSeeded extends AsLongHashFunction {
        private static final long serialVersionUID = 0L;
        private final long seed;
        private final long voidHash;

        /**
         * Constructs a new AsLongHashFunctionSeeded instance with the given seed.
         *
         * @param seed The seed value for the hash function
         */
        private AsLongHashFunctionSeeded(long seed) {
            this.seed = seed;
            voidHash = XxHash_r39.finalize(seed + P5);
        }

        @Override
        public long seed() {
            return seed;
        }

        @Override
        public long hashVoid() {
            return voidHash;
        }
    }
}
