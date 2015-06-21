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

import static java.lang.Long.numberOfLeadingZeros;
import static java.lang.Long.numberOfTrailingZeros;
import static net.openhft.chronicle.algo.bitset.MemoryUnit.BITS;
import static net.openhft.chronicle.algo.bitset.MemoryUnit.LONGS;
import static net.openhft.chronicle.algo.bitset.SingleThreadedFlatBitSetFrame.*;

/**
 * DirectBitSet with input validations and ThreadSafe memory access.
 */
public final class ConcurrentFlatBitSetFrame implements BitSetFrame {
    private final long longLength;

    public ConcurrentFlatBitSetFrame(long logicalSize) {
        longLength = BITS.toLongs(logicalSize);
    }

    private static long rightShiftOneFill(long l, long shift) {
        return (l >> shift) | ~(ALL_ONES >>> shift);
    }

    private static long leftShiftOneFill(long l, long shift) {
        return (l << shift) | ((1L << shift) - 1L);
    }

    private <T> long readLong(Access<T> access, T handle, long offset, long longIndex) {
        return access.readLong(handle, firstByte(offset, longIndex));
    }

    private <T> long readVolatileLong(Access<T> access, T handle, long offset, long longIndex) {
        return access.readVolatileLong(handle, firstByte(offset, longIndex));
    }

    @Override
    public <T> void flip(Access<T> access, T handle, long offset, long bitIndex) {
        long longIndex = longWithThisBit(bitIndex);
        if (bitIndex < 0 || longIndex >= longLength)
            throw new IndexOutOfBoundsException();
        long byteIndex = firstByte(offset, longIndex);
        // only 6 lowest-order bits used, JLS 15.19
        long mask = singleBit(bitIndex);
        while (true) {
            long l = access.readVolatileLong(handle, byteIndex);
            long l2 = l ^ mask;
            if (access.compareAndSwapLong(handle, byteIndex, l, l2))
                return;
        }
    }

    @Override
    public <T> void flipRange(Access<T> access, T handle, long offset,
                         long fromIndex, long exclusiveToIndex) {
        long fromLongIndex = longWithThisBit(fromIndex);
        long toIndex = exclusiveToIndex - 1;
        long toLongIndex = longWithThisBit(toIndex);
        if (fromIndex < 0 || fromIndex > exclusiveToIndex ||
                toLongIndex >= longLength)
            throw new IndexOutOfBoundsException();

        if (fromLongIndex != toLongIndex) {
            long firstFullLongIndex = fromLongIndex;
            if ((fromIndex & 63) != 0) {
                long fromByteIndex = firstByte(offset, fromLongIndex);
                long mask = higherBitsIncludingThis(fromIndex);
                while (true) {
                    long l = access.readVolatileLong(handle, fromByteIndex);
                    long l2 = l ^ mask;
                    if (access.compareAndSwapLong(handle, fromByteIndex, l, l2))
                        break;
                }
                firstFullLongIndex++;
            }

            if ((exclusiveToIndex & 63) == 0) {
                for (long i = firstFullLongIndex; i <= toLongIndex; i++) {
                    while (true) {
                        long l = readVolatileLong(access, handle, offset, i);
                        long l2 = ~l;
                        if (access.compareAndSwapLong(handle, firstByte(offset, i), l, l2))
                            break;
                    }
                }
            } else {
                for (long i = firstFullLongIndex; i < toLongIndex; i++) {
                    while (true) {
                        long l = readVolatileLong(access, handle, offset, i);
                        long l2 = ~l;
                        if (access.compareAndSwapLong(handle, firstByte(offset, i), l, l2))
                            break;
                    }
                }

                long toByteIndex = firstByte(offset, toLongIndex);
                long mask = lowerBitsIncludingThis(toIndex);
                while (true) {
                    long l = access.readVolatileLong(handle, toByteIndex);
                    long l2 = l ^ mask;
                    if (access.compareAndSwapLong(handle, toByteIndex, l, l2))
                        return;
                }
            }
        } else {
            long byteIndex = firstByte(offset, fromLongIndex);
            long mask = higherBitsIncludingThis(fromIndex) & lowerBitsIncludingThis(toIndex);
            while (true) {
                long l = access.readVolatileLong(handle, byteIndex);
                long l2 = l ^ mask;
                if (access.compareAndSwapLong(handle, byteIndex, l, l2))
                    return;
            }
        }
    }

    @Override
    public <T> void set(Access<T> access, T handle, long offset, long bitIndex) {
        long longIndex = longWithThisBit(bitIndex);
        if (bitIndex < 0 || longIndex >= longLength)
            throw new IndexOutOfBoundsException();
        long byteIndex = firstByte(offset, longIndex);
        long mask = singleBit(bitIndex);
        while (true) {
            long l = access.readVolatileLong(handle, byteIndex);
            if ((l & mask) != 0)
                return;
            long l2 = l | mask;
            if (access.compareAndSwapLong(handle, byteIndex, l, l2))
                return;
        }
    }

    @Override
    public <T> boolean setIfClear(Access<T> access, T handle, long offset, long bitIndex) {
        long longIndex = longWithThisBit(bitIndex);
        if (bitIndex < 0 || longIndex >= longLength)
            throw new IndexOutOfBoundsException();
        long byteIndex = firstByte(offset, longIndex);
        long mask = singleBit(bitIndex);
        while (true) {
            long l = access.readVolatileLong(handle, byteIndex);
            long l2 = l | mask;
            if (l == l2)
                return false;
            if (access.compareAndSwapLong(handle, byteIndex, l, l2))
                return true;
        }
    }

    @Override
    public <T> void setRange(Access<T> access, T handle, long offset,
                             long fromIndex, long exclusiveToIndex) {
        long fromLongIndex = longWithThisBit(fromIndex);
        long toIndex = exclusiveToIndex - 1;
        long toLongIndex = longWithThisBit(toIndex);
        if (fromIndex < 0 || fromIndex > exclusiveToIndex ||
                toLongIndex >= longLength)
            throw new IndexOutOfBoundsException();

        if (fromLongIndex != toLongIndex) {
            long firstFullLongIndex = fromLongIndex;
            if ((fromIndex & 63) != 0) {
                long fromByteIndex = firstByte(offset, fromLongIndex);
                long mask = higherBitsIncludingThis(fromIndex);
                while (true) {
                    long l = access.readVolatileLong(handle, fromByteIndex);
                    long l2 = l | mask;
                    if (access.compareAndSwapLong(handle, fromByteIndex, l, l2))
                        break;
                }
                firstFullLongIndex++;
            }

            if ((exclusiveToIndex & 63) == 0) {
                for (long i = firstFullLongIndex; i <= toLongIndex; i++) {
                    access.writeOrderedLong(handle, firstByte(offset, i), ALL_ONES);
                }
            } else {
                for (long i = firstFullLongIndex; i < toLongIndex; i++) {
                    access.writeOrderedLong(handle, firstByte(offset, i), ALL_ONES);
                }

                long toByteIndex = firstByte(offset, toLongIndex);
                long mask = lowerBitsIncludingThis(toIndex);
                while (true) {
                    long l = access.readVolatileLong(handle, toByteIndex);
                    long l2 = l | mask;
                    if (access.compareAndSwapLong(handle, toByteIndex, l, l2))
                        return;
                }
            }
        } else {
            long byteIndex = firstByte(offset, fromLongIndex);
            long mask = higherBitsIncludingThis(fromIndex) & lowerBitsIncludingThis(toIndex);
            while (true) {
                long l = access.readVolatileLong(handle, byteIndex);
                long l2 = l | mask;
                if (access.compareAndSwapLong(handle, byteIndex, l, l2))
                    return;
            }
        }
    }

    @Override
    public <T> void setAll(Access<T> access, T handle, long offset) {
        for (long i = 0; i < longLength; i++) {
            access.writeOrderedLong(handle, firstByte(offset, i), ALL_ONES);
        }
    }

    @Override
    public <T> void clear(Access<T> access, T handle, long offset, long bitIndex) {
        long longIndex = longWithThisBit(bitIndex);
        if (bitIndex < 0 || longIndex >= longLength)
            throw new IndexOutOfBoundsException();
        long byteIndex = firstByte(offset, longIndex);
        long mask = singleBit(bitIndex);
        while (true) {
            long l = access.readVolatileLong(handle, byteIndex);
            if ((l & mask) == 0)
                return;
            long l2 = l & ~mask;
            if (access.compareAndSwapLong(handle, byteIndex, l, l2))
                return;
        }
    }

    @Override
    public <T> boolean clearIfSet(Access<T> access, T handle, long offset, long bitIndex) {
        long longIndex = longWithThisBit(bitIndex);
        if (bitIndex < 0 || longIndex >= longLength)
            throw new IndexOutOfBoundsException();
        long byteIndex = firstByte(offset, longIndex);
        long mask = singleBit(bitIndex);
        while (true) {
            long l = access.readVolatileLong(handle, byteIndex);
            if ((l & mask) == 0) return false;
            long l2 = l & ~mask;
            if (access.compareAndSwapLong(handle, byteIndex, l, l2))
                return true;
        }
    }

    @Override
    public <T> void clearRange(Access<T> access, T handle, long offset,
                               long fromIndex, long exclusiveToIndex) {
        long fromLongIndex = longWithThisBit(fromIndex);
        long toIndex = exclusiveToIndex - 1;
        long toLongIndex = longWithThisBit(toIndex);
        if (fromIndex < 0 || fromIndex > exclusiveToIndex ||
                toLongIndex >= longLength)
            throw new IndexOutOfBoundsException();

        if (fromLongIndex != toLongIndex) {
            long firstFullLongIndex = fromLongIndex;
            if ((fromIndex & 63) != 0) {
                long fromByteIndex = firstByte(offset, fromLongIndex);
                long mask = lowerBitsExcludingThis(fromIndex);
                while (true) {
                    long l = access.readVolatileLong(handle, fromByteIndex);
                    long l2 = l & mask;
                    if (access.compareAndSwapLong(handle, fromByteIndex, l, l2))
                        break;
                }
                firstFullLongIndex++;
            }

            if ((exclusiveToIndex & 63) == 0) {
                for (long i = firstFullLongIndex; i <= toLongIndex; i++) {
                    access.writeOrderedLong(handle, firstByte(offset, i), 0L);
                }
            } else {
                for (long i = firstFullLongIndex; i < toLongIndex; i++) {
                    access.writeOrderedLong(handle, firstByte(offset, i), 0L);
                }

                long toByteIndex = firstByte(offset, toLongIndex);
                long mask = higherBitsExcludingThis(toIndex);
                while (true) {
                    long l = access.readVolatileLong(handle, toByteIndex);
                    long l2 = l & mask;
                    if (access.compareAndSwapLong(handle, toByteIndex, l, l2))
                        return;
                }
            }
        } else {
            long byteIndex = firstByte(offset, fromLongIndex);
            long mask = lowerBitsExcludingThis(fromIndex) | (higherBitsExcludingThis(toIndex));
            while (true) {
                long l = access.readVolatileLong(handle, byteIndex);
                long l2 = l & mask;
                if (access.compareAndSwapLong(handle, byteIndex, l, l2))
                    return;
            }
        }
    }

    @Override
    public <T> void clearAll(Access<T> access, T handle, long offset) {
        access.writeBytes(handle, offset, LONGS.toBytes(longLength), (byte) 0);
    }

    @Override
    public <T> boolean get(Access<T> access, T handle, long offset, long bitIndex) {
        long longIndex = longWithThisBit(bitIndex);
        if (bitIndex < 0 || longIndex >= longLength)
            throw new IndexOutOfBoundsException();
        long l = readVolatileLong(access, handle, offset, longIndex);
        return (l & (singleBit(bitIndex))) != 0;
    }

    @Override
    public <T> boolean isRangeSet(Access<T> access, T handle, long offset,
                                  long fromIndex, long toIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> boolean isRangeClear(Access<T> access, T handle, long offset,
                                    long fromIndex, long toIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> long nextSetBit(Access<T> access, T handle, long offset, long fromIndex) {
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException();
        long fromLongIndex = longWithThisBit(fromIndex);
        if (fromLongIndex >= longLength)
            return NOT_FOUND;
        long l = readVolatileLong(access, handle, offset, fromLongIndex) >>> fromIndex;
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
        while (true) {
            long w = access.readVolatileLong(handle, fromByteIndex);
            long l = w >>> fromIndex;
            if (l != 0) {
                long indexOfSetBit = fromIndex + numberOfTrailingZeros(l);
                long mask = singleBit(indexOfSetBit);
                if (access.compareAndSwapLong(handle, fromByteIndex, w, w ^ mask))
                    return indexOfSetBit;
            } else {
                break;
            }
        }
        longLoop:
        for (long i = fromLongIndex + 1; i < longLength; i++) {
            long byteIndex = firstByte(offset, i);
            while (true) {
                long l = access.readLong(handle, byteIndex);
                if (l != 0) {
                    long indexOfSetBit = firstBit(i) + numberOfTrailingZeros(l);
                    long mask = singleBit(indexOfSetBit);
                    if (access.compareAndSwapLong(handle, byteIndex, l, l ^ mask))
                        return indexOfSetBit;
                } else {
                    continue longLoop;
                }
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
        long l = (~readVolatileLong(access, handle, offset, fromLongIndex)) >>> fromIndex;
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
        while (true) {
            long w = access.readVolatileLong(handle, fromByteIndex);
            long l = (~w) >>> fromIndex;
            if (l != 0) {
                long indexOfClearBit =
                        fromIndex + numberOfTrailingZeros(l);
                long mask = singleBit(indexOfClearBit);
                if (access.compareAndSwapLong(handle, fromByteIndex, w, w ^ mask))
                    return indexOfClearBit;
            } else {
                break;
            }
        }
        longLoop:
        for (long i = fromLongIndex + 1; i < longLength; i++) {
            long byteIndex = firstByte(offset, i);
            while (true) {
                long w = access.readLong(handle, byteIndex);
                long l = ~w;
                if (l != 0) {
                    long indexOfClearBit = firstBit(i) + numberOfTrailingZeros(l);
                    long mask = singleBit(indexOfClearBit);
                    if (access.compareAndSwapLong(handle, byteIndex, w, w ^ mask))
                        return indexOfClearBit;
                } else {
                    continue longLoop;
                }
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
        long l = readVolatileLong(access, handle, offset, fromLongIndex) << ~fromIndex;
        if (l != 0)
            return fromIndex - numberOfLeadingZeros(l);
        for (long i = fromLongIndex - 1; i >= 0; i--) {
            l = readLong(access, handle, offset, i);
            if (l != 0)
                return lastBit(i) - numberOfLeadingZeros(l);
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
        while (true) {
            long w = access.readVolatileLong(handle, fromByteIndex);
            long l = w << ~fromIndex;
            if (l != 0) {
                long indexOfSetBit = fromIndex - numberOfLeadingZeros(l);
                long mask = singleBit(indexOfSetBit);
                if (access.compareAndSwapLong(handle, fromByteIndex, w, w ^ mask))
                    return indexOfSetBit;
            } else {
                break;
            }
        }
        longLoop:
        for (long i = fromLongIndex - 1; i >= 0; i--) {
            long byteIndex = firstByte(offset, i);
            while (true) {
                long l = access.readLong(handle, byteIndex);
                if (l != 0) {
                    long indexOfSetBit = lastBit(i) - numberOfLeadingZeros(l);
                    long mask = singleBit(indexOfSetBit);
                    if (access.compareAndSwapLong(handle, byteIndex, l, l ^ mask))
                        return indexOfSetBit;
                } else {
                    continue longLoop;
                }
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
        long l = (~readVolatileLong(access, handle, offset, fromLongIndex)) << ~fromIndex;
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
        while (true) {
            long w = access.readVolatileLong(handle, fromByteIndex);
            long l = (~w) << ~fromIndex;
            if (l != 0) {
                long indexOfClearBit = fromIndex - numberOfLeadingZeros(l);
                long mask = singleBit(indexOfClearBit);
                if (access.compareAndSwapLong(handle, fromByteIndex, w, w ^ mask))
                    return indexOfClearBit;
            } else {
                break;
            }
        }
        longLoop:
        for (long i = fromLongIndex - 1; i >= 0; i--) {
            long byteIndex = firstByte(offset, i);
            while (true) {
                long w = access.readLong(handle, byteIndex);
                long l = ~w;
                if (l != 0) {
                    long indexOfClearBit = lastBit(i) - numberOfLeadingZeros(l);
                    long mask = singleBit(indexOfClearBit);
                    if (access.compareAndSwapLong(handle, byteIndex, w, w ^ mask))
                        return indexOfClearBit;
                } else {
                    continue longLoop;
                }
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
        long count = Long.bitCount(access.readVolatileLong(handle, 0));
        for (long i = 1; i < longLength; i++) {
            count += Long.bitCount(readLong(access, handle, offset, i));
        }
        return count;
    }

    /**
     * WARNING! This implementation doesn't strictly follow the contract
     * from {@code DirectBitSet} interface. For the sake of atomicity this
     * implementation couldn't find and flip the range crossing native word
     * boundary, e. g. bits from 55 to 75 (boundary is 64).
     *
     * @throws IllegalArgumentException if {@code numberOfBits}
     *         is out of range {@code 0 < numberOfBits && numberOfBits <= 64}
     */
    @Override
    public <T> long setNextNContinuousClearBits(Access<T> access, T handle, long offset,
                                                long fromIndex, int numberOfBits) {
        checkNumberOfBits(numberOfBits);
        if (numberOfBits == 1)
            return setNextClearBit(access, handle, offset, fromIndex);
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException();

        int n64Complement = 64 - numberOfBits;
        long nTrailingOnes = ALL_ONES >>> n64Complement;

        long bitIndex = fromIndex;
        long longIndex = longWithThisBit(bitIndex);
        long byteIndex = firstByte(offset, longIndex);
        long w, l;
        if ((bitIndex & 63) > n64Complement) {
            if (++longIndex >= longLength)
                return NOT_FOUND;
            byteIndex += 8;
            bitIndex = firstBit(longIndex);
            l = w = access.readVolatileLong(handle, byteIndex);
        } else {
            if (longIndex >= longLength)
                return NOT_FOUND;
            w = access.readVolatileLong(handle, byteIndex);
            l = rightShiftOneFill(w, bitIndex);
        }
        // long loop
        while (true) {
            continueLongLoop:
            {
                // (1)
                if ((l & 1) != 0) {
                    long x = ~l;
                    if (x != 0) {
                        int trailingOnes = numberOfTrailingZeros(x);
                        bitIndex += trailingOnes;
                        // i. e. bitIndex + numberOfBits crosses 64 boundary
                        if ((bitIndex & 63) > n64Complement)
                            break continueLongLoop;
                        // (2)
                        l = rightShiftOneFill(l, trailingOnes);
                    } else {
                        // all bits are ones, go to the next long
                        break continueLongLoop;
                    }
                }
                // bit search within a long
                while (true) {
                    // CAS retry loop
                    while ((l & nTrailingOnes) == 0) {
                        long mask = nTrailingOnes << bitIndex;
                        if (access.compareAndSwapLong(handle, byteIndex, w, w ^ mask)) {
                            return bitIndex;
                        } else {
                            w = access.readLong(handle, byteIndex);
                            l = rightShiftOneFill(w, bitIndex);
                        }
                    }
                    // n > trailing zeros > 0
                    // > 0 ensured by block (1)
                    int trailingZeros = numberOfTrailingZeros(l);
                    bitIndex += trailingZeros;
                    // (3)
                    l = rightShiftOneFill(l, trailingZeros);

                    long x = ~l;
                    if (x != 0) {
                        int trailingOnes = numberOfTrailingZeros(x);
                        bitIndex += trailingOnes;
                        // i. e. bitIndex + numberOfBits crosses 64 boundary
                        if ((bitIndex & 63) > n64Complement)
                            break continueLongLoop;
                        // already shifted with one-filling at least once
                        // at (2) or (3), => garanteed highest bit is 1 =>
                        // "natural" one-filling
                        l >>= trailingOnes;
                    } else {
                        // zeros in this long exhausted, go to the next long
                        break continueLongLoop;
                    }
                }
            }
            if (++longIndex >= longLength)
                return NOT_FOUND;
            byteIndex += 8;
            bitIndex = firstBit(longIndex);
            l = w = access.readLong(handle, byteIndex);
        }
    }

    /**
     * WARNING! This implementation doesn't strictly follow the contract
     * from {@code DirectBitSet} interface. For the sake of atomicity this
     * implementation couldn't find and flip the range crossing native word
     * boundary, e. g. bits from 55 to 75 (boundary is 64).
     *
     * @throws IllegalArgumentException if {@code numberOfBits}
     *         is out of range {@code 0 < numberOfBits && numberOfBits <= 64}
     */
    @Override
    public <T> long clearNextNContinuousSetBits(Access<T> access, T handle, long offset,
                                                long fromIndex, int numberOfBits) {
        checkNumberOfBits(numberOfBits);
        if (numberOfBits == 1)
            return clearNextSetBit(access, handle, offset, fromIndex);
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException();

        int n64Complement = 64 - numberOfBits;
        long nTrailingOnes = ALL_ONES >>> n64Complement;

        long bitIndex = fromIndex;
        long longIndex = longWithThisBit(bitIndex);
        long byteIndex = firstByte(offset, longIndex);
        long w, l;
        if ((bitIndex & 63) > n64Complement) {
            if (++longIndex >= longLength)
                return NOT_FOUND;
            byteIndex += 8;
            bitIndex = firstBit(longIndex);
            l = w = access.readVolatileLong(handle, byteIndex);
        } else {
            if (longIndex >= longLength)
                return NOT_FOUND;
            w = access.readVolatileLong(handle, byteIndex);
            l = w >>> bitIndex;
        }
        // long loop
        while (true) {
            continueLongLoop:
            {
                if ((l & 1) == 0) {
                    if (l != 0) {
                        int trailingZeros = numberOfTrailingZeros(l);
                        bitIndex += trailingZeros;
                        // i. e. bitIndex + numberOfBits crosses 64 boundary
                        if ((bitIndex & 63) > n64Complement)
                            break continueLongLoop;
                        l >>>= trailingZeros;
                    } else {
                        // all bits are zeros, go to the next long
                        break continueLongLoop;
                    }
                }
                // bit search within a long
                while (true) {
                    // CAS retry loop
                    while (((~l) & nTrailingOnes) == 0) {
                        long mask = nTrailingOnes << bitIndex;
                        if (access.compareAndSwapLong(handle, byteIndex, w, w ^ mask)) {
                            return bitIndex;
                        } else {
                            w = access.readLong(handle, byteIndex);
                            l = w >>> bitIndex;
                        }
                    }
                    // n > trailing ones > 0
                    int trailingOnes = numberOfTrailingZeros(~l);
                    bitIndex += trailingOnes;
                    l >>>= trailingOnes;

                    if (l != 0) {
                        int trailingZeros = numberOfTrailingZeros(l);
                        bitIndex += trailingZeros;
                        // i. e. bitIndex + numberOfBits crosses 64 boundary
                        if ((bitIndex & 63) > n64Complement)
                            break continueLongLoop;
                        l >>>= trailingZeros;
                    } else {
                        // ones in this long exhausted, go to the next long
                        break continueLongLoop;
                    }
                }
            }
            if (++longIndex >= longLength)
                return NOT_FOUND;
            byteIndex += 8;
            bitIndex = firstBit(longIndex);
            l = w = access.readLong(handle, byteIndex);
        }
    }

    /**
     * WARNING! This implementation doesn't strictly follow the contract
     * from {@code DirectBitSet} interface. For the sake of atomicity this
     * implementation couldn't find and flip the range crossing native word
     * boundary, e. g. bits from 55 to 75 (boundary is 64).
     *
     * @throws IllegalArgumentException if {@code numberOfBits}
     *         is out of range {@code 0 < numberOfBits && numberOfBits <= 64}
     */
    @Override
    public <T> long setPreviousNContinuousClearBits(Access<T> access, T handle, long offset,
            long fromIndex, int numberOfBits) {
        checkNumberOfBits(numberOfBits);
        if (numberOfBits == 1)
            return setPreviousClearBit(access, handle, offset, fromIndex);
        if (checkNotFoundIndex(fromIndex))
            return NOT_FOUND;

        int numberOfBitsMinusOne = numberOfBits - 1;
        long nLeadingOnes = ALL_ONES << (64 - numberOfBits);

        long bitIndex = fromIndex;
        long longIndex = longWithThisBit(bitIndex);
        if (longIndex >= longLength) {
            longIndex = longLength - 1;
            bitIndex = lastBit(longIndex);
        }
        long byteIndex = firstByte(offset, longIndex);
        long w, l;
        if ((bitIndex & 63) < numberOfBitsMinusOne) {
            if (--longIndex < 0)
                return NOT_FOUND;
            byteIndex -= 8;
            bitIndex = lastBit(longIndex);
            l = w = access.readVolatileLong(handle, byteIndex);
        } else {
            w = access.readVolatileLong(handle, byteIndex);
            // left shift by ~bitIndex === left shift by (63 - (bitIndex & 63))
            l = leftShiftOneFill(w, ~bitIndex);
        }
        // long loop
        while (true) {
            continueLongLoop:
            {
                if (l < 0) { // condition means the highest bit is one
                    long x = ~l;
                    if (x != 0) {
                        int leadingOnes = numberOfLeadingZeros(x);
                        bitIndex -= leadingOnes;
                        if ((bitIndex & 63) < numberOfBitsMinusOne)
                            break continueLongLoop;
                        l = leftShiftOneFill(l, leadingOnes);
                    } else {
                        // all bits are ones, go to the next long
                        break continueLongLoop;
                    }
                }
                // bit search within a long
                while (true) {
                    // CAS retry loop
                    while ((l & nLeadingOnes) == 0) {
                        // >>> ~bitIndex === >>> (63 - (butIndex & 63))
                        long mask = nLeadingOnes >>> ~bitIndex;
                        if (access.compareAndSwapLong(handle, byteIndex, w, w ^ mask)) {
                            return bitIndex - numberOfBitsMinusOne;
                        } else {
                            w = access.readLong(handle, byteIndex);
                            l = leftShiftOneFill(w, ~bitIndex);
                        }
                    }
                    // n > leading zeros > 0
                    int leadingZeros = numberOfLeadingZeros(l);
                    bitIndex -= leadingZeros;
                    l = leftShiftOneFill(l, leadingZeros);

                    long x = ~l;
                    if (x != 0) {
                        int leadingOnes = numberOfLeadingZeros(x);
                        bitIndex -= leadingOnes;
                        if ((bitIndex & 63) < numberOfBitsMinusOne)
                            break continueLongLoop;
                        l = leftShiftOneFill(l, leadingOnes);
                    } else {
                        // zeros in this long exhausted, go to the next long
                        break continueLongLoop;
                    }
                }
            }
            if (--longIndex < 0)
                return NOT_FOUND;
            byteIndex -= 8;
            bitIndex = lastBit(longIndex);
            l = w = access.readLong(handle, byteIndex);
        }
    }

    /**
     * WARNING! This implementation doesn't strictly follow the contract
     * from {@code DirectBitSet} interface. For the sake of atomicity this
     * implementation couldn't find and flip the range crossing native word
     * boundary, e. g. bits from 55 to 75 (boundary is 64).
     *
     * @throws IllegalArgumentException if {@code numberOfBits}
     *         is out of range {@code 0 < numberOfBits && numberOfBits <= 64}
     */
    @Override
    public <T> long clearPreviousNContinuousSetBits(Access<T> access, T handle, long offset,
            long fromIndex, int numberOfBits) {
        checkNumberOfBits(numberOfBits);
        if (numberOfBits == 1)
            return clearPreviousSetBit(access, handle, offset, fromIndex);
        if (checkNotFoundIndex(fromIndex))
            return NOT_FOUND;

        int numberOfBitsMinusOne = numberOfBits - 1;
        long nLeadingOnes = ALL_ONES << (64 - numberOfBits);

        long bitIndex = fromIndex;
        long longIndex = longWithThisBit(bitIndex);
        if (longIndex >= longLength) {
            longIndex = longLength - 1;
            bitIndex = lastBit(longIndex);
        }
        long byteIndex = firstByte(offset, longIndex);
        long w, l;
        if ((bitIndex & 63) < numberOfBitsMinusOne) {
            if (--longIndex < 0)
                return NOT_FOUND;
            byteIndex -= 8;
            bitIndex = lastBit(longIndex);
            l = w = access.readVolatileLong(handle, byteIndex);
        } else {
            w = access.readVolatileLong(handle, byteIndex);
            // << ~bitIndex === << (63 - (bitIndex & 63))
            l = w << ~bitIndex;
        }
        // long loop
        while (true) {
            continueLongLoop:
            {
                // condition means the highest bit is zero, but not all
                if (l > 0) {
                    int leadingZeros = numberOfLeadingZeros(l);
                    bitIndex -= leadingZeros;
                    if ((bitIndex & 63) < numberOfBitsMinusOne)
                        break continueLongLoop;
                    l <<= leadingZeros;
                } else if (l == 0) {
                    // all bits are zeros, go to the next long
                    break continueLongLoop;
                }
                // bit search within a long
                while (true) {
                    // CAS retry loop
                    while (((~l) & nLeadingOnes) == 0) {
                        // >>> ~bitIndex === >>> (63 - (butIndex & 63))
                        long mask = nLeadingOnes >>> ~bitIndex;
                        if (access.compareAndSwapLong(handle, byteIndex, w, w ^ mask)) {
                            return bitIndex - numberOfBitsMinusOne;
                        } else {
                            w = access.readLong(handle, byteIndex);
                            l = w << ~bitIndex;
                        }
                    }
                    // n > leading ones > 0
                    int leadingOnes = numberOfLeadingZeros(~l);
                    bitIndex -= leadingOnes;
                    l <<= leadingOnes;

                    if (l != 0) {
                        int leadingZeros = numberOfLeadingZeros(l);
                        bitIndex -= leadingZeros;
                        if ((bitIndex & 63) < numberOfBitsMinusOne)
                            break continueLongLoop;
                        l <<= leadingZeros;
                    } else {
                        // ones in this long exhausted, go to the next long
                        break continueLongLoop;
                    }
                }
            }
            if (--longIndex < 0)
                return NOT_FOUND;
            byteIndex -= 8;
            bitIndex = lastBit(longIndex);
            l = w = access.readLong(handle, byteIndex);
        }
    }

    private class SetBits implements Bits {
        private final long byteLength = longLength << 3;
        private long byteIndex = 0;
        private long bitIndex = 0;

        @Override
        public <T> Bits reset(Access<T> access, T handle, long offset) {
            byteIndex = 0;
            bitIndex = 0;
            return this;
        }

        @Override
        public <T> long next(Access<T> access, T handle, long offset) {
            long bitIndex = this.bitIndex;
            if (bitIndex >= 0) {
                long i = byteIndex;
                long l = access.readVolatileLong(handle, i) >>> bitIndex;
                if (l != 0) {
                    int trailingZeros = numberOfTrailingZeros(l);
                    long index = bitIndex + trailingZeros;
                    if (((this.bitIndex = index + 1) & 63) == 0) {
                        if ((byteIndex = i + 8) == byteLength)
                            this.bitIndex = -1;
                    }
                    return index;
                }
                for (long lim = byteLength; (i += 8) < lim; ) {
                    if ((l = access.readLong(handle, i)) != 0) {
                        int trailingZeros = numberOfTrailingZeros(l);
                        long index = (i << 3) + trailingZeros;
                        if (((this.bitIndex = index + 1) & 63) != 0) {
                            byteIndex = i;
                        } else {
                            if ((byteIndex = i + 8) == lim)
                                this.bitIndex = -1;
                        }
                        return index;
                    }
                }
            }
            this.bitIndex = -1;
            return -1;
        }
    }
}
