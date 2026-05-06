/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algorithms.measures;

import net.openhft.chronicle.algo.bytes.NativeAccess;
import net.openhft.chronicle.algo.hashing.LongHashFunction;
import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.bytes.PointerBytesStore;
import net.openhft.chronicle.bytes.algo.OptimisedBytesStoreHash;

import java.security.SecureRandom;
import java.util.Random;

/**
 * Collection of {@link AddressWrapper} implementations used to exercise hashing strategies over
 * native memory.
 * <p>
 * Each constant implements {@link #setAddress(long, long)} and {@link #hash()} using a different
 * hashing approach so quality and performance can be compared by the score harnesses.
 */
public enum AddressWrappers implements AddressWrapper {
    /**
     * Returns a random {@code long} irrespective of the provided address.
     */
    RANDOM {
        Random rand = new Random();

        @Override
        public void setAddress(long address, long length) {
        }

        @Override
        public long hash() {
            return rand.nextLong();
        }
    },
    /**
     * Uses {@link SecureRandom} to return unpredictable values.
     */
    SECURE_RANDOM {
        SecureRandom rand = new SecureRandom();

        @Override
        public void setAddress(long address, long length) {
        }

        @Override
        public long hash() {
            return rand.nextLong();
        }
    },

    /**
     * Wraps the address as a {@link Bytes} and hashes using {@link OptimisedBytesStoreHash}.
     */
    VANILLA {
        int length;
        Bytes bytes;

        @Override
        public void setAddress(long address, long length) {
            this.length = (int) length;
            PointerBytesStore pbs = BytesStore.nativePointer();
            pbs.set(address, length);
            bytes = pbs.bytesForRead().unchecked(true);
        }

        @Override
        public long hash() {
            return OptimisedBytesStoreHash.applyAsLong32bytesMultiple(bytes, length);
        }
    },
    /**
     * Hashes the memory region using {@link LongHashFunction#city_1_1()}.
     */
    CITY_1_1 {
        long address,length;

        @Override
        public void setAddress(long address, long length) {
            this.address = address;
            this.length = length;
        }

        @Override
        public long hash() {
            return LongHashFunction.city_1_1().hash((Object) null, NativeAccess.instance(), address, length);
        }
    },
    /**
     * Hashes the memory region using {@link LongHashFunction#murmur_3()}.
     */
    MURMUR_3 {
        long address,length;

        @Override
        public void setAddress(long address, long length) {
            this.address = address;
            this.length = length;
        }

        @Override
        public long hash() {
            return LongHashFunction.murmur_3().hash((Object) null, NativeAccess.instance(), address, length);
        }
    },
    /**
     * Builds a 32-bit hash by iterating over bytes as characters and applying the JDK
     * {@link java.util.HashMap} agitation function to reduce collisions.
     */
    STRING32 {
        Bytes bytes;

        @Override
        public void setAddress(long address, long length) {
            PointerBytesStore pbs = BytesStore.nativePointer();
            pbs.set(address, length);
            bytes = pbs.bytesForRead().unchecked(true);
        }

        @Override
        public long hash() {
            int hc = 0;
            for (int i = 0; i < bytes.length(); i++)
                hc = hc * 31 + bytes.charAt(i);

            return hash(hc);
        }

        // from the hash() function in HashMap
        int hash(int h) {
            // This function ensures that hashCodes that differ only by
            // constant multiples at each bit position have a bounded
            // number of collisions (approximately 8 at default load factor).
            h ^= (h >>> 20) ^ (h >>> 12);
            return h ^ (h >>> 7) ^ (h >>> 4);
        }
    },
    /**
     * Builds a 64-bit hash using a similar approach to {@link #STRING32} but keeping more entropy.
     */
    STRING64 {
        Bytes bytes;

        @Override
        public void setAddress(long address, long length) {
            PointerBytesStore pbs = BytesStore.nativePointer();
            pbs.set(address, length);
            bytes = pbs.bytesForRead().unchecked(true);
        }

        @Override
        public long hash() {
            long hc = 0;
            for (int i = 0; i < bytes.length(); i++)
                hc = hc * 31 + bytes.charAt(i);
            return hash(hc);
        }

        // based on the hash() function in HashMap
        long hash(long h) {
            h ^= (h >>> 41) ^ (h >>> 23);
            return h ^ (h >>> 14) ^ (h >>> 7);
        }
    },
    /**
     * Generates the raw 32-bit polynomial hash without the extra agitation step.
     */
    STRING32_WITHOUT_AGITATE {
        Bytes bytes;

        @Override
        public void setAddress(long address, long length) {
            PointerBytesStore pbs = BytesStore.nativePointer();
            pbs.set(address, length);
            bytes = pbs.bytesForRead().unchecked(true);
        }

        @Override
        public long hash() {
            int hc = 0;
            for (int i = 0; i < bytes.length(); i++)
                hc = hc * 31 + bytes.charAt(i);

            return hc;
        }
    }
}
