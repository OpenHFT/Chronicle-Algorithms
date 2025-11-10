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
