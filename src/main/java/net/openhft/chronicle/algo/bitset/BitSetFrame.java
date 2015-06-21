/*
 * Copyright 2014 Higher Frequency Trading http://www.higherfrequencytrading.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.algo.bitset;

import net.openhft.chronicle.algo.bytes.Access;

/**
 */
public interface BitSetFrame {
    /**
     * Returned if no entry is found
     */
    long NOT_FOUND = -1L;

    /**
     * Sets the bit at the specified index to the complement of its
     * current value.
     *
     * @param bitIndex the index of the bit to flip
     */
    <T> void flip(Access<T> access, T handle, long offset, long bitIndex);

    /**
     * Sets each bit from the specified {@code fromIndex} (inclusive) to the
     * specified {@code toIndex} (exclusive) to the complement of its current
     * value.
     *
     * @param fromIndex index of the first bit to flip
     * @param toIndex   index after the last bit to flip
     */
    <T> void flipRange(Access<T> access, T handle, long offset, long fromIndex, long toIndex);

    /**
     * Sets the bit at the specified index to {@code true}.
     *
     * @param bitIndex the bit index
     */
    <T> void set(Access<T> access, T handle, long offset, long bitIndex);

    /**
     * Sets the bit at the specified index to {@code true}.
     *
     * @param bitIndex the bit index
     * @return {@code true} if the bit was zeroOut, or false if the bit was already set
     */
    <T> boolean setIfClear(Access<T> access, T handle, long offset, long bitIndex);

    /**
     * Clears the bit at the specified index (sets it to {@code false}).
     *
     * @param bitIndex a bit index
     * @return the previous value of the bit at the specified index
     */
    <T> boolean clearIfSet(Access<T> access, T handle, long offset, long bitIndex);

    /**
     * Sets the bit at the specified index to the specified value.
     *
     * @param bitIndex the bit index
     * @param value    the boolean value to set
     */
    default <T> void set(Access<T> access, T handle, long offset, long bitIndex, boolean value) {
        if (value) {
            set(access, handle, offset, bitIndex);
        } else {
            clear(access, handle, offset, bitIndex);
        }
    }

    /**
     * Sets the bits from the specified {@code fromIndex} (inclusive) to the
     * specified {@code toIndex} (exclusive) to {@code true}.
     *
     * @param fromIndex index of the first bit to be set
     * @param toIndex   index after the last bit to be set
     */
    <T> void setRange(Access<T> access, T handle, long offset, long fromIndex, long toIndex);

    /**
     * Equivalent to {@code setRange(0, logicalSize())}.
     */
    <T> void setAll(Access<T> access, T handle, long offset);

    /**
     * Sets the bits from the specified {@code fromIndex} (inclusive) to the
     * specified {@code toIndex} (exclusive) to the specified value.
     *
     * @param fromIndex index of the first bit to be set
     * @param toIndex   index after the last bit to be set
     * @param value     value to set the selected bits to
     */
    default <T> void setRange(Access<T> access, T handle, long offset,
                 long fromIndex, long toIndex, boolean value) {
        if (value) {
            setRange(access, handle, offset, fromIndex, toIndex);
        } else {
            clearRange(access, handle, offset, fromIndex, toIndex);
        }
    }

    /**
     * Sets the bit specified by the index to {@code false}.
     *
     * @param bitIndex the index of the bit to be cleared
     */
    <T> void clear(Access<T> access, T handle, long offset, long bitIndex);

    /**
     * Sets the bits from the specified {@code fromIndex} (inclusive) to the
     * specified {@code toIndex} (exclusive) to {@code false}.
     *
     * @param fromIndex index of the first bit to be cleared
     * @param toIndex   index after the last bit to be cleared
     */
    <T> void clearRange(Access<T> access, T handle, long offset, long fromIndex, long toIndex);

    /**
     * Equivalent to {@code clearRange(0, logicalSize())}.
     */
    <T> void clearAll(Access<T> access, T handle, long offset);

    /**
     * Returns the value of the bit with the specified index. The value
     * is {@code true} if the bit with the index {@code bitIndex}
     * is currently set in this {@code DirectBitSet}; otherwise, the result
     * is {@code false}.
     *
     * @param bitIndex the bit index
     * @return the value of the bit with the specified index
     */
    <T> boolean get(Access<T> access, T handle, long offset, long bitIndex);

    /**
     * Synonym of {@link #get)}.
     *
     * @param bitIndex the bit index
     * @return the value of the bit with the specified index
     */
    default <T> boolean isSet(Access<T> access, T handle, long offset, long bitIndex) {
        return get(access, handle, offset, bitIndex);
    }

    /**
     * Checks if each bit from the specified {@code fromIndex} (inclusive) to the specified {@code
     * exclusiveToIndex} is set to {@code true}.
     *
     * @param fromIndex index of the first bit to check
     * @param toIndex index after the last bit to check
     * @return {@code true} if all bits in the specified range are set to {@code true},
     * {@code false} otherwise
     */
    <T> boolean isRangeSet(Access<T> access, T handle, long offset, long fromIndex, long toIndex);

    /**
     * Synonym of {@code !get(long)}.
     *
     * @param bitIndex the bit index
     * @return {@code true} is the bit at the specified index is clear in this
     *         bit set; if the bit is set to {@code true} then returns {@code false}
     */
    default <T> boolean isClear(Access<T> access, T handle, long offset, long bitIndex) {
        return !get(access, handle, offset, bitIndex);
    }

    /**
     * Checks if each bit from the specified {@code fromIndex} (inclusive) to the specified {@code
     * exclusiveToIndex} is set to {@code false}.
     *
     * @param fromIndex index of the first bit to check
     * @param toIndex index after the last bit to check
     * @return {@code true} if all bits in the specified range are set to {@code false},
     * {@code false} otherwise
     */
    <T> boolean isRangeClear(Access<T> access, T handle, long offset, long fromIndex, long toIndex);

    /**
     * Returns the index of the first bit that is set to {@code true}
     * that occurs on or after the specified starting index. If no such
     * bit exists then {@code -1} is returned.
     *
     * @param fromIndex the index to start checking from (inclusive)
     * @return the index of the next set bit, or {@code -1} if there is no such bit
     * @see #clearNextSetBit
     */
    <T> long nextSetBit(Access<T> access, T handle, long offset, long fromIndex);

    /**
     * Returns the index of the first bit that is set to {@code false}
     * that occurs on or after the specified starting index. If no such
     * bit exists then {@code -1} is returned.
     *
     * @param fromIndex the index to start checking from (inclusive)
     * @return the index of the next zeroOut bit, or {@code -1} if there is no such bit
     * @see #setNextClearBit
     */
    <T> long nextClearBit(Access<T> access, T handle, long offset, long fromIndex);

    /**
     * Returns the index of the nearest bit that is set to {@code true}
     * that occurs on or before the specified starting index.
     * If no such bit exists, or if {@code -1} is given as the
     * starting index, then {@code -1} is returned.
     *
     * @param fromIndex the index to start checking from (inclusive)
     * @return the index of the previous set bit, or {@code -1} if there is no such bit
     * @see #clearPreviousSetBit
     */
    <T> long previousSetBit(Access<T> access, T handle, long offset, long fromIndex);

    /**
     * Returns the index of the nearest bit that is set to {@code false}
     * that occurs on or before the specified starting index.
     * If no such bit exists, or if {@code -1} is given as the
     * starting index, then {@code -1} is returned.
     *
     * @param fromIndex the index to start checking from (inclusive)
     * @return the index of the previous zeroOut bit, or {@code -1} if there is no such bit
     * @see #setPreviousClearBit
     */
    <T> long previousClearBit(Access<T> access, T handle, long offset, long fromIndex);

    /**
     * Number of bits in this frame.
     * The index of the last bit in the set eligible to be set or zeroOut
     * is {@code logicalSize() - 1}.
     *
     * @return the number of bits in this bit set
     */
    long logicalSize();

    /**
     * Number of bytes taken by this frame.
     */
    long sizeInBytes();

    /**
     * Returns the number of bits set to {@code true} in the bit set.
     */
    <T> long cardinality(Access<T> access, T handle, long offset);

    /**
     * Finds and sets to {@code true} the first bit that is set to {@code false}
     * that occurs on or after the specified starting index. If no such
     * bit exists then {@code -1} is returned.
     *
     * @param fromIndex the index to start checking from (inclusive)
     * @return the index of the next zeroOut bit, or {@code -1} if there is no such bit
     * @see #nextClearBit
     */
    <T> long setNextClearBit(Access<T> access, T handle, long offset, long fromIndex);

    /**
     * Finds and clears the first bit that is set to {@code true}
     * that occurs on or after the specified starting index. If no such
     * bit exists then {@code -1} is returned.
     *
     * @param fromIndex the index to start checking from (inclusive)
     * @return the index of the next set bit, or {@code -1} if there is no such bit
     * @see #nextSetBit
     */
    <T> long clearNextSetBit(Access<T> access, T handle, long offset, long fromIndex);

    /**
     * Finds and sets to {@code true} the nearest bit that is set
     * to {@code false} that occurs on or before the specified starting index.
     * If no such bit exists, or if {@code -1} is given as the
     * starting index, then {@code -1} is returned.
     *
     * @param fromIndex the index to start checking from (inclusive)
     * @return the index of the previous zeroOut bit, or {@code -1} if there is no such bit
     * @see #previousClearBit
     */
    <T> long setPreviousClearBit(Access<T> access, T handle, long offset, long fromIndex);

    /**
     * Finds and clears the nearest bit that is set to {@code true}
     * that occurs on or before the specified starting index.
     * If no such bit exists, or if {@code -1} is given as the
     * starting index, then {@code -1} is returned.
     *
     * @param fromIndex the index to start checking from (inclusive)
     * @return the index of the previous set bit, or {@code -1} if there is no such bit
     * @see #previousSetBit
     */
    <T> long clearPreviousSetBit(Access<T> access, T handle, long offset, long fromIndex);

    /**
     * Finds the next {@code numberOfBits} consecutive bits set to {@code false},
     * starting from the specified {@code fromIndex}. Then all bits of the found
     * range are set to {@code true}. The first index of the found block
     * is returned. If there is no such range of clear bits, {@code -1}
     * is returned.
     *
     * <p>{@code fromIndex} could be the first index of the found range, thus
     * {@code setNextNContinuousClearBits(i, 1)} is exact equivalent of
     * {@code setNextClearBit(i)}.
     *
     * @param fromIndex the index to start checking from (inclusive)
     * @param numberOfBits how many continuous clear bits to search and set
     * @return the index of the first bit in the found range of clear bits,
     * or {@code -1} if there is no such range
     * @throws IndexOutOfBoundsException if {@code fromIndex} is negative
     * @throws IllegalArgumentException if {@code numberOfBits <= 0}
     */
    <T> long setNextNContinuousClearBits(Access<T> access, T handle, long offset,
                                         long fromIndex, int numberOfBits);

    /**
     * Finds the next {@code numberOfBits} consecutive bits set to {@code true},
     * starting from the specified {@code fromIndex}. Then all bits of the found
     * range are set to {@code false}. The first index of the found block
     * is returned. If there is no such range of {@code true} bits, {@code -1}
     * is returned.
     *
     * <p>{@code fromIndex} could be the first index of the found range, thus
     * {@code clearNextNContinuousSetBits(i, 1)} is exact equivalent of
     * {@code clearNextSetBit(i)}.
     *
     * @param fromIndex the index to start checking from (inclusive)
     * @param numberOfBits how many continuous set bits to search and clear
     * @return the index of the first bit in the found range
     * of {@code true} bits, or {@code -1} if there is no such range
     * @throws IndexOutOfBoundsException if {@code fromIndex} is negative
     * @throws IllegalArgumentException if {@code numberOfBits <= 0}
     */
    <T> long clearNextNContinuousSetBits(Access<T> access, T handle, long offset,
                                         long fromIndex, int numberOfBits);

    /**
     * Finds the previous {@code numberOfBits} consecutive bits
     * set to {@code false}, starting from the specified {@code fromIndex}.
     * Then all bits of the found range are set to {@code true}.
     * The first index of the found block is returned. If there is no such
     * range of clear bits, or if {@code -1} is given as the starting index,
     * {@code -1} is returned.
     *
     * <p>{@code fromIndex} could be the last index of the found range, thus
     * {@code setPreviousNContinuousClearBits(i, 1)} is exact equivalent of
     * {@code setPreviousClearBit(i)}.
     *
     * @param fromIndex the index to start checking from (inclusive)
     * @param numberOfBits how many continuous clear bits to search and set
     * @return the index of the first bit in the found range of clear bits,
     * or {@code -1} if there is no such range
     * @throws IndexOutOfBoundsException if {@code fromIndex} is less
     *                                   than {@code -1}
     * @throws IllegalArgumentException if {@code numberOfBits <= 0}
     */
    <T> long setPreviousNContinuousClearBits(Access<T> access, T handle, long offset,
                                             long fromIndex, int numberOfBits);

    /**
     * Finds the previous {@code numberOfBits} consecutive bits
     * set to {@code true}, starting from the specified {@code fromIndex}.
     * Then all bits of the found range are set to {@code false}.
     * The first index of the found block is returned. If there is no such
     * range of {@code true} bits, or if {@code -1} is given as the starting
     * index, {@code -1} is returned.
     *
     * <p>{@code fromIndex} could be the last index of the found range, thus
     * {@code clearPreviousNContinuousSetBits(i, 1)} is exact equivalent of
     * {@code clearPreviousSetBit(i)}.
     *
     * @param fromIndex the index to start checking from (inclusive)
     * @param numberOfBits how many continuous set bits to search and clear
     * @return the index of the first bit in the found range
     * of {@code true} bits, or {@code -1} if there is no such range
     * @throws IndexOutOfBoundsException if {@code fromIndex} is less
     *                                   than {@code -1}
     * @throws IllegalArgumentException if {@code numberOfBits <= 0}
     */
    <T> long clearPreviousNContinuousSetBits(Access<T> access, T handle, long offset,
                                             long fromIndex, int numberOfBits);

    /**
     * An iteration of bits in a bit set.
     *
     * <p>Usage idiom: <pre>{@code
     * Bits bits = bitSet.setBits();
     * for (long bit; (bit = bits.next()) >= 0;) {
     *     // do something with the bit
     * }}</pre>
     */
    interface Bits {

        <T> Bits reset(Access<T> access, T handle, long offset);

        /**
         * Returns index of the next bit in the iteration,
         * or {@code -1} if there are no more bits.
         *
         * @return index of the next bit in the iteration,
         * or {@code -1} if there are no more bits
         */
        <T> long next(Access<T> access, T handle, long offset);
    }

    /**
     * Returns an iteration of <i>set</i> bits in <i>direct</i> order
     * (from 0 to the end of the bit set).
     *
     * @return an iteration of <i>set</i> bits in <i>direct</i> order
     */
    Bits setBits();

    /**
     * An iteration of bits in a bit set.
     * <p>
     * <p>Usage idiom: <pre>{@code
     * Bits bits = bitSet.setBits();
     * for (long bit; (bit = bits.next()) >= 0;) {
     *     // do something with the bit
     * }}</pre>
     */
    interface Bits {

        <T> Bits reset(Access<T> access, T handle, long offset);

        /**
         * Returns index of the next bit in the iteration,
         * or {@code -1} if there are no more bits.
         *
         * @return index of the next bit in the iteration,
         * or {@code -1} if there are no more bits
         */
        <T> long next(Access<T> access, T handle, long offset);
    }

    BitSetAlgorithm algorithm();
}
