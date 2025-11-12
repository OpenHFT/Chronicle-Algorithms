/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo.hashing;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

class CityHashRegressionTest {

    private static final long[] CITY_WITHOUT_SEEDS = loadVector(
            "City64_1_1_Test.java", "HASHES_OF_LOOPING_BYTES_WITHOUT_SEEDS");
    private static final long[] CITY_WITH_SEEDS = loadVector(
            "City64_1_1_Test.java", "HASHES_OF_LOOPING_BYTES_WITH_SEEDS_0_0");
    private static final int MAX_LEN = Math.min(CITY_WITHOUT_SEEDS.length, CITY_WITH_SEEDS.length) - 1;

    static IntStream lengths() {
        return IntStream.rangeClosed(0, MAX_LEN);
    }

    @ParameterizedTest(name = "unseeded len={0}")
    @MethodSource("lengths")
    void unseededVectorsMatchReference(int len) {
        byte[] data = HashTestSupport.loopingBytes(len);
        HashTestSupport.assertHashMatchesVectors(LongHashFunction.city_1_1(),
                data,
                CITY_WITHOUT_SEEDS[len]);
    }

    @ParameterizedTest(name = "seeded len={0}")
    @MethodSource("lengths")
    void seededZeroZeroVectorsMatchReference(int len) {
        byte[] data = HashTestSupport.loopingBytes(len);
        HashTestSupport.assertHashMatchesVectors(LongHashFunction.city_1_1(0L, 0L),
                data,
                CITY_WITH_SEEDS[len]);
    }

    private static long[] loadVector(String fileName, String arrayName) {
        String content = ReferenceData.load(fileName);
        Pattern pattern = Pattern.compile(arrayName + "\\s*=\\s*(?:new\\s+long\\s*\\[\\s*\\]\\s*)?\\{([^}]*)\\}", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);
        if (!matcher.find()) {
            throw new IllegalStateException("Array " + arrayName + " not found in " + fileName);
        }
        String body = matcher.group(1);
        return Stream.of(body.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.endsWith("L") ? s.substring(0, s.length() - 1) : s)
                .mapToLong(Long::parseLong)
                .toArray();
    }
}
