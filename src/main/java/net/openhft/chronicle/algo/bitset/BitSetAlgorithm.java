//
// Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

package net.openhft.chronicle.algo.bitset;

/**
 * The {@code BitSetAlgorithm} interface defines the contract for algorithms
 * that handle operations related to bit sets. Implementations of this interface
 * provide methods to calculate the size in bytes required for a given logical size in bits
 * and to determine the maximum logical size that fits within the same size in bytes.
 */
public interface BitSetAlgorithm {

    /**
     * Calculates the size in bytes required to represent a given logical size in bits.
     *
     * @param logicalSize the logical size in bits
     * @return the size in bytes required to represent the logical size
     */
    long sizeInBytes(long logicalSize);

    /**
     * Returns the maximum logical size that can fit into the same size in bytes.
     * This method is used to determine the largest bit set that can be represented
     * without exceeding the byte size of the current logical size.
     *
     * @param logicalSize the logical size in bits
     * @return the maximum logical size that fits into the same size in bytes
     */
    long maxLogicalSizeFittingSameSizeInBytes(long logicalSize);
}
