//
// Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

package net.openhft.chronicle.algo.hashing;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

public class MurmurHash3Test {

    @Test
    public void testMurmurWithoutSeed() {
        testMurmur(LongHashFunction.murmur_3(), Hashing.murmur3_128());
    }

    @Test
    public void testMurmurWithSeed() {
        testMurmur(LongHashFunction.murmur_3(42L), Hashing.murmur3_128(42));
    }

    private void testMurmur(LongHashFunction tested, HashFunction referenceFromGuava) {
        byte[] testData = new byte[1024];
        new Random().nextBytes(testData);
        for (int i = 0; i < testData.length; i++) {
            byte[] data = Arrays.copyOf(testData, i);
            LongHashFunctionTest.test(tested, data, referenceFromGuava.hashBytes(data).asLong());
        }
    }
}
