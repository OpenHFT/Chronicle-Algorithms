/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo.bitset;

import static net.openhft.chronicle.algo.MemoryUnit.BITS;

/**
 * Basic {@link BitSetAlgorithm} backed by a flat array of 64-bit words. Byte sizing is a straight
 * bits-to-bytes conversion, and the maximum logical size that fits the same byte length is the
 * logical size itself because no headers or metadata are stored.
 */
enum FlatBitSetAlgorithm implements BitSetAlgorithm {
    INSTANCE;

    /**
     * Calculates the size in bytes required to represent a given logical size in bits.
     *
     * @param logicalSize the logical size in bits
     * @return the size in bytes required to represent the logical size
     */
    @Override
    public long sizeInBytes(long logicalSize) {
        return BITS.toBytes(logicalSize);  // Convert bits to bytes
    }

    /**
     * Returns the maximum logical size that can fit into the same size in bytes.
     * This method simply returns the provided logical size.
     *
     * @param logicalSize the logical size in bits
     * @return the same logical size, as it fits into the same size in bytes
     */
    @Override
    public long maxLogicalSizeFittingSameSizeInBytes(long logicalSize) {
        return logicalSize;  // Return the logical size as it fits the same byte size
    }
}
