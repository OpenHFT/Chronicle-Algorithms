/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo.hashing;

import net.openhft.chronicle.algo.bytes.ReadAccess;

import static java.lang.Long.reverseBytes;
import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static net.openhft.chronicle.algo.hashing.LongHashFunction.NATIVE_LITTLE_ENDIAN;

/**
 * Derived from https://github.com/google/guava/blob/fa95e381e665d8ee9639543b99ed38020c8de5ef
 * /guava/src/com/google/common/hash/Murmur3_128HashFunction.java
 */
@SuppressWarnings("fallthrough")
class MurmurHash_3 {
    // Singleton instance of MurmurHash_3
    private static final MurmurHash_3 INSTANCE = new MurmurHash_3();

    // Constants used in the hash function
    private static final long C1 = 0x87c37b91114253d5L;
    private static final long C2 = 0x4cf5ad432745937fL;

    // Private constructor to prevent instantiation
    private MurmurHash_3() {
    }

    private static MurmurHash_3 nativeMurmur() {
        return NATIVE_LITTLE_ENDIAN ? INSTANCE : BigEndian.INSTANCE;
    }

    /**
     * Finalizes the hash computation by mixing the hash values.
     *
     * @param length The length of the input data.
     * @param h1     The first hash value.
     * @param h2     The second hash value.
     * @return The final hash value.
     */
    private static long finalize(long length, long h1, long h2) {
        h1 ^= length;
        h2 ^= length;

        h1 += h2;
        h2 += h1;

        h1 = fmix64(h1);
        h2 = fmix64(h2);

        h1 += h2;
        return h1;
    }

    /**
     * Mixes the bits of the given value.
     *
     * @param k The value to mix.
     * @return The mixed value.
     */
    private static long fmix64(long k) {
        k ^= k >>> 33;
        k *= 0xff51afd7ed558ccdL;
        k ^= k >>> 33;
        k *= 0xc4ceb9fe1a85ec53L;
        k ^= k >>> 33;
        return k;
    }

    /**
     * Mixes the first key for the hash function.
     *
     * @param k1 The first key.
     * @return The mixed key.
     */
    private static long mixK1(long k1) {
        k1 *= C1;
        k1 = Long.rotateLeft(k1, 31);
        k1 *= C2;
        return k1;
    }

    /**
     * Mixes the second key for the hash function.
     *
     * @param k2 The second key.
     * @return The mixed key.
     */
    private static long mixK2(long k2) {
        k2 *= C2;
        k2 = Long.rotateLeft(k2, 33);
        k2 *= C1;
        return k2;
    }

    /**
     * Returns an instance of LongHashFunction implementing the MurmurHash3 algorithm without a seed.
     *
     * @return An instance of LongHashFunction.
     */
    public static LongHashFunction asLongHashFunctionWithoutSeed() {
        return AsLongHashFunction.INSTANCE;
    }

    /**
     * Returns an instance of LongHashFunction implementing the MurmurHash3 algorithm with a seed.
     *
     * @param seed The seed value.
     * @return An instance of LongHashFunction with the given seed.
     */
    public static LongHashFunction asLongHashFunctionWithSeed(long seed) {
        return new AsLongHashFunctionSeeded(seed);
    }

    /**
     * Fetches a 64-bit value from the input.
     *
     * @param access The read access strategy.
     * @param in     The input object.
     * @param off    The offset within the input.
     * @param <T>    The type of the input object.
     * @return The fetched 64-bit value.
     */
    <T> long fetch64(ReadAccess<T> access, T in, long off) {
        return access.readLong(in, off);
    }

    /**
     * Fetches a 32-bit value from the input.
     *
     * @param access The read access strategy.
     * @param in     The input object.
     * @param off    The offset within the input.
     * @param <T>    The type of the input object.
     * @return The fetched 32-bit value.
     */
    <T> int fetch32(ReadAccess<T> access, T in, long off) {
        return access.readInt(in, off);
    }

    /**
     * Converts a 64-bit value to little-endian byte order.
     *
     * @param v The value to convert.
     * @return The value in little-endian byte order.
     */
    long toLittleEndian(long v) {
        return v;
    }

    /**
     * Converts a 32-bit value to little-endian byte order.
     *
     * @param v The value to convert.
     * @return The value in little-endian byte order.
     */
    int toLittleEndian(int v) {
        return v;
    }

    /**
     * Converts an unsigned short value to little-endian byte order.
     *
     * @param unsignedShort The unsigned short value to convert.
     * @return The value in little-endian byte order.
     */
    int toLittleEndianShort(int unsignedShort) {
        return unsignedShort;
    }

    /**
     * Computes the MurmurHash3 hash value for the given input.
     *
     * @param seed   The seed value.
     * @param input  The input object.
     * @param access The read access strategy.
     * @param offset The offset within the input.
     * @param length The length of the input.
     * @param <T>    The type of the input object.
     * @return The computed hash value.
     */
    @SuppressWarnings("fallthrough")
    public <T> long hash(long seed, T input, ReadAccess<T> access, long offset, long length) {
        long h1 = seed;
        long h2 = seed;
        long remaining = length;
        while (remaining >= 16L) {
            long blockOffset = offset;
            long k1 = fetch64(access, input, blockOffset);
            offset = blockOffset + 16L;
            remaining -= 16L;
            h1 ^= mixK1(k1);

            h1 = Long.rotateLeft(h1, 27);
            h1 += h2;
            h1 = h1 * 5L + 0x52dce729L;

            long k2 = fetch64(access, input, blockOffset + 8L);
            h2 ^= mixK2(k2);

            h2 = Long.rotateLeft(h2, 31);
            h2 += h1;
            h2 = h2 * 5L + 0x38495ab5L;
        }
        if (remaining > 0L) {
            int tailLength = (int) remaining;
            long k1 = tailK1(access, input, offset, tailLength);
            long k2 = tailK2(access, input, offset, tailLength);
            h1 ^= mixK1(k1);
            h2 ^= mixK2(k2);
        }
        return finalize(length, h1, h2);
    }

    private <T> long tailK1(ReadAccess<T> access, T input, long offset, int remaining) {
        if (remaining >= 8) {
            return fetch64(access, input, offset);
        }
        long k1 = 0L;
        switch (remaining) {
            case 7:
                k1 ^= ((long) access.readUnsignedByte(input, offset + 6L)) << 48; // fall through
            case 6:
                k1 ^= ((long) access.readUnsignedByte(input, offset + 5L)) << 40; // fall through
            case 5:
                k1 ^= ((long) access.readUnsignedByte(input, offset + 4L)) << 32; // fall through
            case 4:
                k1 ^= Primitives.unsignedInt(fetch32(access, input, offset));
                break;
            case 3:
                k1 ^= ((long) access.readUnsignedByte(input, offset + 2L)) << 16; // fall through
            case 2:
                k1 ^= ((long) access.readUnsignedByte(input, offset + 1L)) << 8; // fall through
            case 1:
                k1 ^= access.readUnsignedByte(input, offset);
            default:
                break;
        }
        return k1;
    }

    private <T> long tailK2(ReadAccess<T> access, T input, long offset, int remaining) {
        if (remaining <= 8) {
            return 0L;
        }
        long k2 = 0L;
        switch (remaining) {
            case 15:
                k2 ^= ((long) access.readUnsignedByte(input, offset + 14L)) << 48; // fall through
            case 14:
                k2 ^= ((long) access.readUnsignedByte(input, offset + 13L)) << 40; // fall through
            case 13:
                k2 ^= ((long) access.readUnsignedByte(input, offset + 12L)) << 32; // fall through
            case 12:
                k2 ^= ((long) access.readUnsignedByte(input, offset + 11L)) << 24; // fall through
            case 11:
                k2 ^= ((long) access.readUnsignedByte(input, offset + 10L)) << 16; // fall through
            case 10:
                k2 ^= ((long) access.readUnsignedByte(input, offset + 9L)) << 8; // fall through
            case 9:
                k2 ^= access.readUnsignedByte(input, offset + 8L);
            default:
                break;
        }
        return k2;
    }

    /**
     * Big-endian implementation of MurmurHash3.
     */
    private static class BigEndian extends MurmurHash_3 {
        // Singleton instance of BigEndian
        private static final BigEndian INSTANCE = new BigEndian();

        // Private constructor to prevent instantiation
        private BigEndian() {
        }

        @Override
        <T> long fetch64(ReadAccess<T> access, T in, long off) {
            return reverseBytes(super.fetch64(access, in, off));
        }

        @Override
        <T> int fetch32(ReadAccess<T> access, T in, long off) {
            return Integer.reverseBytes(super.fetch32(access, in, off));
        }

        @Override
        long toLittleEndian(long v) {
            return reverseBytes(v);
        }

        @Override
        int toLittleEndian(int v) {
            return Integer.reverseBytes(v);
        }

        @Override
        int toLittleEndianShort(int unsignedShort) {
            return ((unsignedShort & 0xFF) << 8) | (unsignedShort >> 8);
        }
    }

    /**
     * Implementation of LongHashFunction using MurmurHash3.
     */
    private static class AsLongHashFunction extends LongHashFunction {
        // Singleton instance of AsLongHashFunction
        public static final AsLongHashFunction INSTANCE = new AsLongHashFunction();
        private static final long serialVersionUID = 0L;

        // Ensures singleton pattern after deserialization
        private Object readResolve() {
            return INSTANCE;
        }

        /**
         * Returns the seed value.
         *
         * @return The seed value.
         */
        long seed() {
            return 0L;
        }

        /**
         * Computes the hash value for the given native long value.
         *
         * @param nativeLong The native long value.
         * @param len        The length of the value.
         * @return The computed hash value.
         */
        long hashNativeLong(long nativeLong, long len) {
            long h1 = mixK1(nativeLong);
            long h2 = 0L;
            return MurmurHash_3.finalize(len, h1, h2);
        }

        @Override
        public long hashLong(long input) {
            return hashNativeLong(nativeMurmur().toLittleEndian(input), 8L);
        }

        @Override
        public long hashInt(int input) {
            return hashNativeLong(Primitives.unsignedInt(nativeMurmur().toLittleEndian(input)), 4L);
        }

        @Override
        public long hashShort(short input) {
            return hashNativeLong(
                    nativeMurmur().toLittleEndianShort(Primitives.unsignedShort(input)), 2L);
        }

        @Override
        public long hashChar(char input) {
            return hashNativeLong(nativeMurmur().toLittleEndianShort(input), 2L);
        }

        @Override
        public long hashByte(byte input) {
            return hashNativeLong(Primitives.unsignedByte(input), 1L);
        }

        @Override
        public long hashVoid() {
            return 0L;
        }

        @Override
        public <T> long hash(T input, ReadAccess<T> access, long off, long len) {
            long seed = seed();
            if (access.byteOrder(input) == LITTLE_ENDIAN) {
                return MurmurHash_3.INSTANCE.hash(seed, input, access, off, len);
            } else {
                return BigEndian.INSTANCE.hash(seed, input, access, off, len);
            }
        }
    }

    /**
     * Implementation of LongHashFunction using MurmurHash3 with a seed value.
     */
    private static class AsLongHashFunctionSeeded extends AsLongHashFunction {
        private static final long serialVersionUID = 0L;

        // The seed value
        private final long seed;
        // The precomputed hash value for an empty input
        private final long voidHash;

        /**
         * Constructs an instance with the given seed.
         *
         * @param seed The seed value.
         */
        private AsLongHashFunctionSeeded(long seed) {
            this.seed = seed;
            voidHash = MurmurHash_3.finalize(0L, seed, seed);
        }

        @Override
        long seed() {
            return seed;
        }

        @Override
        long hashNativeLong(long nativeLong, long len) {
            long seed = this.seed;
            long h1 = seed ^ mixK1(nativeLong);
            return MurmurHash_3.finalize(len, h1, seed);
        }

        @Override
        public long hashVoid() {
            return voidHash;
        }
    }
}
