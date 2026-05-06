/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
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
