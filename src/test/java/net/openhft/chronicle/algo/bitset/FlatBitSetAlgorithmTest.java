/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo.bitset;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FlatBitSetAlgorithmTest {
    @Test
    void testSizeInBytes() {
        long logicalSize = 64L;
        long expectedSizeInBytes = logicalSize / 8;
        assertEquals(expectedSizeInBytes, FlatBitSetAlgorithm.INSTANCE.sizeInBytes(logicalSize));
    }

    @Test
    void testMaxLogicalSizeFittingSameSizeInBytes() {
        long logicalSize = 64L;
        assertEquals(logicalSize, FlatBitSetAlgorithm.INSTANCE.maxLogicalSizeFittingSameSizeInBytes(logicalSize));
    }

    @Test
    void testSizeInBytesWithDifferentLogicalSize() {
        long logicalSize = 128L;
        long expectedSizeInBytes = logicalSize / 8;
        assertEquals(expectedSizeInBytes, FlatBitSetAlgorithm.INSTANCE.sizeInBytes(logicalSize));
    }

    @Test
    void testMaxLogicalSizeFittingSameSizeInBytesWithDifferentLogicalSize() {
        long logicalSize = 128L;
        assertEquals(logicalSize, FlatBitSetAlgorithm.INSTANCE.maxLogicalSizeFittingSameSizeInBytes(logicalSize));
    }

    @Test
    void testSizeInBytesWithZeroLogicalSize() {
        long logicalSize = 0L;
        long expectedSizeInBytes = 0L;
        assertEquals(expectedSizeInBytes, FlatBitSetAlgorithm.INSTANCE.sizeInBytes(logicalSize));
    }

    @Test
    void testMaxLogicalSizeFittingSameSizeInBytesWithZeroLogicalSize() {
        long logicalSize = 0L;
        assertEquals(logicalSize, FlatBitSetAlgorithm.INSTANCE.maxLogicalSizeFittingSameSizeInBytes(logicalSize));
    }
}