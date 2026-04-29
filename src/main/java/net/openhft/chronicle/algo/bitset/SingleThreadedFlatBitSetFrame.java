/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo.bitset;

import net.openhft.chronicle.algo.bytes.Access;

import static java.lang.Long.numberOfLeadingZeros;
import static java.lang.Long.numberOfTrailingZeros;
import static net.openhft.chronicle.algo.MemoryUnit.BITS;
import static net.openhft.chronicle.algo.MemoryUnit.LONGS;

/**
 * This is the SingleThreadedFlatBitSetFrame class implementing BitSetFrame.
 * It provides methods for bit manipulation with input validations.
 * This class is not thread-safe.
 */
public final class SingleThreadedFlatBitSetFrame implements BitSetFrame {

    public static final long ALL_ONES = ~0L;

    private final long longLength;

    /**
     * Creates a new {@code SingleThreadedFlatBitSetFrame} of the given logical size.
     *
     * @param logicalSize the logical bit set size, should be {@code long}-aligned
     *                    (i.e. a multiple of 8)
     * @throws IllegalArgumentException is the given logicalSize is not a multiple of 8
     *                                  or non-positive
     */
    public SingleThreadedFlatBitSetFrame(long logicalSize) {
        if (logicalSize <= 0) {
            throw new IllegalArgumentException("Logical size should be positive, " +
                    logicalSize + " given");
        }
        longLength = BITS.toLongs(logicalSize);
        if (LONGS.toBits(longLength) != logicalSize) {
            throw new IllegalArgumentException(
                    "logical size should be long-aligned (i.e. a multiple of 8), " +
                            logicalSize + " given");
        }
    }

    // Utility methods for bit manipulation and conversions
    static long singleBit(long bitIndex) {
        return 1L << bitIndex;
    }

    static long higherBitsIncludingThis(long bitIndex) {
        return ALL_ONES << bitIndex;
    }

    static long lowerBitsIncludingThis(long bitIndex) {
        return ALL_ONES >>> ~bitIndex;
    }
// conversions

    static long higherBitsExcludingThis(long bitIndex) {
        return ~(ALL_ONES >>> ~bitIndex);
    }

    static long lowerBitsExcludingThis(long bitIndex) {
        return ~(ALL_ONES << bitIndex);
    }

    static long longWithThisBit(long bitIndex) {
        return bitIndex >> 6;
    }

    static long firstByte(long offset, long longIndex) {
        return offset + (longIndex << 3);
    }

    static long firstBit(long longIndex) {
        return longIndex << 6;
    }

    static long lastBit(long longIndex) {
        return firstBit(longIndex) + 63;
    }

    static void checkNumberOfBits(int numberOfBits) {
        if (numberOfBits <= 0 || numberOfBits > 64)
            throw new IllegalArgumentException("Illegal number of bits: " + numberOfBits);
    }
// checks

    static boolean checkNotFoundIndex(long fromIndex) {
        if (fromIndex < 0) {
            if (fromIndex == NOT_FOUND)
                return true;
            throw new IndexOutOfBoundsException("from index: " + fromIndex);
        }
        return false;
    }

    static void checkFromIndex(long fromIndex) {
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException("from index: " + fromIndex);
    }

    long byteWithThisBit(long offset, long bitIndex) {
        return offset + ((bitIndex >> 6) << 3);
    }

    private boolean checkIndex(long bitIndex) {
        if (bitIndex < 0 || (bitIndex >> 6) >= longLength) {
            throw new IndexOutOfBoundsException(
                    "index: " + bitIndex + ", logical size: " + LONGS.toBits(longLength));
        }
        return true;
    }

    private boolean checkFromTo(long fromIndex, long exclusiveToIndex, long toLongIndex) {
        if (fromIndex < 0 || fromIndex > exclusiveToIndex || toLongIndex >= longLength) {
            throw new IndexOutOfBoundsException(
                    "index range: [" + fromIndex + ", " + exclusiveToIndex + "), " +
                            "logical size: " + LONGS.toBits(longLength));
        }
        return true;
    }

    private <T> long readLong(Access<T> access, T handle, long offset, long longIndex) {
        return access.readLong(handle, firstByte(offset, longIndex));
    }

    private <T> void writeLong(Access<T> access, T handle, long offset,
                               long longIndex, long toWrite) {
        access.writeLong(handle, firstByte(offset, longIndex), toWrite);
    }

    /**
     * Flips the bit at the specified index.
     *
     * @param access   the access interface for reading/writing bits
     * @param handle   the handle to the underlying data structure
     * @param offset   the offset within the data structure
     * @param bitIndex the index of the bit to flip
     * @param <T>      the type of the handle
     */
    @Override
    public <T> void flip(Access<T> access, T handle, long offset, long bitIndex) {
        assert checkIndex(bitIndex);
        long byteIndex = byteWithThisBit(offset, bitIndex);
        long mask = singleBit(bitIndex);
        long l = access.readLong(handle, byteIndex);
        long l2 = l ^ mask;
        access.writeLong(handle, byteIndex, l2);
    }

    /**
     * Flips the bits from the specified {@code fromIndex} (inclusive) to the
     * specified {@code toIndex} (exclusive).
     *
     * @param access           the access interface for reading/writing bits
     * @param handle           the handle to the underlying data structure
     * @param offset           the offset within the data structure
     * @param fromIndex        the index of the first bit to flip (inclusive)
     * @param exclusiveToIndex the index after the last bit to flip (exclusive)
     * @param <T>              the type of the handle
     */
    @Override
    public <T> void flipRange(Access<T> access, T handle, long offset,
                              long fromIndex, long exclusiveToIndex) {
        long fromLongIndex = longWithThisBit(fromIndex);
        long toIndex = exclusiveToIndex - 1;
        long toLongIndex = longWithThisBit(toIndex);
        assert checkFromTo(fromIndex, exclusiveToIndex, toLongIndex);

        if (fromLongIndex != toLongIndex) {
            long firstFullLongIndex = fromLongIndex;
            if ((fromIndex & 63) != 0) {
                long fromByteIndex = firstByte(offset, fromLongIndex);
                long mask = higherBitsIncludingThis(fromIndex);
                long l = access.readLong(handle, fromByteIndex);
                long l2 = l ^ mask;
                access.writeLong(handle, fromByteIndex, l2);
                firstFullLongIndex++;
            }
            if ((exclusiveToIndex & 63) == 0) {
                for (long i = firstFullLongIndex; i <= toLongIndex; i++) {
                    writeLong(access, handle, offset, i, ~readLong(access, handle, offset, i));
                }
            } else {
                for (long i = firstFullLongIndex; i < toLongIndex; i++) {
                    writeLong(access, handle, offset, i, ~readLong(access, handle, offset, i));
                }
                long toByteIndex = firstByte(offset, toLongIndex);
                long mask = lowerBitsIncludingThis(toIndex);
                long l = access.readLong(handle, toByteIndex);
                long l2 = l ^ mask;
                access.writeLong(handle, toByteIndex, l2);
            }
        } else {
            long byteIndex = firstByte(offset, fromLongIndex);
            long mask = higherBitsIncludingThis(fromIndex) & lowerBitsIncludingThis(toIndex);
            long l = access.readLong(handle, byteIndex);
            long l2 = l ^ mask;
            access.writeLong(handle, byteIndex, l2);
        }
    }

    @Override
    public <T> void set(Access<T> access, T handle, long offset, long bitIndex) {
        assert checkIndex(bitIndex);  // Ensure the bit index is within bounds
        long byteIndex = byteWithThisBit(offset, bitIndex);  // Calculate the byte index for the bit
        long mask = singleBit(bitIndex);  // Create a mask for the specific bit
        long l = access.readLong(handle, byteIndex);  // Read the current value at the byte index
        if ((l & mask) != 0)  // If the bit is already set, return
            return;
        long l2 = l | mask;  // Set the bit
        access.writeLong(handle, byteIndex, l2);  // Write the new value back
    }

    /**
     * Sets the bit at the specified index to {@code true} if it is not already set.
     *
     * @param access   the access interface for reading/writing bits
     * @param handle   the handle to the underlying data structure
     * @param offset   the offset within the data structure
     * @param bitIndex the index of the bit to set
     * @param <T>      the type of the handle
     * @return {@code true} if the bit was set, {@code false} if it was already set
     */
    @Override
    public <T> boolean setIfClear(Access<T> access, T handle, long offset, long bitIndex) {
        assert checkIndex(bitIndex);  // Ensure the bit index is within bounds
        long byteIndex = byteWithThisBit(offset, bitIndex);  // Calculate the byte index for the bit
        long mask = singleBit(bitIndex);  // Create a mask for the specific bit
        long l = access.readLong(handle, byteIndex);  // Read the current value at the byte index
        long l2 = l | mask;  // Set the bit
        if (l == l2)  // If the bit was already set, return false
            return false;
        access.writeLong(handle, byteIndex, l2);  // Write the new value back
        return true;  // Return true as the bit was set
    }

    /**
     * Sets the bits from the specified {@code fromIndex} (inclusive) to the
     * specified {@code toIndex} (exclusive) to {@code true}.
     *
     * @param access           the access interface for reading/writing bits
     * @param handle           the handle to the underlying data structure
     * @param offset           the offset within the data structure
     * @param fromIndex        the index of the first bit to set (inclusive)
     * @param exclusiveToIndex the index after the last bit to set (exclusive)
     * @param <T>              the type of the handle
     */
    @Override
    public <T> void setRange(Access<T> access, T handle, long offset,
                             long fromIndex, long exclusiveToIndex) {
        long fromLongIndex = longWithThisBit(fromIndex);  // Calculate the long index for the start bit
        long toIndex = exclusiveToIndex - 1;
        long toLongIndex = longWithThisBit(toIndex);  // Calculate the long index for the end bit
        assert checkFromTo(fromIndex, exclusiveToIndex, toLongIndex);  // Ensure the range is valid

        if (fromLongIndex != toLongIndex) {
            long firstFullLongIndex = fromLongIndex;
            if ((fromIndex & 63) != 0) {  // Handle bits in the first partial long
                long fromByteIndex = firstByte(offset, fromLongIndex);
                long mask = higherBitsIncludingThis(fromIndex);
                long l = access.readLong(handle, fromByteIndex);
                long l2 = l | mask;
                access.writeLong(handle, fromByteIndex, l2);
                firstFullLongIndex++;
            }
            if ((exclusiveToIndex & 63) == 0) {  // Handle bits in the full longs
                for (long i = firstFullLongIndex; i <= toLongIndex; i++) {
                    writeLong(access, handle, offset, i, ALL_ONES);
                }
            } else {  // Handle bits in the last partial long
                for (long i = firstFullLongIndex; i < toLongIndex; i++) {
                    writeLong(access, handle, offset, i, ALL_ONES);
                }
                long toByteIndex = firstByte(offset, toLongIndex);
                long mask = lowerBitsIncludingThis(toIndex);
                long l = access.readLong(handle, toByteIndex);
                long l2 = l | mask;
                access.writeLong(handle, toByteIndex, l2);
            }
        } else {  // Handle bits within a single long
            long byteIndex = firstByte(offset, fromLongIndex);
            long mask = higherBitsIncludingThis(fromIndex) & lowerBitsIncludingThis(toIndex);
            long l = access.readLong(handle, byteIndex);
            long l2 = l | mask;
            access.writeLong(handle, byteIndex, l2);
        }
    }

    /**
     * Sets all bits in the bit set to {@code true}.
     *
     * @param access the access interface for reading/writing bits
     * @param handle the handle to the underlying data structure
     * @param offset the offset within the data structure
     * @param <T>    the type of the handle
     */
    @Override
    public <T> void setAll(Access<T> access, T handle, long offset) {
        for (long i = 0; i < longLength; i++) {  // Iterate through all longs
            writeLong(access, handle, offset, i, ALL_ONES);  // Set all bits to 1
        }
    }

    /**
     * Clears the bit at the specified index.
     *
     * @param access   the access interface for reading/writing bits
     * @param handle   the handle to the underlying data structure
     * @param offset   the offset within the data structure
     * @param bitIndex the index of the bit to clear
     * @param <T>      the type of the handle
     */
    @Override
    public <T> void clear(Access<T> access, T handle, long offset, long bitIndex) {
        assert checkIndex(bitIndex);  // Ensure the bit index is within bounds
        long byteIndex = byteWithThisBit(offset, bitIndex);  // Calculate the byte index for the bit
        long mask = singleBit(bitIndex);  // Create a mask for the specific bit
        long l = access.readLong(handle, byteIndex);  // Read the current value at the byte index
        if ((l & mask) == 0)  // If the bit is already clear, return
            return;
        long l2 = l & ~mask;  // Clear the bit
        access.writeLong(handle, byteIndex, l2);  // Write the new value back
    }

    /**
     * Clears the bit at the specified index if it is currently set.
     *
     * @param access   the access interface for reading/writing bits
     * @param handle   the handle to the underlying data structure
     * @param offset   the offset within the data structure
     * @param bitIndex the index of the bit to clear
     * @param <T>      the type of the handle
     * @return {@code true} if the bit was cleared, {@code false} if it was already clear
     */
    @Override
    public <T> boolean clearIfSet(Access<T> access, T handle, long offset, long bitIndex) {
        assert checkIndex(bitIndex);  // Ensure the bit index is within bounds
        long byteIndex = byteWithThisBit(offset, bitIndex);  // Calculate the byte index for the bit
        long mask = singleBit(bitIndex);  // Create a mask for the specific bit
        long l = access.readLong(handle, byteIndex);  // Read the current value at the byte index
        if ((l & mask) == 0)  // If the bit is already clear, return false
            return false;
        long l2 = l & ~mask;  // Clear the bit
        access.writeLong(handle, byteIndex, l2);  // Write the new value back
        return true;  // Return true as the bit was cleared
    }

    /**
     * Clears the bits from the specified {@code fromIndex} (inclusive) to the
     * specified {@code toIndex} (exclusive).
     *
     * @param access           the access interface for reading/writing bits
     * @param handle           the handle to the underlying data structure
     * @param offset           the offset within the data structure
     * @param fromIndex        the index of the first bit to clear (inclusive)
     * @param exclusiveToIndex the index after the last bit to clear (exclusive)
     * @param <T>              the type of the handle
     */
    @Override
    public <T> void clearRange(Access<T> access, T handle, long offset,
                               long fromIndex, long exclusiveToIndex) {
        long fromLongIndex = longWithThisBit(fromIndex);  // Calculate the long index for the start bit
        long toIndex = exclusiveToIndex - 1;
        long toLongIndex = longWithThisBit(toIndex);  // Calculate the long index for the end bit
        assert checkFromTo(fromIndex, exclusiveToIndex, toLongIndex);  // Ensure the range is valid

        if (fromLongIndex != toLongIndex) {
            long firstFullLongIndex = fromLongIndex;
            if ((fromIndex & 63) != 0) {  // Handle bits in the first partial long
                long fromByteIndex = firstByte(offset, fromLongIndex);
                long mask = higherBitsIncludingThis(fromIndex);
                long l = access.readLong(handle, fromByteIndex);
                long l2 = l & ~mask;
                access.writeLong(handle, fromByteIndex, l2);
                firstFullLongIndex++;
            }
            if ((exclusiveToIndex & 63) == 0) {  // Handle bits in the full longs
                for (long i = firstFullLongIndex; i <= toLongIndex; i++) {
                    writeLong(access, handle, offset, i, 0L);
                }
            } else {  // Handle bits in the last partial long
                for (long i = firstFullLongIndex; i < toLongIndex; i++) {
                    writeLong(access, handle, offset, i, 0L);
                }
                long toByteIndex = firstByte(offset, toLongIndex);
                long mask = lowerBitsIncludingThis(toIndex);
                long l = access.readLong(handle, toByteIndex);
                long l2 = l & ~mask;
                access.writeLong(handle, toByteIndex, l2);
            }
        } else {  // Handle bits within a single long
            long byteIndex = firstByte(offset, fromLongIndex);
            long mask = higherBitsIncludingThis(fromIndex) & lowerBitsIncludingThis(toIndex);
            long l = access.readLong(handle, byteIndex);
            long l2 = l & ~mask;
            access.writeLong(handle, byteIndex, l2);
        }
    }

    /**
     * Checks if all bits in the specified range are set to {@code true}.
     *
     * @param access           the access interface for reading bits
     * @param handle           the handle to the underlying data structure
     * @param offset           the offset within the data structure
     * @param fromIndex        the index of the first bit to check (inclusive)
     * @param exclusiveToIndex the index after the last bit to check (exclusive)
     * @param <T>              the type of the handle
     * @return {@code true} if all bits in the range are set to {@code true}, {@code false} otherwise
     */
    @Override
    public <T> boolean isRangeSet(Access<T> access, T handle, long offset,
                                  long fromIndex, long exclusiveToIndex) {
        long fromLongIndex = longWithThisBit(fromIndex);  // Calculate the long index for the start bit
        long toIndex = exclusiveToIndex - 1;
        long toLongIndex = longWithThisBit(toIndex);  // Calculate the long index for the end bit
        assert checkFromTo(fromIndex, exclusiveToIndex, toLongIndex);  // Ensure the range is valid

        if (fromLongIndex != toLongIndex) {
            long firstFullLongIndex = fromLongIndex;
            if ((fromIndex & 63) != 0) {  // Check bits in the first partial long
                long mask = higherBitsIncludingThis(fromIndex);
                if ((~(readLong(access, handle, offset, fromLongIndex)) & mask) != 0L)
                    return false;
                firstFullLongIndex++;
            }
            if ((exclusiveToIndex & 63) == 0) {  // Check bits in the full longs
                for (long i = firstFullLongIndex; i <= toLongIndex; i++) {
                    if (~readLong(access, handle, offset, i) != 0L)
                        return false;
                }
                return true;
            } else {  // Check bits in the last partial long
                for (long i = firstFullLongIndex; i < toLongIndex; i++) {
                    if (~readLong(access, handle, offset, i) != 0L)
                        return false;
                }
                long mask = lowerBitsIncludingThis(toIndex);
                return ((~readLong(access, handle, offset, toLongIndex)) & mask) == 0L;
            }
        } else {  // Check bits within a single long
            long mask = higherBitsIncludingThis(fromIndex) & lowerBitsIncludingThis(toIndex);
            return ((~readLong(access, handle, offset, fromLongIndex)) & mask) == 0L;
        }
    }

    /**
     * Checks if all bits in the specified range are clear (set to {@code false}).
     *
     * @param access           the access interface for reading bits
     * @param handle           the handle to the underlying data structure
     * @param offset           the offset within the data structure
     * @param fromIndex        the index of the first bit to check (inclusive)
     * @param exclusiveToIndex the index after the last bit to check (exclusive)
     * @param <T>              the type of the handle
     * @return {@code true} if all bits in the range are clear, {@code false} otherwise
     */
    @Override
    public <T> boolean isRangeClear(Access<T> access, T handle, long offset,
                                    long fromIndex, long exclusiveToIndex) {
        long fromLongIndex = longWithThisBit(fromIndex);  // Calculate the long index for the start bit
        long toIndex = exclusiveToIndex - 1;
        long toLongIndex = longWithThisBit(toIndex);  // Calculate the long index for the end bit
        assert checkFromTo(fromIndex, exclusiveToIndex, toLongIndex);  // Ensure the range is valid

        if (fromLongIndex != toLongIndex) {
            long firstFullLongIndex = fromLongIndex;
            if ((fromIndex & 63) != 0) {  // Check bits in the first partial long
                long mask = higherBitsIncludingThis(fromIndex);
                if ((readLong(access, handle, offset, fromLongIndex) & mask) != 0L)
                    return false;
                firstFullLongIndex++;
            }
            if ((exclusiveToIndex & 63) == 0) {  // Check bits in the full longs
                for (long i = firstFullLongIndex; i <= toLongIndex; i++) {
                    if (readLong(access, handle, offset, i) != 0L)
                        return false;
                }
                return true;
            } else {  // Check bits in the last partial long
                for (long i = firstFullLongIndex; i < toLongIndex; i++) {
                    if (readLong(access, handle, offset, i) != 0L)
                        return false;
                }
                long mask = lowerBitsIncludingThis(toIndex);
                return (readLong(access, handle, offset, toLongIndex) & mask) == 0L;
            }
        } else {  // Check bits within a single long
            long mask = higherBitsIncludingThis(fromIndex) & lowerBitsIncludingThis(toIndex);
            return (readLong(access, handle, offset, fromLongIndex) & mask) == 0L;
        }
    }

    /**
     * Clears all bits in the bit set.
     *
     * @param access the access interface for writing bits
     * @param handle the handle to the underlying data structure
     * @param offset the offset within the data structure
     * @param <T>    the type of the handle
     */
    @Override
    public <T> void clearAll(Access<T> access, T handle, long offset) {
        access.writeBytes(handle, offset, LONGS.toBytes(longLength), (byte) 0);  // Clear all bytes
    }

    /**
     * Returns the value of the bit with the specified index.
     *
     * @param access   the access interface for reading bits
     * @param handle   the handle to the underlying data structure
     * @param offset   the offset within the data structure
     * @param bitIndex the index of the bit to check
     * @param <T>      the type of the handle
     * @return {@code true} if the bit is set, {@code false} otherwise
     */
    @Override
    public <T> boolean get(Access<T> access, T handle, long offset, long bitIndex) {
        assert checkIndex(bitIndex);  // Ensure the bit index is within bounds
        long byteIndex = byteWithThisBit(offset, bitIndex);  // Calculate the byte index for the bit
        long l = access.readLong(handle, byteIndex);  // Read the value at the byte index
        return (l & (singleBit(bitIndex))) != 0;  // Check if the specific bit is set
    }

    /**
     * Returns the index of the first bit that is set to {@code true} on or after the specified starting index.
     *
     * @param access    the access interface for reading bits
     * @param handle    the handle to the underlying data structure
     * @param offset    the offset within the data structure
     * @param fromIndex the index to start checking from (inclusive)
     * @param <T>       the type of the handle
     * @return the index of the next set bit, or {@code -1} if there is no such bit
     */
    @Override
    public <T> long nextSetBit(Access<T> access, T handle, long offset, long fromIndex) {
        checkFromIndex(fromIndex);  // Ensure the start index is valid
        long fromLongIndex = longWithThisBit(fromIndex);  // Calculate the long index for the start bit
        if (fromLongIndex >= longLength)
            return NOT_FOUND;  // Return -1 if the start index is out of bounds
        long l = readLong(access, handle, offset, fromLongIndex) >>> fromIndex;  // Check the current long
        if (l != 0) {
            return fromIndex + numberOfTrailingZeros(l);  // Return the index of the first set bit
        }
        for (long i = fromLongIndex + 1; i < longLength; i++) {  // Check subsequent longs
            l = readLong(access, handle, offset, i);
            if (l != 0)
                return firstBit(i) + numberOfTrailingZeros(l);
        }
        return NOT_FOUND;  // Return -1 if no set bit is found
    }

    /**
     * Returns an iteration of <i>set</i> bits in <i>direct</i> order (from 0 to the end of the bit set).
     *
     * @return an iteration of <i>set</i> bits in <i>direct</i> order
     */
    @Override
    public Bits setBits() {
        return new SetBits();
    }

    /**
     * Returns the bit set algorithm used by this bit set.
     *
     * @return the bit set algorithm
     */
    @Override
    public BitSetAlgorithm algorithm() {
        return FlatBitSetAlgorithm.INSTANCE;
    }

    /**
     * Clears the next bit that is set to {@code true} on or after the specified starting index.
     *
     * @param access    the access interface for reading and writing bits
     * @param handle    the handle to the underlying data structure
     * @param offset    the offset within the data structure
     * @param fromIndex the index to start checking from (inclusive)
     * @param <T>       the type of the handle
     * @return the index of the next set bit that was cleared, or {@code -1} if there is no such bit
     */
    @Override
    public <T> long clearNextSetBit(Access<T> access, T handle, long offset, long fromIndex) {
        checkFromIndex(fromIndex);  // Ensure the start index is valid
        long fromLongIndex = longWithThisBit(fromIndex);  // Calculate the long index for the start bit
        if (fromLongIndex >= longLength)
            return NOT_FOUND;  // Return -1 if the start index is out of bounds
        long fromByteIndex = firstByte(offset, fromLongIndex);
        long w = access.readLong(handle, fromByteIndex);
        long l = w >>> fromIndex;
        if (l != 0) {
            long indexOfSetBit = fromIndex + numberOfTrailingZeros(l);
            long mask = singleBit(indexOfSetBit);
            access.writeLong(handle, fromByteIndex, w ^ mask);  // Clear the set bit
            return indexOfSetBit;  // Return the index of the cleared bit
        }
        for (long i = fromLongIndex + 1; i < longLength; i++) {  // Check subsequent longs
            long byteIndex = firstByte(offset, i);
            l = access.readLong(handle, byteIndex);
            if (l != 0) {
                long indexOfSetBit = firstBit(i) + numberOfTrailingZeros(l);
                long mask = singleBit(indexOfSetBit);
                access.writeLong(handle, byteIndex, l ^ mask);  // Clear the set bit
                return indexOfSetBit;  // Return the index of the cleared bit
            }
        }
        return NOT_FOUND;  // Return -1 if no set bit is found
    }

    /**
     * Returns the index of the first bit that is set to {@code false} on or after the specified starting index.
     *
     * @param access    the access interface for reading bits
     * @param handle    the handle to the underlying data structure
     * @param offset    the offset within the data structure
     * @param fromIndex the index to start checking from (inclusive)
     * @param <T>       the type of the handle
     * @return the index of the next clear bit, or {@code -1} if there is no such bit
     */
    @Override
    public <T> long nextClearBit(Access<T> access, T handle, long offset, long fromIndex) {
        checkFromIndex(fromIndex);  // Ensure the start index is valid
        long fromLongIndex = longWithThisBit(fromIndex);  // Calculate the long index for the start bit
        if (fromLongIndex >= longLength)
            return NOT_FOUND;  // Return -1 if the start index is out of bounds
        long l = (~readLong(access, handle, offset, fromLongIndex)) >>> fromIndex;  // Check the current long
        if (l != 0) {
            return fromIndex + numberOfTrailingZeros(l);  // Return the index of the first clear bit
        }
        for (long i = fromLongIndex + 1; i < longLength; i++) {  // Check subsequent longs
            l = ~readLong(access, handle, offset, i);
            if (l != 0)
                return firstBit(i) + numberOfTrailingZeros(l);
        }
        return NOT_FOUND;  // Return -1 if no clear bit is found
    }

    /**
     * Sets the next bit that is set to {@code false} on or after the specified starting index.
     *
     * @param access    the access interface for reading and writing bits
     * @param handle    the handle to the underlying data structure
     * @param offset    the offset within the data structure
     * @param fromIndex the index to start checking from (inclusive)
     * @param <T>       the type of the handle
     * @return the index of the next clear bit that was set, or {@code -1} if there is no such bit
     */
    @Override
    public <T> long setNextClearBit(Access<T> access, T handle, long offset, long fromIndex) {
        checkFromIndex(fromIndex);  // Ensure the start index is valid
        long fromLongIndex = longWithThisBit(fromIndex);  // Calculate the long index for the start bit
        if (fromLongIndex >= longLength)
            return NOT_FOUND;  // Return -1 if the start index is out of bounds
        long fromByteIndex = firstByte(offset, fromLongIndex);
        long w = access.readLong(handle, fromByteIndex);
        long l = (~w) >>> fromIndex;
        if (l != 0) {
            long indexOfClearBit = fromIndex + numberOfTrailingZeros(l);
            long mask = singleBit(indexOfClearBit);
            access.writeLong(handle, fromByteIndex, w ^ mask);  // Set the clear bit
            return indexOfClearBit;  // Return the index of the set bit
        }
        for (long i = fromLongIndex + 1; i < longLength; i++) {  // Check subsequent longs
            long byteIndex = firstByte(offset, i);
            w = access.readLong(handle, byteIndex);
            l = ~w;
            if (l != 0) {
                long indexOfClearBit = firstBit(i) + numberOfTrailingZeros(l);
                long mask = singleBit(indexOfClearBit);
                access.writeLong(handle, byteIndex, w ^ mask);  // Set the clear bit
                return indexOfClearBit;  // Return the index of the set bit
            }
        }
        return NOT_FOUND;  // Return -1 if no clear bit is found
    }

    /**
     * Returns the index of the last bit that is set to {@code true} on or before the specified starting index.
     *
     * @param access    the access interface for reading bits
     * @param handle    the handle to the underlying data structure
     * @param offset    the offset within the data structure
     * @param fromIndex the index to start checking from (inclusive)
     * @param <T>       the type of the handle
     * @return the index of the previous set bit, or {@code -1} if there is no such bit
     */
    @Override
    public <T> long previousSetBit(Access<T> access, T handle, long offset, long fromIndex) {
        if (checkNotFoundIndex(fromIndex))  // Ensure the start index is valid
            return NOT_FOUND;
        long fromLongIndex = longWithThisBit(fromIndex);  // Calculate the long index for the start bit
        if (fromLongIndex >= longLength) {
            fromLongIndex = longLength - 1;  // Adjust the start index if out of bounds
            fromIndex = logicalSize() - 1;
        }
        // Shift to check bits from the start index downwards
        long l = readLong(access, handle, offset, fromLongIndex) << ~fromIndex;
        if (l != 0)
            return fromIndex - numberOfLeadingZeros(l);  // Return the index of the previous set bit
        for (long i = fromLongIndex - 1; i >= 0; i--) {  // Check previous longs
            l = readLong(access, handle, offset, i);
            if (l != 0)
                return lastBit(i) - numberOfLeadingZeros(l);
        }
        return NOT_FOUND;  // Return -1 if no set bit is found
    }

    /**
     * Returns the index of the last bit that is set to {@code true} within the specified range.
     *
     * @param access           the access interface for reading bits
     * @param handle           the handle to the underlying data structure
     * @param offset           the offset within the data structure
     * @param fromIndex        the index to start checking from (inclusive)
     * @param inclusiveToIndex the index to check to (inclusive)
     * @param <T>              the type of the handle
     * @return the index of the previous set bit within the range, or {@code -1} if there is no such bit
     */
    private <T> long previousSetBit(Access<T> access, T handle, long offset,
                                    long fromIndex, long inclusiveToIndex) {
        long fromLongIndex = longWithThisBit(fromIndex);  // Calculate the long index for the start bit
        long toLongIndex = longWithThisBit(inclusiveToIndex);  // Calculate the long index for the end bit
        assert checkFromTo(inclusiveToIndex, fromIndex + 1, toLongIndex);  // Ensure the range is valid
        if (fromLongIndex >= longLength) {
            fromLongIndex = longLength - 1;  // Adjust the start index if out of bounds
            fromIndex = logicalSize() - 1;
        }
        if (fromLongIndex != toLongIndex) {
            // << ~fromIndex === << (63 - (fromIndex & 63))
            long l = readLong(access, handle, offset, fromLongIndex) << ~fromIndex;
            if (l != 0)
                return fromIndex - numberOfLeadingZeros(l);  // Return the index of the previous set bit
            for (long i = fromLongIndex - 1; i > toLongIndex; i--) {  // Check previous longs
                l = readLong(access, handle, offset, i);
                if (l != 0)
                    return lastBit(i) - numberOfLeadingZeros(l);
            }
            fromIndex = lastBit(toLongIndex);
        }
        long w = readLong(access, handle, offset, toLongIndex);
        long mask = higherBitsIncludingThis(inclusiveToIndex) & lowerBitsIncludingThis(fromIndex);
        long l = w & mask;
        if (l != 0) {
            return lastBit(toLongIndex) - numberOfLeadingZeros(l);
        }
        return NOT_FOUND;  // Return -1 if no set bit is found
    }

    /**
     * Clears the previous set bit (set to {@code true}) on or before the specified starting index.
     *
     * @param access    the access interface for reading and writing bits
     * @param handle    the handle to the underlying data structure
     * @param offset    the offset within the data structure
     * @param fromIndex the index to start checking from (inclusive)
     * @param <T>       the type of the handle
     * @return the index of the previous set bit that was cleared, or {@code -1} if there is no such bit
     */
    @Override
    public <T> long clearPreviousSetBit(Access<T> access, T handle, long offset, long fromIndex) {
        if (checkNotFoundIndex(fromIndex))
            return NOT_FOUND;
        long fromLongIndex = longWithThisBit(fromIndex);
        if (fromLongIndex >= longLength) {
            fromLongIndex = longLength - 1;
            fromIndex = logicalSize() - 1;
        }
        long fromByteIndex = firstByte(offset, fromLongIndex);
        long w = access.readLong(handle, fromByteIndex);
        long l = w << ~fromIndex;
        if (l != 0) {
            long indexOfSetBit = fromIndex - numberOfLeadingZeros(l);
            long mask = singleBit(indexOfSetBit);
            access.writeLong(handle, fromByteIndex, w ^ mask);
            return indexOfSetBit;
        }
        for (long i = fromLongIndex - 1; i >= 0; i--) {
            long byteIndex = firstByte(offset, i);
            l = access.readLong(handle, byteIndex);
            if (l != 0) {
                long indexOfSetBit = lastBit(i) - numberOfLeadingZeros(l);
                long mask = singleBit(indexOfSetBit);
                access.writeLong(handle, byteIndex, l ^ mask);
                return indexOfSetBit;
            }
        }
        return NOT_FOUND;
    }

    /**
     * Returns the index of the last bit that is set to {@code false} on or before the specified starting index.
     *
     * @param access    the access interface for reading bits
     * @param handle    the handle to the underlying data structure
     * @param offset    the offset within the data structure
     * @param fromIndex the index to start checking from (inclusive)
     * @param <T>       the type of the handle
     * @return the index of the previous clear bit, or {@code -1} if there is no such bit
     */
    @Override
    public <T> long previousClearBit(Access<T> access, T handle, long offset, long fromIndex) {
        if (checkNotFoundIndex(fromIndex))
            return NOT_FOUND;
        long fromLongIndex = longWithThisBit(fromIndex);
        if (fromLongIndex >= longLength) {
            fromLongIndex = longLength - 1;
            fromIndex = logicalSize() - 1;
        }
        long l = (~readLong(access, handle, offset, fromLongIndex)) << ~fromIndex;
        if (l != 0)
            return fromIndex - numberOfLeadingZeros(l);
        for (long i = fromLongIndex - 1; i >= 0; i--) {
            l = ~readLong(access, handle, offset, i);
            if (l != 0)
                return lastBit(i) - numberOfLeadingZeros(l);
        }
        return NOT_FOUND;
    }

    /**
     * Sets the previous bit that is set to {@code false} on or before the specified starting index.
     *
     * @param access    the access interface for reading and writing bits
     * @param handle    the handle to the underlying data structure
     * @param offset    the offset within the data structure
     * @param fromIndex the index to start checking from (inclusive)
     * @param <T>       the type of the handle
     * @return the index of the previous clear bit that was set, or {@code -1} if there is no such bit
     */
    @Override
    public <T> long setPreviousClearBit(Access<T> access, T handle, long offset, long fromIndex) {
        if (checkNotFoundIndex(fromIndex))
            return NOT_FOUND;
        long fromLongIndex = longWithThisBit(fromIndex);
        if (fromLongIndex >= longLength) {
            fromLongIndex = longLength - 1;
            fromIndex = logicalSize() - 1;
        }
        long fromByteIndex = firstByte(offset, fromLongIndex);
        long w = access.readLong(handle, fromByteIndex);
        long l = (~w) << ~fromIndex;
        if (l != 0) {
            long indexOfClearBit = fromIndex - numberOfLeadingZeros(l);
            long mask = singleBit(indexOfClearBit);
            access.writeLong(handle, fromByteIndex, w ^ mask);
            return indexOfClearBit;
        }
        for (long i = fromLongIndex - 1; i >= 0; i--) {
            long byteIndex = firstByte(offset, i);
            w = access.readLong(handle, byteIndex);
            l = ~w;
            if (l != 0) {
                long indexOfClearBit = lastBit(i) - numberOfLeadingZeros(l);
                long mask = singleBit(indexOfClearBit);
                access.writeLong(handle, byteIndex, w ^ mask);
                return indexOfClearBit;
            }
        }
        return NOT_FOUND;
    }

    /**
     * Returns the logical size (number of bits) of this bit set.
     *
     * @return the logical size of the bit set
     */
    @Override
    public long logicalSize() {
        return LONGS.toBits(longLength);
    }

    /**
     * Returns the size in bytes of this bit set.
     *
     * @return the size in bytes of the bit set
     */
    @Override
    public long sizeInBytes() {
        return LONGS.toBytes(longLength);
    }

    /**
     * Returns the number of bits set to {@code true} in the bit set.
     *
     * @param access the access interface for reading bits
     * @param handle the handle to the underlying data structure
     * @param offset the offset within the data structure
     * @param <T>    the type of the handle
     * @return the number of bits set to {@code true}
     */
    @Override
    public <T> long cardinality(Access<T> access, T handle, long offset) {
        long count = 0;
        for (long i = 0; i < longLength; i++) {
            count += Long.bitCount(readLong(access, handle, offset, i));
        }
        return count;
    }

    /**
     * Sets the next {@code numberOfBits} consecutive bits that are set to {@code false},
     * starting from the specified {@code fromIndex}. If no such range of clear bits exists,
     * or if {@code numberOfBits} is greater than 64, this method delegates to {@link #setNextManyContinuousClearBits}.
     *
     * @param access       the access interface for reading and writing bits
     * @param handle       the handle to the underlying data structure
     * @param offset       the offset within the data structure
     * @param fromIndex    the index to start checking from (inclusive)
     * @param numberOfBits the number of consecutive clear bits to set
     * @param <T>          the type of the handle
     * @return the index of the first bit in the found range of clear bits, or {@code -1} if there is no such range
     * @throws IllegalArgumentException if {@code numberOfBits} is negative or zero
     */
    @Override
    public <T> long setNextNContinuousClearBits(Access<T> access, T handle, long offset,
                                                long fromIndex, int numberOfBits) {
        if (numberOfBits > 64)
            return setNextManyContinuousClearBits(access, handle, offset, fromIndex, numberOfBits);
        checkNumberOfBits(numberOfBits);
        if (numberOfBits == 1)
            return setNextClearBit(access, handle, offset, fromIndex);
        checkFromIndex(fromIndex);

        long nTrailingOnes = ALL_ONES >>> (64 - numberOfBits);

        long bitIndex = fromIndex;
        long longIndex2 = longWithThisBit(bitIndex);
        if (longIndex2 >= longLength)
            return NOT_FOUND;
        int bitsFromFirstWord = 64 - (((int) bitIndex) & 63);
        long byteIndex2 = firstByte(offset, longIndex2);
        long w1, w2 = access.readLong(handle, byteIndex2);
        longLoop:
        while (true) {
            w1 = w2;
            byteIndex2 += 8;
            if (++longIndex2 < longLength) {
                w2 = access.readLong(handle, byteIndex2);
            } else if (longIndex2 == longLength) {
                w2 = ALL_ONES;
            } else {
                return NOT_FOUND;
            }
            long l;
            // (1)
            if (bitsFromFirstWord != 64) {
                l = (w1 >>> bitIndex) | (w2 << bitsFromFirstWord);
            } else {
                // special case, because if bitsFromFirstWord is 64
                // w2 shift is overflowed
                l = w1;
            }
            // (2)
            if ((l & 1) != 0) {
                long x = ~l;
                if (x != 0) {
                    int trailingOnes = numberOfTrailingZeros(x);
                    bitIndex += trailingOnes;
                    // (3)
                    if ((bitsFromFirstWord -= trailingOnes) <= 0) {
                        bitsFromFirstWord += 64;
                        continue; // long loop
                    }
                    l = (w1 >>> bitIndex) | (w2 << bitsFromFirstWord);
                } else {
                    // all bits are ones, skip a whole word,
                    // bitsFromFirstWord not changed
                    bitIndex += 64;
                    continue; // long loop
                }
            }
            while (true) {
                if ((l & nTrailingOnes) == 0) {
                    long mask1 = nTrailingOnes << bitIndex;
                    access.writeLong(handle, byteIndex2 - 8, w1 ^ mask1);
                    int bitsFromSecondWordToSwitch =
                            numberOfBits - bitsFromFirstWord;
                    if (bitsFromSecondWordToSwitch > 0) {
                        long mask2 = (singleBit(bitsFromSecondWordToSwitch)) - 1;
                        access.writeLong(handle, byteIndex2, w2 ^ mask2);
                    }
                    return bitIndex;
                }
                // n > trailing zeros > 0
                // > 0 ensured by block (2)
                int trailingZeros = numberOfTrailingZeros(l);
                bitIndex += trailingZeros;
                // (4)
                if ((bitsFromFirstWord -= trailingZeros) <= 0) {
                    bitsFromFirstWord += 64;
                    continue longLoop;
                }
                // (5)
                // subtractions (3) and (4) together ensure that
                // bitsFromFirstWord != 64, => no need in condition like (1)
                l = (w1 >>> bitIndex) | (w2 << bitsFromFirstWord);

                long x = ~l;
                if (x != 0) {
                    int trailingOnes = numberOfTrailingZeros(x);
                    bitIndex += trailingOnes;
                    if ((bitsFromFirstWord -= trailingOnes) <= 0) {
                        bitsFromFirstWord += 64;
                        continue longLoop;
                    }
                    // same as (5)
                    l = (w1 >>> bitIndex) | (w2 << bitsFromFirstWord);
                } else {
                    // all bits are ones, skip a whole word,
                    // bitsFromFirstWord not changed
                    bitIndex += 64;
                    continue longLoop;
                }
            }
        }
    }

    /**
     * Sets the next continuous block of clear bits to {@code true}.
     * If the block size is greater than 64 bits, it uses this method to handle the operation.
     *
     * @param access       the access interface for reading and writing bits
     * @param handle       the handle to the underlying data structure
     * @param offset       the offset within the data structure
     * @param fromIndex    the index to start checking from (inclusive)
     * @param numberOfBits the number of consecutive clear bits to set
     * @param <T>          the type of the handle
     * @return the index of the first bit in the found range of clear bits, or {@code -1} if there is no such range
     */
    private <T> long setNextManyContinuousClearBits(Access<T> access, T handle, long offset,
                                                    long fromIndex, int numberOfBits) {
        long size = logicalSize();
        long testFromIndex = fromIndex;
        while (true) {
            long limit = fromIndex + numberOfBits;
            if (limit > size)
                return NOT_FOUND;
            long needToBeZerosUntil = limit - 1;
            long lastSetBit =
                    previousSetBit(access, handle, offset, needToBeZerosUntil, testFromIndex);
            if (lastSetBit == NOT_FOUND) {
                setRange(access, handle, offset, fromIndex, limit);
                return fromIndex;
            }
            fromIndex = lastSetBit + 1;
            testFromIndex = limit;
        }
    }

    /**
     * Clears the next {@code numberOfBits} consecutive bits that are set to {@code true},
     * starting from the specified {@code fromIndex}.
     *
     * @param access       the access interface for reading and writing bits
     * @param handle       the handle to the underlying data structure
     * @param offset       the offset within the data structure
     * @param fromIndex    the index to start checking from (inclusive)
     * @param numberOfBits the number of consecutive set bits to clear
     * @param <T>          the type of the handle
     * @return the index of the first bit in the found range of set bits, or {@code -1} if there is no such range
     * @throws IllegalArgumentException if {@code numberOfBits} is out of range {@code 0 < numberOfBits <= 64}
     */
    @Override
    public <T> long clearNextNContinuousSetBits(Access<T> access, T handle, long offset,
                                                long fromIndex, int numberOfBits) {
        checkNumberOfBits(numberOfBits);
        if (numberOfBits == 1)
            return clearNextSetBit(access, handle, offset, fromIndex);
        checkFromIndex(fromIndex);

        long nTrailingOnes = ALL_ONES >>> (64 - numberOfBits);

        long bitIndex = fromIndex;
        long longIndex2 = longWithThisBit(bitIndex);
        if (longIndex2 >= longLength)
            return NOT_FOUND;
        int bitsFromFirstWord = 64 - (((int) bitIndex) & 63);
        long byteIndex2 = firstByte(offset, longIndex2);
        long w1, w2 = access.readLong(handle, byteIndex2);
        longLoop:
        while (true) {
            w1 = w2;
            byteIndex2 += 8;
            if (++longIndex2 < longLength) {
                w2 = access.readLong(handle, byteIndex2);
            } else if (longIndex2 == longLength) {
                w2 = 0L;
            } else {
                return NOT_FOUND;
            }
            long l;
            // (1)
            if (bitsFromFirstWord != 64) {
                l = (w1 >>> bitIndex) | (w2 << bitsFromFirstWord);
            } else {
                // special case, because if bitsFromFirstWord is 64
                // w2 shift is overflowed
                l = w1;
            }
            // (2)
            if ((l & 1) == 0) {
                if (l != 0) {
                    int trailingZeros = numberOfTrailingZeros(l);
                    bitIndex += trailingZeros;
                    // (3)
                    if ((bitsFromFirstWord -= trailingZeros) <= 0) {
                        bitsFromFirstWord += 64;
                        continue; // long loop
                    }
                    l = (w1 >>> bitIndex) | (w2 << bitsFromFirstWord);
                } else {
                    // all bits are zeros, skip a whole word,
                    // bitsFromFirstWord not changed
                    bitIndex += 64;
                    continue; // long loop
                }
            }
            while (true) {
                if (((~l) & nTrailingOnes) == 0) {
                    long mask1 = nTrailingOnes << bitIndex;
                    access.writeLong(handle, byteIndex2 - 8, w1 ^ mask1);
                    int bitsFromSecondWordToSwitch =
                            numberOfBits - bitsFromFirstWord;
                    if (bitsFromSecondWordToSwitch > 0) {
                        long mask2 = (singleBit(bitsFromSecondWordToSwitch)) - 1;
                        access.writeLong(handle, byteIndex2, w2 ^ mask2);
                    }
                    return bitIndex;
                }
                // n > trailing ones > 0
                // > 0 ensured by block (2)
                int trailingOnes = numberOfTrailingZeros(~l);
                bitIndex += trailingOnes;
                // (4)
                if ((bitsFromFirstWord -= trailingOnes) <= 0) {
                    bitsFromFirstWord += 64;
                    continue longLoop;
                }
                // (5)
                // subtractions (3) and (4) together ensure that
                // bitsFromFirstWord != 64, => no need in condition like (1)
                l = (w1 >>> bitIndex) | (w2 << bitsFromFirstWord);

                if (l != 0) {
                    int trailingZeros = numberOfTrailingZeros(l);
                    bitIndex += trailingZeros;
                    if ((bitsFromFirstWord -= trailingZeros) <= 0) {
                        bitsFromFirstWord += 64;
                        continue longLoop;
                    }
                    // same as (5)
                    l = (w1 >>> bitIndex) | (w2 << bitsFromFirstWord);
                } else {
                    // all bits are zeros, skip a whole word,
                    // bitsFromFirstWord not changed
                    bitIndex += 64;
                    continue longLoop;
                }
            }
        }
    }

    /**
     * Sets the previous {@code numberOfBits} consecutive bits that are set to {@code false},
     * starting from the specified {@code fromIndex}.
     *
     * @param access       the access interface for reading and writing bits
     * @param handle       the handle to the underlying data structure
     * @param offset       the offset within the data structure
     * @param fromIndex    the index to start checking from (inclusive)
     * @param numberOfBits the number of consecutive clear bits to set
     * @param <T>          the type of the handle
     * @return the index of the first bit in the found range of clear bits, or {@code -1} if there is no such range
     * @throws IllegalArgumentException if {@code numberOfBits} is out of range {@code 0 < numberOfBits <= 64}
     */
    @Override
    public <T> long setPreviousNContinuousClearBits(Access<T> access, T handle, long offset,
                                                    long fromIndex, int numberOfBits) {
        checkNumberOfBits(numberOfBits);
        if (numberOfBits == 1)
            return setPreviousClearBit(access, handle, offset, fromIndex);
        if (checkNotFoundIndex(fromIndex))
            return NOT_FOUND;

        int n64Complement = 64 - numberOfBits;
        long nLeadingOnes = higherBitsIncludingThis(n64Complement);

        long higherBitBound = fromIndex + 1;
        long lowLongIndex = longWithThisBit(fromIndex);
        if (lowLongIndex >= longLength) {
            lowLongIndex = longLength - 1;
            higherBitBound = longLength << 6;
        }
        int bitsFromLowWord = (64 - (((int) higherBitBound) & 63)) & 63;
        long lowByteIndex = firstByte(offset, lowLongIndex);
        // low word, high word
        long hw, lw = access.readLong(handle, lowByteIndex);
        longLoop:
        while (true) {
            hw = lw;
            lowByteIndex -= 8;
            if (--lowLongIndex >= 0) {
                lw = access.readLong(handle, lowByteIndex);
            } else if (lowLongIndex == -1) {
                lw = ALL_ONES;
            } else {
                return NOT_FOUND;
            }
            long l;
            if (bitsFromLowWord != 0) { // (1)
                l = (lw >>> higherBitBound) | (hw << bitsFromLowWord);
            } else {
                // all bits from high word, special case needed because
                // higherBitBound is multiple of 64 and lw not shifted away
                l = hw;
            }
            // (2)
            if (l < 0) { // condition means the highest bit is one
                long x = ~l;
                if (x != 0) {
                    int leadingOnes = numberOfLeadingZeros(x);
                    higherBitBound -= leadingOnes;
                    bitsFromLowWord += leadingOnes; // (3)
                    int flw;
                    if ((flw = bitsFromLowWord - 64) >= 0) {
                        bitsFromLowWord = flw;
                        continue; // long loop
                    }
                    l = (lw >>> higherBitBound) | (hw << bitsFromLowWord);
                } else {
                    // all bits are ones, skip a whole word,
                    // bitsFromLowWord not changed
                    higherBitBound -= 64;
                    continue; // long loop
                }
            }
            while (true) {
                if ((l & nLeadingOnes) == 0) {
                    long hMask = nLeadingOnes >>> bitsFromLowWord;
                    access.writeLong(handle, lowByteIndex + 8, hw ^ hMask);
                    // bitsFromLow - (64 - n) = n - (64 - bitsFromLow) =
                    // = n - bitsFromHigh
                    int bitsFromLowWordToSwitch =
                            bitsFromLowWord - n64Complement;
                    if (bitsFromLowWordToSwitch > 0) {
                        long lMask = ~(ALL_ONES >>> bitsFromLowWordToSwitch);
                        access.writeLong(handle, lowByteIndex, lw ^ lMask);
                    }
                    return higherBitBound - numberOfBits;
                }
                // n > leading zeros > 0
                // > 0 ensured by block (2)
                int leadingZeros = numberOfLeadingZeros(l);
                higherBitBound -= leadingZeros;
                bitsFromLowWord += leadingZeros; // (4)
                int flw;
                if ((flw = bitsFromLowWord - 64) >= 0) {
                    bitsFromLowWord = flw;
                    continue longLoop;
                }
                // (5)
                // additions (3) and (4) together ensure that
                // bitsFromFirstWord > 0, => no need in condition like (1)
                l = (lw >>> higherBitBound) | (hw << bitsFromLowWord);

                long x = ~l;
                if (x != 0) {
                    int leadingOnes = numberOfLeadingZeros(x);
                    higherBitBound -= leadingOnes;
                    bitsFromLowWord += leadingOnes;
                    if ((flw = bitsFromLowWord - 64) >= 0) {
                        bitsFromLowWord = flw;
                        continue longLoop;
                    }
                    // same as (5)
                    l = (lw >>> higherBitBound) | (hw << bitsFromLowWord);
                } else {
                    // all bits are ones, skip a whole word,
                    // bitsFromLowWord not changed
                    higherBitBound -= 64;
                    continue longLoop;
                }
            }
        }
    }

    /**
     * Clears the previous {@code numberOfBits} consecutive bits that are set to {@code true},
     * starting from the specified {@code fromIndex}. If no such range of set bits exists,
     * or if {@code numberOfBits} is greater than 64, this method clears the bits in a loop.
     *
     * @param access       the access interface for reading and writing bits
     * @param handle       the handle to the underlying data structure
     * @param offset       the offset within the data structure
     * @param fromIndex    the index to start checking from (inclusive)
     * @param numberOfBits the number of consecutive set bits to clear
     * @param <T>          the type of the handle
     * @return the index of the first bit in the found range of set bits, or {@code -1} if there is no such range
     * @throws IllegalArgumentException if {@code numberOfBits} is out of range {@code 0 < numberOfBits <= 64}
     */
    @Override
    public <T> long clearPreviousNContinuousSetBits(Access<T> access, T handle, long offset,
                                                    long fromIndex, int numberOfBits) {
        checkNumberOfBits(numberOfBits);
        if (numberOfBits == 1)
            return clearPreviousSetBit(access, handle, offset, fromIndex);
        if (checkNotFoundIndex(fromIndex))
            return NOT_FOUND;

        int n64Complement = 64 - numberOfBits;
        long nLeadingOnes = higherBitsIncludingThis(n64Complement);

        long higherBitBound = fromIndex + 1;
        long lowLongIndex = longWithThisBit(fromIndex);
        if (lowLongIndex >= longLength) {
            lowLongIndex = longLength - 1;
            higherBitBound = longLength << 6;
        }
        int bitsFromLowWord = (64 - (((int) higherBitBound) & 63)) & 63;
        long lowByteIndex = firstByte(offset, lowLongIndex);
        // low word, high word
        long hw, lw = access.readLong(handle, lowByteIndex);
        longLoop:
        while (true) {
            hw = lw;
            lowByteIndex -= 8;
            if (--lowLongIndex >= 0) {
                lw = access.readLong(handle, lowByteIndex);
            } else if (lowLongIndex == -1) {
                lw = 0L;
            } else {
                return NOT_FOUND;
            }
            long l;
            if (bitsFromLowWord != 0) { // (1)
                l = (lw >>> higherBitBound) | (hw << bitsFromLowWord);
            } else {
                // all bits from high word, special case needed because
                // higherBitBound is multiple of 64 and lw not shifted away
                l = hw;
            }
            // (2)
            if (l > 0) { // condition means the highest bit is zero, but not all
                int leadingZeros = numberOfLeadingZeros(l);
                higherBitBound -= leadingZeros;
                bitsFromLowWord += leadingZeros; // (3)
                int flw;
                if ((flw = bitsFromLowWord - 64) >= 0) {
                    bitsFromLowWord = flw;
                    continue; // long loop
                }
                l = (lw >>> higherBitBound) | (hw << bitsFromLowWord);
            } else if (l == 0) {
                // all bits are zeros, skip a whole word,
                // bitsFromLowWord not changed
                higherBitBound -= 64;
                continue; // long loop
            }
            while (true) {
                if (((~l) & nLeadingOnes) == 0) {
                    long hMask = nLeadingOnes >>> bitsFromLowWord;
                    access.writeLong(handle, lowByteIndex + 8, hw ^ hMask);
                    // bitsFromLow - (64 - n) = n - (64 - bitsFromLow) =
                    // = n - bitsFromHigh
                    int bitsFromLowWordToSwitch =
                            bitsFromLowWord - n64Complement;
                    if (bitsFromLowWordToSwitch > 0) {
                        long lMask = ~(ALL_ONES >>> bitsFromLowWordToSwitch);
                        access.writeLong(handle, lowByteIndex, lw ^ lMask);
                    }
                    return higherBitBound - numberOfBits;
                }
                // n > leading ones > 0
                // > 0 ensured by block (2)
                int leadingOnes = numberOfLeadingZeros(~l);
                higherBitBound -= leadingOnes;
                bitsFromLowWord += leadingOnes; // (4)
                int flw;
                if ((flw = bitsFromLowWord - 64) >= 0) {
                    bitsFromLowWord = flw;
                    continue longLoop;
                }
                // (5)
                // additions (3) and (4) together ensure that
                // bitsFromFirstWord > 0, => no need in condition like (1)
                l = (lw >>> higherBitBound) | (hw << bitsFromLowWord);

                if (l != 0) {
                    int leadingZeros = numberOfLeadingZeros(l);
                    higherBitBound -= leadingZeros;
                    bitsFromLowWord += leadingZeros;
                    if ((flw = bitsFromLowWord - 64) >= 0) {
                        bitsFromLowWord = flw;
                        continue longLoop;
                    }
                    // same as (5)
                    l = (lw >>> higherBitBound) | (hw << bitsFromLowWord);
                } else {
                    // all bits are zeros, skip a whole word,
                    // bitsFromLowWord not changed
                    higherBitBound -= 64;
                    continue longLoop;
                }
            }
        }
    }

    /**
     * Implementation of {@link BitSetFrame.Bits} for iterating over set bits.
     */
    private class SetBits implements Bits {
        private final long byteLength = longLength << 3;
        private long byteIndex;
        private long bitIndex;
        private long currentWord;

        /**
         * Resets the iterator to the start of the bit set.
         *
         * @param access the access interface for reading bits
         * @param handle the handle to the underlying data structure
         * @param offset the offset within the data structure
         * @param <T>    the type of the handle
         * @return the {@link Bits} instance for method chaining
         */
        @Override
        public <T> Bits reset(Access<T> access, T handle, long offset) {
            byteIndex = 0;
            bitIndex = -1;
            currentWord = access.readLong(handle, offset);
            return this;
        }

        /**
         * Finds the next set bit in the bit set.
         *
         * @param access the access interface for reading bits
         * @param handle the handle to the underlying data structure
         * @param offset the offset within the data structure
         * @param <T>    the type of the handle
         * @return the index of the next set bit, or {@code -1} if there are no more set bits
         */
        @Override
        public <T> long next(Access<T> access, T handle, long offset) {
            long l;
            if ((l = currentWord) != 0) {
                int trailingZeros = numberOfTrailingZeros(l);
                currentWord = (l >>> trailingZeros) >>> 1;
                return bitIndex += trailingZeros + 1;
            }
            for (long i = byteIndex, lim = byteLength; (i += 8) < lim; ) {
                if ((l = access.readLong(handle, i)) != 0) {
                    byteIndex = i;
                    int trailingZeros = numberOfTrailingZeros(l);
                    currentWord = (l >>> trailingZeros) >>> 1;
                    return bitIndex = (i << 3) + trailingZeros;
                }
            }
            currentWord = 0;
            byteIndex = byteLength;
            return -1;
        }
    }
}
