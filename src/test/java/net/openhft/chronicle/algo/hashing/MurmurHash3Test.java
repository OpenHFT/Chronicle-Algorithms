/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo.hashing;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MurmurHash3Test {

    @Test
    public void testMurmurWithoutSeed() {
        LongHashFunction tested = LongHashFunction.murmur_3();
        HashFunction referenceFromGuava = Hashing.murmur3_128();
        byte[] sample = new byte[0];
        assertEquals(referenceFromGuava.hashBytes(sample).asLong(),
                tested.hashBytes(sample),
                "hashBytes matches Guava seed=0 len=0");
        testMurmur(tested, referenceFromGuava);
    }

    @Test
    public void testMurmurWithSeed() {
        LongHashFunction tested = LongHashFunction.murmur_3(42L);
        HashFunction referenceFromGuava = Hashing.murmur3_128(42);
        byte[] sample = new byte[0];
        assertEquals(referenceFromGuava.hashBytes(sample).asLong(),
                tested.hashBytes(sample),
                "hashBytes matches Guava seed=42 len=0");
        testMurmur(tested, referenceFromGuava);
    }

    private void testMurmur(LongHashFunction tested, HashFunction referenceFromGuava) {
        byte[] testData = new byte[1024];
        ThreadLocalRandom.current().nextBytes(testData);
        for (int i = 0; i < testData.length; i++) {
            byte[] data = Arrays.copyOf(testData, i);
            LongHashFunctionTestUtils.test(tested, data, referenceFromGuava.hashBytes(data).asLong());
        }
    }
}
