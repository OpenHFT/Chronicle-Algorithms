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

import net.openhft.chronicle.bytes.Access;

import static java.lang.Long.numberOfLeadingZeros;
import static java.lang.Long.numberOfTrailingZeros;
import static net.openhft.chronicle.core.MemoryUnit.BITS;
import static net.openhft.chronicle.core.MemoryUnit.LONGS;

/**
 * DirectBitSet with input validations, This class is not thread safe
 */
public final class SingleThreadedFlatBitSetFrame implements BitSetFrame {

    public static final long ALL_ONES = ~0L;

    // masks

    static long singleBit(long bitIndex) {
        return 1L << bitIndex;
    }

    static long higherBitsIncludingThis(long bitIndex) {
        return ALL_ONES << bitIndex;
    }

    static long lowerBitsIncludingThis(long bitIndex) {
        return ALL_ONES >>> ~bitIndex;
    }

    static long higherBitsExcludingThis(long bitIndex) {
        return ~(ALL_ONES >>> ~bitIndex);
    }

    static long lowerBitsExcludingThis(long bitIndex) {
        return ~(ALL_ONES << bitIndex);
    }

    // conversions

    static long longWithThisBit(long bitIndex) {
        return bitIndex >> 6;
    }

    long byteWithThisBit(long offset, long bitIndex) {
        return offset + ((bitIndex >> 6) << 3);
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

    private final long longLength;

    public SingleThreadedFlatBitSetFrame(long logicalSize) {
        LONGS.checkAligned(logicalSize, BITS);
        longLength = BITS.toLongs(logicalSize);
    }

    // checks

    static void checkNumberOfBits(int numberOfBits) {
        if (numberOfBits <= 0 || numberOfBits > 64)
            throw new IllegalArgumentException("Illegal number of bits: " + numberOfBits);
    }

    static boolean checkNotFoundIndex(long fromIndex) {
        if (fromIndex < 0) {
            if (fromIndex == NOT_FOUND)
                return true;
            throw new IndexOutOfBoundsException();
        }
        return false;
    }

    private void checkIndex(long bitIndex, long longIndex) {
        if (bitIndex < 0 || longIndex >= longLength)
            throw new IndexOutOfBoundsException();
    }

    private void checkFromTo(long fromIndex, long exclusiveToIndex, long toLongIndex) {
        if (fromIndex < 0 || fromIndex > exclusiveToIndex ||
                toLongIndex >= longLength)
            throw new IndexOutOfBoundsException();
    }

    private <T> long readLong(Access<T> access, T handle, long offset, long longIndex) {
        return access.readLong(handle, firstByte(offset, longIndex));
    }

    private <T> void writeLong(Access<T> access, T handle, long offset,
                               long longIndex, long toWrite) {
        access.writeLong(handle, firstByte(offset, longIndex), toWrite);
    }

    @Override
    public <T> void flip(Access<T> access, T handle, long offset, long bitIndex) {
        long byteIndex = byteWithThisBit(offset, bitIndex);
        long mask = singleBit(bitIndex);
        long l = access.readLong(handle, byteIndex);
        long l2 = l ^ mask;
        access.writeLong(handle, byteIndex, l2);
    }

    @Override
    public <T> void flipRange(Access<T> access, T handle, long offset,
                              long fromIndex, long exclusiveToIndex) {
        long fromLongIndex = longWithThisBit(fromIndex);
        long toIndex = exclusiveToIndex - 1;
        long toLongIndex = longWithThisBit(toIndex);
        checkFromTo(fromIndex, exclusiveToIndex, toLongIndex);

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
        long byteIndex = byteWithThisBit(offset, bitIndex);
        long mask = singleBit(bitIndex);
        long l = access.readLong(handle, byteIndex);
        if ((l & mask) != 0)
            return;
        long l2 = l | mask;
        access.writeLong(handle, byteIndex, l2);
    }

    @Override
    public <T> boolean setIfClear(Access<T> access, T handle, long offset, long bitIndex) {
        long byteIndex = byteWithThisBit(offset, bitIndex);
        long mask = singleBit(bitIndex);
        long l = access.readLong(handle, byteIndex);
        long l2 = l | mask;
        if (l == l2)
            return false;
        access.writeLong(handle, byteIndex, l2);
        return true;
    }

    @Override
    public <T> void setRange(Access<T> access, T handle, long offset,
                        long fromIndex, long exclusiveToIndex) {
        long fromLongIndex = longWithThisBit(fromIndex);
        long toIndex = exclusiveToIndex - 1;
        long toLongIndex = longWithThisBit(toIndex);
        checkFromTo(fromIndex, exclusiveToIndex, toLongIndex);

        if (fromLongIndex != toLongIndex) {
            long firstFullLongIndex = fromLongIndex;
            if ((fromIndex & 63) != 0) {
                long fromByteIndex = firstByte(offset, fromLongIndex);
                long mask = higherBitsIncludingThis(fromIndex);
                long l = access.readLong(handle, fromByteIndex);
                long l2 = l | mask;
                access.writeLong(handle, fromByteIndex, l2);
                firstFullLongIndex++;
            }

            if ((exclusiveToIndex & 63) == 0) {
                for (long i = firstFullLongIndex; i <= toLongIndex; i++) {
                    writeLong(access, handle, offset, i, ALL_ONES);
                }
            } else {
                for (long i = firstFullLongIndex; i < toLongIndex; i++) {
                    writeLong(access, handle, offset, i, ALL_ONES);
                }

                long toByteIndex = firstByte(offset, toLongIndex);
                long mask = lowerBitsIncludingThis(toIndex);
                long l = access.readLong(handle, toByteIndex);
                long l2 = l | mask;
                access.writeLong(handle, toByteIndex, l2);
            }
        } else {
            long byteIndex = firstByte(offset, fromLongIndex);
            long mask = higherBitsIncludingThis(fromIndex) & lowerBitsIncludingThis(toIndex);
            long l = access.readLong(handle, byteIndex);
            long l2 = l | mask;
            access.writeLong(handle, byteIndex, l2);
        }
    }

    @Override
    public <T> void setAll(Access<T> access, T handle, long offset) {
        for (long i = 0; i < longLength; i++) {
            writeLong(access, handle, offset, i, ALL_ONES);
        }
    }

    @Override
    public <T> void clear(Access<T> access, T handle, long offset, long bitIndex) {
        long byteIndex = byteWithThisBit(offset, bitIndex);
        long mask = singleBit(bitIndex);
        long l = access.readLong(handle, byteIndex);
        if ((l & mask) == 0)
            return;
        long l2 = l & ~mask;
        access.writeLong(handle, byteIndex, l2);
    }

    @Override
    public <T> boolean clearIfSet(Access<T> access, T handle, long offset, long bitIndex) {
        long byteIndex = byteWithThisBit(offset, bitIndex);
        long mask = singleBit(bitIndex);
        long l = access.readLong(handle, byteIndex);
        if ((l & mask) == 0)
            return false;
        long l2 = l & ~mask;
        access.writeLong(handle, byteIndex, l2);
        return true;
    }

    @Override
    public <T> void clearRange(Access<T> access, T handle, long offset,
                              long fromIndex, long exclusiveToIndex) {
        long fromLongIndex = longWithThisBit(fromIndex);
        long toIndex = exclusiveToIndex - 1;
        long toLongIndex = longWithThisBit(toIndex);
        checkFromTo(fromIndex, exclusiveToIndex, toLongIndex);

        if (fromLongIndex != toLongIndex) {
            long firstFullLongIndex = fromLongIndex;
            if ((fromIndex & 63) != 0) {
                long fromByteIndex = firstByte(offset, fromLongIndex);
                long mask = higherBitsIncludingThis(fromIndex);
                long l = access.readLong(handle, fromByteIndex);
                long l2 = l & ~mask;
                access.writeLong(handle, fromByteIndex, l2);
                firstFullLongIndex++;
            }

            if ((exclusiveToIndex & 63) == 0) {
                for (long i = firstFullLongIndex; i <= toLongIndex; i++) {
                    writeLong(access, handle, offset, i, 0L);
                }
            } else {
                for (long i = firstFullLongIndex; i < toLongIndex; i++) {
                    writeLong(access, handle, offset, i, 0L);
                }

                long toByteIndex = firstByte(offset, toLongIndex);
                long mask = lowerBitsIncludingThis(toIndex);
                long l = access.readLong(handle, toByteIndex);
                long l2 = l & ~mask;
                access.writeLong(handle, toByteIndex, l2);
            }
        } else {
            long byteIndex = firstByte(offset, fromLongIndex);
            long mask = higherBitsIncludingThis(fromIndex) & lowerBitsIncludingThis(toIndex);
            long l = access.readLong(handle, byteIndex);
            long l2 = l & ~mask;
            access.writeLong(handle, byteIndex, l2);
        }
    }

    @Override
    public <T> boolean isRangeSet(Access<T> access, T handle, long offset,
                              long fromIndex, long exclusiveToIndex) {
        long fromLongIndex = longWithThisBit(fromIndex);
        long toIndex = exclusiveToIndex - 1;
        long toLongIndex = longWithThisBit(toIndex);
        checkFromTo(fromIndex, exclusiveToIndex, toLongIndex);

        if (fromLongIndex != toLongIndex) {
            long firstFullLongIndex = fromLongIndex;
            if ((fromIndex & 63) != 0) {
                long mask = higherBitsIncludingThis(fromIndex);
                if ((~(readLong(access, handle, offset, fromLongIndex)) & mask) != 0L)
                    return false;
                firstFullLongIndex++;
            }

            if ((exclusiveToIndex & 63) == 0) {
                for (long i = firstFullLongIndex; i <= toLongIndex; i++) {
                    if (~readLong(access, handle, offset, i) != 0L)
                        return false;
                }
                return true;
            } else {
                for (long i = firstFullLongIndex; i < toLongIndex; i++) {
                    if (~readLong(access, handle, offset, i) != 0L)
                        return false;
                }

                long mask = lowerBitsIncludingThis(toIndex);
                return ((~readLong(access, handle, offset, toLongIndex)) & mask) == 0L;
            }
        } else {
            long mask = higherBitsIncludingThis(fromIndex) & lowerBitsIncludingThis(toIndex);
            return ((~readLong(access, handle, offset, fromLongIndex)) & mask) == 0L;
        }
    }

    @Override
    public <T> boolean isRangeClear(Access<T> access, T handle, long offset,
                                long fromIndex, long exclusiveToIndex) {
        long fromLongIndex = longWithThisBit(fromIndex);
        long toIndex = exclusiveToIndex - 1;
        long toLongIndex = longWithThisBit(toIndex);
        checkFromTo(fromIndex, exclusiveToIndex, toLongIndex);

        if (fromLongIndex != toLongIndex) {
            long firstFullLongIndex = fromLongIndex;
            if ((fromIndex & 63) != 0) {
                long mask = higherBitsIncludingThis(fromIndex);
                if ((readLong(access, handle, offset, fromLongIndex) & mask) != 0L)
                    return false;
                firstFullLongIndex++;
            }

            if ((exclusiveToIndex & 63) == 0) {
                for (long i = firstFullLongIndex; i <= toLongIndex; i++) {
                    if (readLong(access, handle, offset, i) != 0L)
                        return false;
                }
                return true;
            } else {
                for (long i = firstFullLongIndex; i < toLongIndex; i++) {
                    if (readLong(access, handle, offset, i) != 0L)
                        return false;
                }

                long mask = lowerBitsIncludingThis(toIndex);
                return (readLong(access, handle, offset, toLongIndex) & mask) == 0L;
            }
        } else {
            long mask = higherBitsIncludingThis(fromIndex) & lowerBitsIncludingThis(toIndex);
            return (readLong(access, handle, offset, fromLongIndex) & mask) == 0L;
        }
    }

    @Override
    public <T> void clearAll(Access<T> access, T handle, long offset) {
        access.writeBytes(handle, offset, LONGS.toBytes(longLength), (byte) 0);
    }

    @Override
    public <T> boolean get(Access<T> access, T handle, long offset, long bitIndex) {
        long byteIndex = byteWithThisBit(offset, bitIndex);
        long l = access.readLong(handle, byteIndex);
        return (l & (singleBit(bitIndex))) != 0;
    }

    @Override
    public <T> long nextSetBit(Access<T> access, T handle, long offset, long fromIndex) {
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException();
        long fromLongIndex = longWithThisBit(fromIndex);
        if (fromLongIndex >= longLength)
            return NOT_FOUND;
        long l = readLong(access, handle, offset, fromLongIndex) >>> fromIndex;
        if (l != 0) {
            return fromIndex + numberOfTrailingZeros(l);
        }
        for (long i = fromLongIndex + 1; i < longLength; i++) {
            l = readLong(access, handle, offset, i);
            if (l != 0)
                return firstBit(i) + numberOfTrailingZeros(l);
        }
        return NOT_FOUND;
    }

    private class SetBits implements Bits {
        private final long byteLength = longLength << 3;
        private long byteIndex;
        private long bitIndex;
        private long currentWord;

        @Override
        public <T> Bits reset(Access<T> access, T handle, long offset) {
            byteIndex = 0;
            bitIndex = -1;
            currentWord = access.readLong(handle, offset);
            return this;
        }

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

    @Override
    public Bits setBits() {
        return new SetBits();
    }

    @Override
    public BitSetAlgorithm algorithm() {
        return FlatBitSetAlgorithm.INSTANCE;
    }

    @Override
    public <T> long clearNextSetBit(Access<T> access, T handle, long offset, long fromIndex) {
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException();
        long fromLongIndex = longWithThisBit(fromIndex);
        if (fromLongIndex >= longLength)
            return NOT_FOUND;
        long fromByteIndex = firstByte(offset, fromLongIndex);
        long w = access.readLong(handle, fromByteIndex);
        long l = w >>> fromIndex;
        if (l != 0) {
            long indexOfSetBit = fromIndex + numberOfTrailingZeros(l);
            long mask = singleBit(indexOfSetBit);
            access.writeLong(handle, fromByteIndex, w ^ mask);
            return indexOfSetBit;
        }
        for (long i = fromLongIndex + 1; i < longLength; i++) {
            long byteIndex = firstByte(offset, i);
            l = access.readLong(handle, byteIndex);
            if (l != 0) {
                long indexOfSetBit = firstBit(i) + numberOfTrailingZeros(l);
                long mask = singleBit(indexOfSetBit);
                access.writeLong(handle, byteIndex, l ^ mask);
                return indexOfSetBit;
            }
        }
        return NOT_FOUND;
    }
    @Override
    public <T> long nextClearBit(Access<T> access, T handle, long offset, long fromIndex) {
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException();
        long fromLongIndex = longWithThisBit(fromIndex);
        if (fromLongIndex >= longLength)
            return NOT_FOUND;
        long l = (~readLong(access, handle, offset, fromLongIndex)) >>> fromIndex;
        if (l != 0) {
            return fromIndex + numberOfTrailingZeros(l);
        }
        for (long i = fromLongIndex + 1; i < longLength; i++) {
            l = ~readLong(access, handle, offset, i);
            if (l != 0)
                return firstBit(i) + numberOfTrailingZeros(l);
        }
        return NOT_FOUND;
    }

    @Override
    public <T> long setNextClearBit(Access<T> access, T handle, long offset, long fromIndex) {
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException();
        long fromLongIndex = longWithThisBit(fromIndex);
        if (fromLongIndex >= longLength)
            return NOT_FOUND;
        long fromByteIndex = firstByte(offset, fromLongIndex);
        long w = access.readLong(handle, fromByteIndex);
        long l = (~w) >>> fromIndex;
        if (l != 0) {
            long indexOfClearBit = fromIndex + numberOfTrailingZeros(l);
            long mask = singleBit(indexOfClearBit);
            access.writeLong(handle, fromByteIndex, w ^ mask);
            return indexOfClearBit;
        }
        for (long i = fromLongIndex + 1; i < longLength; i++) {
            long byteIndex = firstByte(offset, i);
            w = access.readLong(handle, byteIndex);
            l = ~w;
            if (l != 0) {
                long indexOfClearBit = firstBit(i) + numberOfTrailingZeros(l);
                long mask = singleBit(indexOfClearBit);
                access.writeLong(handle, byteIndex, w ^ mask);
                return indexOfClearBit;
            }
        }
        return NOT_FOUND;
    }

    @Override
    public <T> long previousSetBit(Access<T> access, T handle, long offset, long fromIndex) {
        if (checkNotFoundIndex(fromIndex))
            return NOT_FOUND;
        long fromLongIndex = longWithThisBit(fromIndex);
        if (fromLongIndex >= longLength) {
            // the same policy for this "index out of bounds" situation
            // as in j.u.BitSet
            fromLongIndex = longLength - 1;
            fromIndex = logicalSize() - 1;
        }
        // << ~fromIndex === << (63 - (fromIndex & 63))
        long l = readLong(access, handle, offset, fromLongIndex) << ~fromIndex;
        if (l != 0)
            return fromIndex - numberOfLeadingZeros(l);
        for (long i = fromLongIndex - 1; i >= 0; i--) {
            l = readLong(access, handle, offset, i);
            if (l != 0)
                return lastBit(i) - numberOfLeadingZeros(l);
        }
        return NOT_FOUND;
    }


    private <T> long previousSetBit(Access<T> access, T handle, long offset,
                                    long fromIndex, long inclusiveToIndex) {
        long fromLongIndex = longWithThisBit(fromIndex);
        long toLongIndex = longWithThisBit(inclusiveToIndex);
        checkFromTo(inclusiveToIndex, fromIndex + 1, toLongIndex);
        if (fromLongIndex >= longLength) {
            // the same policy for this "index out of bounds" situation
            // as in j.u.BitSet
            fromLongIndex = longLength - 1;
            fromIndex = logicalSize() - 1;
        }
        if (fromLongIndex != toLongIndex) {
            // << ~fromIndex === << (63 - (fromIndex & 63))
            long l = readLong(access, handle, offset, fromLongIndex) << ~fromIndex;
            if (l != 0)
                return fromIndex - numberOfLeadingZeros(l);
            for (long i = fromLongIndex - 1; i > toLongIndex; i--) {
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
        return NOT_FOUND;
    }

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

    @Override
    public long logicalSize() {
        return LONGS.toBits(longLength);
    }

    @Override
    public long sizeInBytes() {
        return LONGS.toBytes(longLength);
    }

    @Override
    public <T> long cardinality(Access<T> access, T handle, long offset) {
        long count = 0;
        for (long i = 0; i < longLength; i++) {
            count += Long.bitCount(readLong(access, handle, offset, i));
        }
        return count;
    }

    /**
     * @throws IllegalArgumentException if {@code numberOfBits} is negative
     */
    @Override
    public <T> long setNextNContinuousClearBits(Access<T> access, T handle, long offset,
                                                long fromIndex, int numberOfBits) {
        if (numberOfBits > 64)
            return setNextManyContinuousClearBits(access, handle, offset, fromIndex, numberOfBits);
        checkNumberOfBits(numberOfBits);
        if (numberOfBits == 1)
            return setNextClearBit(access, handle, offset, fromIndex);
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException();

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
     * @throws IllegalArgumentException if {@code numberOfBits} is out of range {@code 0 <
     *                                  numberOfBits && numberOfBits <= 64}
     */
    @Override
    public <T> long clearNextNContinuousSetBits(Access<T> access, T handle, long offset,
                                                long fromIndex, int numberOfBits) {
        checkNumberOfBits(numberOfBits);
        if (numberOfBits == 1)
            return clearNextSetBit(access, handle, offset, fromIndex);
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException();

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
}
