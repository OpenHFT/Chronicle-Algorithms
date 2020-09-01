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

/**
 * Most notable logical difference of this interface with {@code java.util.BitSet}
 * is that {@code DirectBitSet} has a rigid {@link #logicalSize},
 * attempts to {@link #get}, {@link #set} or {@link #clear} bits at indices
 * exceeding the size cause {@code IndexOutOfBoundsException}. There is also
 * a {@link #setAll()} method to set all bits within the size.
 * {@code java.util.BitSet} doesn't have such rigid capacity.
 *
 * @see java.util.BitSet
 */
public interface BitSet {
    /**
     * Returned if no entry is found
     */
    long NOT_FOUND = -1L;

    /**
     * Sets the bit at the specified index to the complement of its
     * current value.
     *
     * @param bitIndex the index of the bit to flip
     * @return this {@code DirectBitSet} back
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   {@code (index < 0 || index >= size())}
     */
    void flip(long bitIndex);

    /**
     * Sets each bit from the specified {@code fromIndex} (inclusive) to the
     * specified {@code toIndex} (exclusive) to the complement of its current
     * value.
     *
     * @param fromIndex index of the first bit to flip
     * @param toIndex   index after the last bit to flip
     * @return this {@code DirectBitSet} back
     * @throws IndexOutOfBoundsException if {@code fromIndex} is negative,
     *                                   or {@code fromIndex} is larger than {@code toIndex},
     *                                   or {@code toIndex} is larger or equal to {@code size()}
     */
    void flipRange(long fromIndex, long toIndex);

    /**
     * Sets the bit at the specified index to {@code true}.
     *
     * @param bitIndex a bit index
     * @return this {@code DirectBitSet} back
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   {@code (index < 0 || index >= size())}
     */
    void set(long bitIndex);

    /**
     * Sets the bit at the specified index to {@code true}.
     *
     * @param bitIndex a bit index
     * @return true if the bit was zeroOut, or false if the bit was already set.
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   {@code (index < 0 || index >= size())}
     */
    boolean setIfClear(long bitIndex);

    /**
     * Clears the bit at the specified index (sets it to {@code false}).
     *
     * @param bitIndex a bit index
     * @return the previous value of the bit at the specified index
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   {@code (index < 0 || index >= size())}
     */
    boolean clearIfSet(long bitIndex);

    /**
     * Sets the bit at the specified index to the specified value.
     *
     * @param bitIndex a bit index
     * @param value    a boolean value to set
     * @return this {@code DirectBitSet} back
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   {@code (index < 0 || index >= size())}
     */
    default void set(long bitIndex, boolean value) {
        if (value) {
            set(bitIndex);
        } else {
            clear(bitIndex);
        }
    }

    /**
     * Sets the bits from the specified {@code fromIndex} (inclusive) to the
     * specified {@code toIndex} (exclusive) to {@code true}.
     *
     * @param fromIndex index of the first bit to be set
     * @param toIndex   index after the last bit to be set
     * @return this {@code DirectBitSet} back
     * @throws IndexOutOfBoundsException if {@code fromIndex} is negative,
     *                                   or {@code fromIndex} is larger than {@code toIndex},
     *                                   or {@code toIndex} is larger or equal to {@code size()}
     */
    void setRange(long fromIndex, long toIndex);

    boolean isRangeSet(long fromIndex, long toIndex);

    /**
     * Sets all bits, {@code bs.setAll()} is equivalent
     * of {@code bs.set(0, bs.size()}.
     *
     * @return this bit set back
     */
    void setAll();

    /**
     * Sets the bits from the specified {@code fromIndex} (inclusive) to the
     * specified {@code toIndex} (exclusive) to the specified value.
     *
     * @param fromIndex index of the first bit to be set
     * @param toIndex   index after the last bit to be set
     * @param value     value to set the selected bits to
     * @return this {@code DirectBitSet} back
     * @throws IndexOutOfBoundsException if {@code fromIndex} is negative,
     *                                   or {@code fromIndex} is larger than {@code toIndex},
     *                                   or {@code toIndex} is larger or equal to {@code size()}
     */
    default void setRange(long fromIndex, long toIndex, boolean value) {
        if (value) {
            setRange(fromIndex, toIndex);
        } else {
            clearRange(fromIndex, toIndex);
        }
    }

    /**
     * Sets the bit specified by the index to {@code false}.
     *
     * @param bitIndex the index of the bit to be cleared
     * @return this {@code DirectBitSet} back
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   {@code (index < 0 || index >= size())}
     */
    void clear(long bitIndex);

    /**
     * Sets the bits from the specified {@code fromIndex} (inclusive) to the
     * specified {@code toIndex} (exclusive) to {@code false}.
     *
     * @param fromIndex index of the first bit to be cleared
     * @param toIndex   index after the last bit to be cleared
     * @return this {@code DirectBitSet} back
     * @throws IndexOutOfBoundsException if {@code fromIndex} is negative,
     *                                   or {@code fromIndex} is larger than {@code toIndex},
     *                                   or {@code toIndex} is larger or equal to {@code size()}
     */
    void clearRange(long fromIndex, long toIndex);

    boolean isRangeClear(long fromIndex, long toIndex);

    /**
     * Sets all of the bits in this BitSet to {@code false}.
     *
     * @return this {@code DirectBitSet} back
     */
    void clearAll();

    /**
     * Returns the value of the bit with the specified index. The value
     * is {@code true} if the bit with the index {@code bitIndex}
     * is currently set in this {@code DirectBitSet}; otherwise, the result
     * is {@code false}.
     *
     * @param bitIndex the bit index
     * @return the value of the bit with the specified index
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   {@code (index < 0 || index >= size())}
     */
    boolean get(long bitIndex);

    /**
     * Synonym of {@link #get(long)}.
     *
     * @param bitIndex the bit index
     * @return the value of the bit with the specified index
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   {@code (index < 0 || index >= size())}
     */
    default boolean isSet(long bitIndex) {
        return get(bitIndex);
    }

    /**
     * Synonym of {@code !get(long)}.
     *
     * @param bitIndex the bit index
     * @return {@code true} is the bit at the specified index is clear in this
     * bit set; if the bit is set to {@code true} then returns {@code false}
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   {@code (index < 0 || index >= size())}
     */
    default boolean isClear(long bitIndex) {
        return !get(bitIndex);
    }

    /**
     * Returns the index of the first bit that is set to {@code true}
     * that occurs on or after the specified starting index. If no such
     * bit exists then {@code -1} is returned.
     *
     * <p>To iterate over the {@code true} bits in a {@code DirectBitSet},
     * use the following loop:
     * <pre> {@code
     * for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i+1)) {
     * &nbsp;&nbsp;&nbsp;&nbsp;// operate on index i here
     * }}</pre>
     *
     * @param fromIndex the index to start checking from (inclusive)
     * @return the index of the next set bit, or {@code -1} if there
     * is no such bit
     * @throws IndexOutOfBoundsException if the specified index is negative
     * @see #clearNextSetBit(long)
     */
    long nextSetBit(long fromIndex);

    /**
     * Returns the index of the first bit that is set to {@code false}
     * that occurs on or after the specified starting index. If no such
     * bit exists then {@code -1} is returned.
     *
     * @param fromIndex the index to start checking from (inclusive)
     * @return the index of the next zeroOut bit,
     * or {@code -1} if there is no such bit
     * @throws IndexOutOfBoundsException if the specified index is negative
     * @see #setNextClearBit(long)
     */
    long nextClearBit(long fromIndex);

    /**
     * Returns the index of the nearest bit that is set to {@code true}
     * that occurs on or before the specified starting index.
     * If no such bit exists, or if {@code -1} is given as the
     * starting index, then {@code -1} is returned.
     *
     * <p>To iterate over the {@code true} bits in a {@code DirectBitSet},
     * use the following loop:
     * <pre> {@code
     * for (int i = bs.size(); (i = bs.previousSetBit(i-1)) >= 0; ) {
     * // operate on index i here
     * }}</pre>
     *
     * @param fromIndex the index to start checking from (inclusive)
     * @return the index of the previous set bit, or {@code -1} if there
     * is no such bit
     * @throws IndexOutOfBoundsException if the specified index is less
     *                                   than {@code -1}
     * @see #clearPreviousSetBit(long)
     */
    long previousSetBit(long fromIndex);

    /**
     * Returns the index of the nearest bit that is set to {@code false}
     * that occurs on or before the specified starting index.
     * If no such bit exists, or if {@code -1} is given as the
     * starting index, then {@code -1} is returned.
     *
     * @param fromIndex the index to start checking from (inclusive)
     * @return the index of the previous zeroOut bit, or {@code -1} if there
     * is no such bit
     * @throws IndexOutOfBoundsException if the specified index is less
     *                                   than {@code -1}
     * @see #setPreviousClearBit(long)
     */
    long previousClearBit(long fromIndex);

    /**
     * Returns the number of bits of space actually in use by this BitSet to represent bit values.
     * The index of the last bit in the set eligible to be set or zeroOut
     * is {@code size() - 1}.
     *
     * @return the number of bits in this bit set
     */
    long logicalSize();

    /**
     * Returns the number of bits set to {@code true} in this {@code DirectBitSet}.
     *
     * @return the number of bits set to {@code true} in this {@code DirectBitSet}
     */
    long cardinality();

    /**
     * Finds and sets to {@code true} the first bit that is set to {@code false}
     * that occurs on or after the specified starting index. If no such
     * bit exists then {@code -1} is returned.
     *
     * @param fromIndex the index to start checking from (inclusive)
     * @return the index of the next zeroOut bit,
     * or {@code -1} if there is no such bit
     * @throws IndexOutOfBoundsException if the specified index is negative
     * @see #nextClearBit(long)
     */
    long setNextClearBit(long fromIndex);

    /**
     * Finds and clears the first bit that is set to {@code true}
     * that occurs on or after the specified starting index. If no such
     * bit exists then {@code -1} is returned.
     *
     * @param fromIndex the index to start checking from (inclusive)
     * @return the index of the next set bit, or {@code -1} if there
     * is no such bit
     * @throws IndexOutOfBoundsException if the specified index is negative
     * @see #nextSetBit(long)
     */
    long clearNextSetBit(long fromIndex);

    /**
     * Finds and sets to {@code true} the nearest bit that is set
     * to {@code false} that occurs on or before the specified starting index.
     * If no such bit exists, or if {@code -1} is given as the
     * starting index, then {@code -1} is returned.
     *
     * @param fromIndex the index to start checking from (inclusive)
     * @return the index of the previous zeroOut bit, or {@code -1} if there
     * is no such bit
     * @throws IndexOutOfBoundsException if the specified index is less
     *                                   than {@code -1}
     * @see #previousClearBit(long)
     */
    long setPreviousClearBit(long fromIndex);

    /**
     * Finds and clears the nearest bit that is set to {@code true}
     * that occurs on or before the specified starting index.
     * If no such bit exists, or if {@code -1} is given as the
     * starting index, then {@code -1} is returned.
     *
     * @param fromIndex the index to start checking from (inclusive)
     * @return the index of the previous set bit, or {@code -1} if there
     * is no such bit
     * @throws IndexOutOfBoundsException if the specified index is less
     *                                   than {@code -1}
     * @see #previousSetBit(long)
     */
    long clearPreviousSetBit(long fromIndex);

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
     * @param fromIndex    the index to start checking from (inclusive)
     * @param numberOfBits how many continuous clear bits to search and set
     * @return the index of the first bit in the found range of clear bits,
     * or {@code -1} if there is no such range
     * @throws IndexOutOfBoundsException if {@code fromIndex} is negative
     * @throws IllegalArgumentException  if {@code numberOfBits <= 0}
     */
    long setNextNContinuousClearBits(long fromIndex, int numberOfBits);

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
     * @param fromIndex    the index to start checking from (inclusive)
     * @param numberOfBits how many continuous set bits to search and clear
     * @return the index of the first bit in the found range
     * of {@code true} bits, or {@code -1} if there is no such range
     * @throws IndexOutOfBoundsException if {@code fromIndex} is negative
     * @throws IllegalArgumentException  if {@code numberOfBits <= 0}
     */
    long clearNextNContinuousSetBits(long fromIndex, int numberOfBits);

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
     * @param fromIndex    the index to start checking from (inclusive)
     * @param numberOfBits how many continuous clear bits to search and set
     * @return the index of the first bit in the found range of clear bits,
     * or {@code -1} if there is no such range
     * @throws IndexOutOfBoundsException if {@code fromIndex} is less
     *                                   than {@code -1}
     * @throws IllegalArgumentException  if {@code numberOfBits <= 0}
     */
    long setPreviousNContinuousClearBits(long fromIndex, int numberOfBits);

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
     * @param fromIndex    the index to start checking from (inclusive)
     * @param numberOfBits how many continuous set bits to search and clear
     * @return the index of the first bit in the found range
     * of {@code true} bits, or {@code -1} if there is no such range
     * @throws IndexOutOfBoundsException if {@code fromIndex} is less
     *                                   than {@code -1}
     * @throws IllegalArgumentException  if {@code numberOfBits <= 0}
     */
    long clearPreviousNContinuousSetBits(long fromIndex, int numberOfBits);

    /**
     * Returns an iteration of <i>set</i> bits in <i>direct</i> order
     * (from 0 to the end of the bit set).
     *
     * @return an iteration of <i>set</i> bits in <i>direct</i> order
     */
    Bits setBits();

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

        Bits reset();

        /**
         * Returns index of the next bit in the iteration,
         * or {@code -1} if there are no more bits.
         *
         * @return index of the next bit in the iteration,
         * or {@code -1} if there are no more bits
         */
        long next();
    }
}
