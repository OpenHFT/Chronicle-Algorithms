/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo.bitset;

import static net.openhft.chronicle.algo.MemoryUnit.BITS;

/**
 * The {@code FlatBitSetAlgorithm} enum implements the {@link BitSetAlgorithm} interface
 * providing concrete implementations for the methods defined in the interface.
 * This enum represents a singleton instance of the algorithm used for BitSet operations.
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
