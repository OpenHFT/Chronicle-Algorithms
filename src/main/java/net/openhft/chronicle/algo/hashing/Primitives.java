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

package net.openhft.chronicle.algo.hashing;

/**
 * This is the Primitives class providing utility methods for working with primitive data types.
 * It includes methods for converting signed integer values to their unsigned equivalents.
 * The class is designed to be a utility class with static methods and a private constructor
 * to prevent instantiation.
 */
final class Primitives {

    // Private constructor to prevent instantiation
    private Primitives() {
    }

    /**
     * Converts a signed int value to an unsigned long value.
     * The conversion ensures that the value is treated as an unsigned 32-bit integer.
     *
     * @param i The signed int value to convert
     * @return The unsigned long value
     */
    static long unsignedInt(int i) {
        return i & 0xFFFFFFFFL;
    }

    /**
     * Converts a signed short value to an unsigned int value.
     * The conversion ensures that the value is treated as an unsigned 16-bit integer.
     *
     * @param s The signed short value to convert
     * @return The unsigned int value
     */
    static int unsignedShort(int s) {
        return s & 0xFFFF;
    }

    /**
     * Converts a signed byte value to an unsigned int value.
     * The conversion ensures that the value is treated as an unsigned 8-bit integer.
     *
     * @param b The signed byte value to convert
     * @return The unsigned int value
     */
    static int unsignedByte(int b) {
        return b & 0xFF;
    }
}
