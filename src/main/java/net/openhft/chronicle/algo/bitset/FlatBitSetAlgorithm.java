//
// Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

/*
 * Copyright 2014-2020 chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
