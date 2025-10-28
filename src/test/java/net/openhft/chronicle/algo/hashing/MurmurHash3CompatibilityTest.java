/*
 * Copyright 2014-2025 chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.algo.hashing;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.stream.IntStream;

/**
 * Cross-check the MurmurHash3 implementation against Guava's reference for a wide
 * range of input sizes.
 */
class MurmurHash3CompatibilityTest {

    private static final int MAX_LEN = 256;

    static IntStream lengths() {
        return IntStream.rangeClosed(0, MAX_LEN);
    }

    @ParameterizedTest(name = "unseeded len={0}")
    @MethodSource("lengths")
    void unseededMatchesGuava(int len) {
        byte[] data = HashTestSupport.loopingBytes(len);
        long expected = lowerLong(Hashing.murmur3_128().hashBytes(data).asBytes());
        HashTestSupport.assertHashMatchesVectors(LongHashFunction.murmur_3(), data, expected);
    }

    @ParameterizedTest(name = "seeded len={0}")
    @MethodSource("lengths")
    void seededMatchesGuava(int len) {
        byte[] data = HashTestSupport.loopingBytes(len);
        HashFunction reference = Hashing.murmur3_128(42);
        long expected = lowerLong(reference.hashBytes(data).asBytes());
        HashTestSupport.assertHashMatchesVectors(LongHashFunction.murmur_3(42L), data, expected);
    }

    private static long lowerLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        return buffer.getLong();
    }
}
