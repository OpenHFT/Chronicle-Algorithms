/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo.bitset;

import net.openhft.chronicle.algo.MemoryUnit;
import net.openhft.chronicle.algo.bytes.Access;
import net.openhft.chronicle.bytes.BytesStore;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;

import static net.openhft.chronicle.algo.bytes.Access.checkedByteBufferAccess;
import static org.junit.jupiter.api.Assertions.*;

public class DirectBitSetTest {

    private static final int[] INDICES = new int[]{0, 50, 100, 127, 128, 255};
    private ReusableBitSet bs;
    private boolean singleThreaded;

    private void init(ReusableBitSet bs) {
        this.bs = bs;
        this.singleThreaded = bs.frame instanceof SingleThreadedFlatBitSetFrame;
        assertTrue(bs.logicalSize() >= 256);
    }

    @SuppressWarnings("unchecked")
    static Collection<Object[]> data() {
        int capacityInBytes = (int) MemoryUnit.BITS.toBytes(256);
        BytesStore<?, ByteBuffer> bytes1 = BytesStore.wrap(ByteBuffer.allocateDirect(capacityInBytes));
        BytesStore<?, ByteBuffer> bytes2 = BytesStore.wrap(ByteBuffer.allocateDirect(capacityInBytes));
        return Arrays.asList(new Object[][]{
                {
                        new ReusableBitSet(
                                new ConcurrentFlatBitSetFrame(256),
                                (Access) Access.checkedBytesStoreAccess(),
                                bytes1,
                                0)
                },
                {
                        new ReusableBitSet(
                                new SingleThreadedFlatBitSetFrame(256),
                                checkedByteBufferAccess(),
                                ByteBuffer.allocate(capacityInBytes),
                                0)
                },
                {
                        new ReusableBitSet(
                                new ConcurrentFlatBitSetFrame(256),
                                (Access) Access.checkedBytesStoreAccess(),
                                bytes2,
                                0)
                },
                {
                        new ReusableBitSet(
                                new SingleThreadedFlatBitSetFrame(256),
                                checkedByteBufferAccess(),
                                ByteBuffer.allocateDirect(capacityInBytes),
                                0)
                },

        });
    }

    private void setIndices() {
        bs.clearAll();
        for (int i : INDICES) {
            bs.set(i);
        }
    }

    private void setIndicesComplement() {
        setIndices();
        bs.flipRange(0, bs.logicalSize());
    }

    private void assertRangeIsClear(long from, long to) {
        for (long i = from; i < to; i++) {
            assertFalse(bs.get(i));
        }
    }

    private void assertRangeIsClear(String message, long from, long to) {
        for (long i = from; i < to; i++) {
            assertFalse(bs.get(i), message + ", bit: " + i);
        }
    }

    private void assertRangeIsSet(long from, long to) {
        for (long i = from; i < to; i++) {
            assertTrue(bs.get(i));
        }
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testGetSetClearAndCardinality(ReusableBitSet bs) {
        init(bs);
        bs.clearAll();
        assertEquals(0, bs.cardinality());
        int c = 0;
        for (int i : INDICES) {
            c++;
            assertFalse(bs.get(i), "At index " + i);
            assertFalse(bs.isSet(i), "At index " + i);
            assertTrue(bs.isClear(i), "At index " + i);
            bs.set(i);
            assertTrue(bs.get(i), "At index " + i);
            assertTrue(bs.isSet(i), "At index " + i);
            assertFalse(bs.isClear(i), "At index " + i);
            assertEquals(c, bs.cardinality());
        }
        for (int i : INDICES) {
            assertTrue(bs.get(i), "At index " + i);
            assertTrue(bs.isSet(i), "At index " + i);
            assertFalse(bs.isClear(i), "At index " + i);
            bs.clear(i);
            assertFalse(bs.get(i), "At index " + i);
            assertFalse(bs.isSet(i), "At index " + i);
            assertTrue(bs.isClear(i), "At index " + i);
        }
        for (int i : INDICES) {
            assertTrue(bs.setIfClear(i), "At index " + i);
            assertFalse(bs.setIfClear(i), "At index " + i);
        }
        for (int i : INDICES) {
            assertTrue(bs.clearIfSet(i), "At index " + i);
            assertFalse(bs.clearIfSet(i), "At index " + i);
        }
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testFlip(ReusableBitSet bs) {
        init(bs);
        bs.clearAll();
        for (int i : INDICES) {
            assertFalse(bs.get(i), "At index " + i);
            bs.flip(i);
            assertTrue(bs.get(i), "At index " + i);
            bs.flip(i);
            assertFalse(bs.get(i), "At index " + i);
        }
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testNextSetBit(ReusableBitSet bs) {
        init(bs);
        setIndices();
        int order = 0;
        for (long i = bs.nextSetBit(0L); i >= 0; i = bs.nextSetBit(i + 1)) {
            assertEquals(INDICES[order], i);
            order++;
        }
        assertEquals(-1, bs.nextSetBit(bs.logicalSize()));

        bs.clearAll();
        assertEquals(-1, bs.nextSetBit(0L));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testSetBitsIteration(ReusableBitSet bs) {
        init(bs);
        setIndices();

        BitSet.Bits bits = bs.setBits().reset();
        long i;
        int order = 0;
        while ((i = bits.next()) >= 0) {
            assertEquals(INDICES[order], i);
            order++;
        }
        assertEquals(-1, bits.next());

        // reset support
        bits.reset();
        order = 0;
        while ((i = bits.next()) >= 0) {
            assertEquals(INDICES[order], i);
            order++;
        }
        assertEquals(-1, bits.next());

        bs.clearAll();
        assertEquals(-1, bits.reset().next());
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testClearNextSetBit(ReusableBitSet bs) {
        init(bs);
        setIndices();
        long cardinality = bs.cardinality();
        int order = 0;
        for (long i = bs.clearNextSetBit(0L); i >= 0;
             i = bs.clearNextSetBit(i + 1)) {
            assertEquals(INDICES[order], i);
            assertFalse(bs.get(i));
            order++;
            cardinality--;
            assertEquals(cardinality, bs.cardinality());
        }
        assertEquals(-1, bs.clearNextSetBit(bs.logicalSize()));
        assertEquals(0, bs.cardinality());
        assertEquals(-1, bs.clearNextSetBit(0L));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testClearNext1SetBit(ReusableBitSet bs) {
        init(bs);
        setIndices();
        long cardinality = bs.cardinality();
        int order = 0;
        for (long i = bs.clearNextNContinuousSetBits(0L, 1); i >= 0;
             i = bs.clearNextNContinuousSetBits(i + 1, 1)) {
            assertEquals(INDICES[order], i);
            assertFalse(bs.get(i));
            order++;
            cardinality--;
            assertEquals(cardinality, bs.cardinality());
        }
        assertEquals(-1, bs.clearNextNContinuousSetBits(bs.logicalSize(), 1));
        assertEquals(0, bs.cardinality());
        assertEquals(-1, bs.clearNextNContinuousSetBits(0L, 1));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testNextClearBit(ReusableBitSet bs) {
        init(bs);
        setIndicesComplement();
        int order = 0;
        for (long i = bs.nextClearBit(0L); i >= 0; i = bs.nextClearBit(i + 1)) {
            assertEquals(INDICES[order], i);
            order++;
        }
        assertEquals(-1, bs.nextClearBit(bs.logicalSize()));

        bs.setAll();
        assertEquals(-1, bs.nextClearBit(0L));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testSetNextClearBit(ReusableBitSet bs) {
        init(bs);
        setIndicesComplement();
        long cardinality = bs.cardinality();
        int order = 0;
        for (long i = bs.setNextClearBit(0L); i >= 0;
             i = bs.setNextClearBit(i + 1)) {
            assertEquals(INDICES[order], i);
            assertTrue(bs.get(i));
            order++;
            cardinality++;
            assertEquals(cardinality, bs.cardinality());
        }
        assertEquals(-1, bs.setNextClearBit(bs.logicalSize()));
        assertEquals(bs.logicalSize(), bs.cardinality());
        assertEquals(-1, bs.setNextClearBit(0L));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testSetNext1ClearBit(ReusableBitSet bs) {
        init(bs);
        setIndicesComplement();
        long cardinality = bs.cardinality();
        int order = 0;
        for (long i = bs.setNextNContinuousClearBits(0L, 1); i >= 0;
             i = bs.setNextNContinuousClearBits(i + 1, 1)) {
            assertEquals(INDICES[order], i);
            assertTrue(bs.get(i));
            order++;
            cardinality++;
            assertEquals(cardinality, bs.cardinality());
        }
        assertEquals(-1, bs.setNextNContinuousClearBits(bs.logicalSize(), 1));
        assertEquals(bs.logicalSize(), bs.cardinality());
        assertEquals(-1, bs.setNextNContinuousClearBits(0L, 1));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testPreviousSetBit(ReusableBitSet bs) {
        init(bs);
        setIndices();
        int order = INDICES.length;
        for (long i = bs.logicalSize(); (i = bs.previousSetBit(i - 1)) >= 0; ) {
            order--;
            assertEquals(INDICES[order], i);
        }
        assertEquals(-1, bs.previousSetBit(-1));

        bs.clearAll();
        assertEquals(-1, bs.previousSetBit(bs.logicalSize()));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testClearPreviousSetBit(ReusableBitSet bs) {
        init(bs);
        setIndices();
        long cardinality = bs.cardinality();
        int order = INDICES.length;
        for (long i = bs.logicalSize(); (i = bs.clearPreviousSetBit(i - 1)) >= 0; ) {
            order--;
            cardinality--;
            assertEquals(INDICES[order], i);
            assertFalse(bs.get(i));
            assertEquals(cardinality, bs.cardinality());
        }
        assertEquals(-1, bs.clearPreviousSetBit(-1));
        assertEquals(0, bs.cardinality());
        assertEquals(-1, bs.clearPreviousSetBit(bs.logicalSize()));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testClearPrevious1SetBit(ReusableBitSet bs) {
        init(bs);
        setIndices();
        long cardinality = bs.cardinality();
        int order = INDICES.length;
        for (long i = bs.logicalSize();
             (i = bs.clearPreviousNContinuousSetBits(i - 1, 1)) >= 0; ) {
            order--;
            cardinality--;
            assertEquals(INDICES[order], i);
            assertFalse(bs.get(i));
            assertEquals(cardinality, bs.cardinality());
        }
        assertEquals(-1, bs.clearPreviousNContinuousSetBits(-1, 1));
        assertEquals(0, bs.cardinality());
        assertEquals(-1, bs.clearPreviousNContinuousSetBits(bs.logicalSize(), 1));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testPreviousClearBit(ReusableBitSet bs) {
        init(bs);
        setIndicesComplement();
        int order = INDICES.length;
        for (long i = bs.logicalSize(); (i = bs.previousClearBit(i - 1)) >= 0; ) {
            order--;
            assertEquals(INDICES[order], i);
        }
        assertEquals(-1, bs.previousClearBit(-1));

        bs.setAll();
        assertEquals(-1, bs.previousClearBit(bs.logicalSize()));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testSetPreviousClearBit(ReusableBitSet bs) {
        init(bs);
        setIndicesComplement();
        long cardinality = bs.cardinality();
        int order = INDICES.length;
        for (long i = bs.logicalSize(); (i = bs.setPreviousClearBit(i - 1)) >= 0; ) {
            order--;
            cardinality++;
            assertEquals(INDICES[order], i);
            assertTrue(bs.get(i));
            assertEquals(cardinality, bs.cardinality());
        }
        assertEquals(-1, bs.setPreviousClearBit(-1));
        assertEquals(bs.logicalSize(), bs.cardinality());
        assertEquals(-1, bs.setPreviousClearBit(bs.logicalSize()));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testSetPrevious1ClearBit(ReusableBitSet bs) {
        init(bs);
        setIndicesComplement();
        long cardinality = bs.cardinality();
        int order = INDICES.length;
        for (long i = bs.logicalSize();
             (i = bs.setPreviousNContinuousClearBits(i - 1, 1)) >= 0; ) {
            order--;
            cardinality++;
            assertEquals(INDICES[order], i);
            assertTrue(bs.get(i));
            assertEquals(cardinality, bs.cardinality());
        }
        assertEquals(-1, bs.setPreviousNContinuousClearBits(-1, 1));
        assertEquals(bs.logicalSize(), bs.cardinality());
        assertEquals(-1, bs.setPreviousNContinuousClearBits(bs.logicalSize(), 1));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testSetAll(ReusableBitSet bs) {
        init(bs);
        bs.clearAll();
        bs.setAll();
        assertEquals(bs.logicalSize(), bs.cardinality());
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testRangeOpsWithinLongCase(ReusableBitSet bs) {
        init(bs);
        bs.clearAll();
        if (singleThreaded) {
            assertTrue(bs.isRangeClear(0, 0));
            assertTrue(bs.isRangeClear(63, 63));
            assertTrue(bs.isRangeSet(0, 0));
            assertTrue(bs.isRangeSet(63, 63));
        }
        bs.flipRange(0, 0);
        assertFalse(bs.get(0));
        assertEquals(0, bs.cardinality());
        bs.flipRange(0, 1);
        assertTrue(bs.get(0));
        assertEquals(1, bs.cardinality());
        if (singleThreaded) {
            assertTrue(bs.isRangeSet(0, 1));
            assertFalse(bs.isRangeSet(0, 2));
            assertFalse(bs.isRangeClear(0, 1));
        }
        bs.clearRange(0, 0);
        assertTrue(bs.get(0));
        assertEquals(1, bs.cardinality());
        bs.clearRange(0, 1);
        assertFalse(bs.get(0));
        assertEquals(0, bs.cardinality());

        bs.setRange(0, 0);
        assertFalse(bs.get(0));
        assertEquals(0, bs.cardinality());
        bs.setRange(0, 1);
        assertTrue(bs.get(0));
        assertEquals(1, bs.cardinality());
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testRangeOpsCrossLongCase(ReusableBitSet bs) {
        init(bs);
        bs.clearAll();

        bs.flipRange(63, 64);
        assertTrue(bs.get(63));
        assertFalse(bs.get(64));
        assertEquals(1, bs.cardinality());
        if (singleThreaded) {
            assertFalse(bs.isRangeSet(63, 65));
            assertFalse(bs.isRangeClear(63, 65));
        }
        bs.flipRange(63, 65);
        assertFalse(bs.get(63));
        assertTrue(bs.get(64));
        assertEquals(1, bs.cardinality());
        if (singleThreaded) {
            assertFalse(bs.isRangeSet(63, 65));
            assertFalse(bs.isRangeClear(63, 65));
        }
        bs.clear(64);
        bs.setRange(63, 64);
        assertTrue(bs.get(63));
        assertFalse(bs.get(64));
        assertEquals(1, bs.cardinality());

        bs.set(64);
        bs.clearRange(63, 64);
        assertFalse(bs.get(63));
        assertTrue(bs.get(64));
        assertEquals(1, bs.cardinality());

        bs.clear(64);
        bs.setRange(63, 65);
        assertTrue(bs.get(63));
        assertTrue(bs.get(64));
        assertEquals(2, bs.cardinality());
        if (singleThreaded) {
            assertTrue(bs.isRangeSet(63, 65));
            assertFalse(bs.isRangeClear(63, 65));
        }
        bs.clearRange(63, 65);
        assertFalse(bs.get(63));
        assertFalse(bs.get(64));
        assertEquals(0, bs.cardinality());
        if (singleThreaded) {
            assertFalse(bs.isRangeSet(63, 65));
            assertTrue(bs.isRangeClear(63, 65));
        }
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testRangeOpsSpanLongCase(ReusableBitSet bs) {
        init(bs);
        bs.clearAll();
        if (singleThreaded) {
            assertTrue(bs.isRangeClear(0, bs.logicalSize()));
            assertFalse(bs.isRangeSet(0, bs.logicalSize()));
        }
        bs.setRange(0, bs.logicalSize());
        assertEquals(bs.logicalSize(), bs.cardinality());
        if (singleThreaded) {
            assertFalse(bs.isRangeClear(0, bs.logicalSize()));
            assertTrue(bs.isRangeSet(0, bs.logicalSize()));
        }
        bs.clearRange(0, bs.logicalSize());
        assertEquals(0, bs.cardinality());

        bs.flipRange(0, bs.logicalSize());
        assertEquals(bs.logicalSize(), bs.cardinality());
    }

    private String m(int n) {
        return "N: " + n + ", " + bs.getClass().getSimpleName();
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testSetNextNContinuousClearBitsWithinLongCase(ReusableBitSet bs) {
        init(bs);
        long size = (bs.logicalSize() + 63) / 64 * 64;
        for (int n = 1; n <= 64; n *= 2) {
            bs.clearAll();
            for (int i = 0; i < size / n; i++) {
                assertRangeIsClear(i * n, i * n + n);
                assertEquals(i * n, bs.setNextNContinuousClearBits(0L, n), m(n));
                assertRangeIsSet(i * n, i * n + n);
                assertEquals(i * n + n, bs.cardinality());
            }
        }
        for (int n = 2; n <= 64; n *= 2) {
            bs.setAll();
            bs.clearRange(size - n, size);
            assertEquals(size - n, bs.setNextNContinuousClearBits(0L, n));
            assertRangeIsSet(size - n, size);

            long offset = (64 - n) / 2;
            long from = size - n - offset;
            long to = size - offset;
            bs.clearRange(from, to);
            assertEquals(from, bs.setNextNContinuousClearBits(from, n));
            assertRangeIsSet(from, to);

            bs.clearRange(from, to);
            for (long i = from - 2; i >= 0; i -= 2) {
                bs.clear(i);
            }
            long cardinality = bs.cardinality();
            assertEquals(from, bs.setNextNContinuousClearBits(0, n));
            assertEquals(cardinality + n, bs.cardinality());
        }
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testSetNextNContinuousClearBitsCrossLongCase(ReusableBitSet bs) {
        init(bs);
        if (concurrentBS())
            return;
        long size = bs.logicalSize();
        for (int n : new int[]{3, 7, 13, 31, 33, 63, 65, 100, 127, 128, 129, 254, 255}) {
            bs.clearAll();
            for (int i = 0; i < size / n; i++) {
                assertRangeIsClear(i * n, i * n + n);
                assertEquals(i * n, bs.setNextNContinuousClearBits(0L, n), m(n));
                assertRangeIsSet(i * n, i * n + n);
                assertEquals(i * n + n, bs.cardinality());
            }
        }
        long lastBound = size - (size % 64 == 0 ? 64 : size % 64);
        for (int n : new int[]{2, 3, 7, 13, 31, 33, 63, 64, 65, 100, 127, 128, 129}) {
            bs.setAll();
            long from = n <= 64 ? lastBound - (n / 2) : 30;
            long to = from + n;
            bs.clearRange(from, to);
            assertEquals(from, bs.setNextNContinuousClearBits(0L, n), "" + n);
            assertRangeIsSet(from, to);

            bs.clearRange(from, to);
            for (long i = from - 2; i >= 0; i -= 2) {
                bs.clear(i);
            }
            for (long i = to + 1; i < bs.logicalSize(); i += 2) {
                bs.clear(i);
            }
            long cardinality = bs.cardinality();
            assertEquals(from, bs.setNextNContinuousClearBits(from, n));
            assertEquals(cardinality + n, bs.cardinality());
        }
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testClearNextNContinuousSetBitsWithinLongCase(ReusableBitSet bs) {
        init(bs);
        long size = (bs.logicalSize() + 63) / 64 * 64;
        for (int n = 1; n <= 64; n *= 2) {
            bs.setAll();
            long cardinality = bs.cardinality();
            for (int i = 0; i < size / n; i++) {
                assertRangeIsSet(i * n, i * n + n);
                assertEquals(i * n, bs.clearNextNContinuousSetBits(0L, n), m(n));
                assertRangeIsClear(i * n, i * n + n);
                assertEquals(cardinality - (i * n + n), bs.cardinality());
            }
        }
        for (int n = 2; n <= 64; n *= 2) {
            bs.clearAll();
            bs.setRange(size - n, size);
            assertEquals(size - n, bs.clearNextNContinuousSetBits(0L, n));
            assertRangeIsClear(size - n, size);

            long offset = (64 - n) / 2;
            long from = size - n - offset;
            long to = size - offset;
            bs.setRange(from, to);
            assertEquals(from, bs.clearNextNContinuousSetBits(from, n));
            assertRangeIsClear(from, to);

            bs.setRange(from, to);
            for (long i = from - 2; i >= 0; i -= 2) {
                bs.set(i);
            }
            long cardinality = bs.cardinality();
            assertEquals(from, bs.clearNextNContinuousSetBits(0, n));
            assertEquals(cardinality - n, bs.cardinality());
        }
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testClearNextNContinuousSetBitsCrossLongCase(ReusableBitSet bs) {
        init(bs);
        if (concurrentBS())
            return;
        long size = bs.logicalSize();
        for (int n : new int[]{3, 7, 13, 31, 33, 63}) {
            bs.setAll();
            long cardinality = bs.cardinality();
            for (int i = 0; i < size / n; i++) {
                assertRangeIsSet(i * n, i * n + n);
                assertEquals(i * n, bs.clearNextNContinuousSetBits(0L, n), m(n));
                assertRangeIsClear(i * n, i * n + n);
                assertEquals(cardinality -= n, bs.cardinality());
            }
        }
        long lastBound = size - (size % 64 == 0 ? 64 : size % 64);
        for (int n : new int[]{2, 3, 7, 13, 31, 33, 63, 64}) {
            bs.clearAll();
            long from = lastBound - (n / 2);
            long to = from + n;
            bs.setRange(from, to);
            assertEquals(from, bs.clearNextNContinuousSetBits(0L, n));
            assertRangeIsClear(from, to);

            bs.setRange(from, to);
            for (long i = from - 2; i >= 0; i -= 2) {
                bs.set(i);
            }
            for (long i = to + 1; i < bs.logicalSize(); i += 2) {
                bs.set(i);
            }
            long cardinality = bs.cardinality();
            assertEquals(from, bs.clearNextNContinuousSetBits(from, n));
            assertEquals(cardinality - n, bs.cardinality());
        }
    }

    private boolean concurrentBS() {
        return bs.frame instanceof ConcurrentFlatBitSetFrame;
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testSetPreviousNContinuousClearBitsWithinLongCase(ReusableBitSet bs) {
        init(bs);
        long size = (bs.logicalSize() + 63) / 64 * 64;
        for (int n = 1; n <= 64; n *= 2) {
            bs.clearAll();
            long cardinality = 0;
            for (long i = size / n - 1; i >= 0; i--) {
                assertRangeIsClear(i * n, i * n + n);
                assertEquals(i * n, bs.setPreviousNContinuousClearBits(size, n), m(n));
                assertRangeIsSet(i * n, i * n + n);
                assertEquals(cardinality += n, bs.cardinality());
            }
        }
        for (int n = 2; n <= 64; n *= 2) {
            bs.setAll();
            bs.clearRange(0, n);
            assertEquals(0, bs.setPreviousNContinuousClearBits(bs.logicalSize(), n));
            assertRangeIsSet(0, n);

            long from = (64 - n) / 2;
            long to = from + n;
            bs.clearRange(from, to);
            assertEquals(from, bs.setPreviousNContinuousClearBits(to - 1, n));
            assertRangeIsSet(from, to);

            bs.clearRange(from, to);
            for (long i = to + 1; i < bs.logicalSize(); i += 2) {
                bs.clear(i);
            }
            long cardinality = bs.cardinality();
            assertEquals(from, bs.setPreviousNContinuousClearBits(bs.logicalSize(), n));
            assertEquals(cardinality + n, bs.cardinality());
        }
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testSetPreviousNContinuousClearBitsCrossLongCase(ReusableBitSet bs) {
        init(bs);
        if (concurrentBS())
            return;
        long size = bs.logicalSize();
        for (int n : new int[]{3, 7, 13, 31, 33, 63}) {
            bs.clearAll();
            long cardinality = 0;
            for (long from = size - n; from >= 0; from -= n) {
                assertRangeIsClear(from, from + n);
                assertEquals(from, bs.setPreviousNContinuousClearBits(size, n), m(n));
                assertRangeIsSet(from, from + n);
                assertEquals(cardinality += n, bs.cardinality());
            }
        }
        for (int n : new int[]{2, 3, 7, 13, 31, 33, 63, 64}) {
            bs.setAll();
            long from = 64 - (n / 2);
            long to = from + n;
            bs.clearRange(from, to);
            assertEquals(from, bs.setPreviousNContinuousClearBits(size, n));
            assertRangeIsSet(from, to);

            bs.clearRange(from, to);
            for (long i = from - 2; i >= 0; i -= 2) {
                bs.clear(i);
            }
            for (long i = to + 1; i < bs.logicalSize(); i += 2) {
                bs.clear(i);
            }
            long cardinality = bs.cardinality();
            assertEquals(from, bs.setPreviousNContinuousClearBits(to - 1, n));
            assertEquals(cardinality + n, bs.cardinality());
        }
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testClearPreviousNContinuousSetBitsWithinLongCase(ReusableBitSet bs) {
        init(bs);
        long size = (bs.logicalSize() + 63) / 64 * 64;
        for (int n = 1; n <= 64; n *= 2) {
            bs.setAll();
            long cardinality = bs.cardinality();
            for (long i = size / n - 1; i >= 0; i--) {
                assertRangeIsSet(i * n, i * n + n);
                assertEquals(i * n, bs.clearPreviousNContinuousSetBits(size, n), m(n));
                assertRangeIsClear(m(n), i * n, i * n + n);
                assertEquals(cardinality -= n, bs.cardinality());
            }
        }
        for (int n = 2; n <= 64; n *= 2) {
            bs.clearAll();
            bs.setRange(0, n);
            assertEquals(0, bs.clearPreviousNContinuousSetBits(bs.logicalSize(), n));
            assertRangeIsClear(0, n);

            long from = (64 - n) / 2;
            long to = from + n;
            bs.setRange(from, to);
            assertEquals(from, bs.clearPreviousNContinuousSetBits(to - 1, n));
            assertRangeIsClear(from, to);

            bs.setRange(from, to);
            for (long i = to + 1; i < bs.logicalSize(); i += 2) {
                bs.set(i);
            }
            long cardinality = bs.cardinality();
            assertEquals(from, bs.clearPreviousNContinuousSetBits(bs.logicalSize(), n));
            assertEquals(cardinality - n, bs.cardinality());
        }
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testClearPreviousNContinuousSetBitsCrossLongCase(ReusableBitSet bs) {
        init(bs);
        if (concurrentBS())
            return;
        long size = bs.logicalSize();
        for (int n : new int[]{3, 7, 13, 31, 33, 63}) {
            bs.setAll();
            long cardinality = bs.cardinality();
            for (long from = size - n; from >= 0; from -= n) {
                assertRangeIsSet(from, from + n);
                assertEquals(from, bs.clearPreviousNContinuousSetBits(size, n), m(n));
                assertRangeIsClear(from, from + n);
                assertEquals(cardinality -= n, bs.cardinality());
            }
        }
        for (int n : new int[]{2, 3, 7, 13, 31, 33, 63, 64}) {
            bs.clearAll();
            long from = 64 - (n / 2);
            long to = from + n;
            bs.setRange(from, to);
            assertEquals(from, bs.clearPreviousNContinuousSetBits(size, n));
            assertRangeIsClear(from, to);

            bs.setRange(from, to);
            for (long i = from - 2; i >= 0; i -= 2) {
                bs.set(i);
            }
            for (long i = to + 1; i < bs.logicalSize(); i += 2) {
                bs.set(i);
            }
            long cardinality = bs.cardinality();
            assertEquals(from, bs.clearPreviousNContinuousSetBits(to - 1, n));
            assertEquals(cardinality - n, bs.cardinality());
        }
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testIoobeGetNegative(ReusableBitSet bs) {
        init(bs);
        assertThrows(IndexOutOfBoundsException.class, () -> bs.get(-1));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testIoobeGetOverCapacity(ReusableBitSet bs) {
        init(bs);
        assertThrows(IndexOutOfBoundsException.class, () -> bs.get(bs.logicalSize()));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testIoobeSetNegative(ReusableBitSet bs) {
        init(bs);
        assertThrows(IndexOutOfBoundsException.class, () -> bs.set(-1));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testIoobeSetOverCapacity(ReusableBitSet bs) {
        init(bs);
        assertThrows(IndexOutOfBoundsException.class, () -> bs.set(bs.logicalSize()));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testIoobeSetIfClearNegative(ReusableBitSet bs) {
        init(bs);
        assertThrows(IndexOutOfBoundsException.class, () -> bs.setIfClear(-1));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testIoobeSetIfClearOverCapacity(ReusableBitSet bs) {
        init(bs);
        assertThrows(IndexOutOfBoundsException.class, () -> bs.setIfClear(bs.logicalSize()));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testIoobeClearIfSetNegative(ReusableBitSet bs) {
        init(bs);
        assertThrows(IndexOutOfBoundsException.class, () -> bs.clearIfSet(-1));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testIoobeClearIfSetOverCapacity(ReusableBitSet bs) {
        init(bs);
        assertThrows(IndexOutOfBoundsException.class, () -> bs.clearIfSet(bs.logicalSize()));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testIoobeFlipNegative(ReusableBitSet bs) {
        init(bs);
        assertThrows(IndexOutOfBoundsException.class, () -> bs.flip(-1));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testIoobeFlipOverCapacity(ReusableBitSet bs) {
        init(bs);
        assertThrows(IndexOutOfBoundsException.class, () -> bs.flip(bs.logicalSize()));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testIoobeNextSetBit(ReusableBitSet bs) {
        init(bs);
        assertThrows(IndexOutOfBoundsException.class, () -> bs.nextSetBit(-1));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testIoobeNextClearBit(ReusableBitSet bs) {
        init(bs);
        assertThrows(IndexOutOfBoundsException.class, () -> bs.nextClearBit(-1));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testIoobePreviousSetBit(ReusableBitSet bs) {
        init(bs);
        assertThrows(IndexOutOfBoundsException.class, () -> bs.previousSetBit(-2));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testIoobePreviousClearBit(ReusableBitSet bs) {
        init(bs);
        assertThrows(IndexOutOfBoundsException.class, () -> bs.previousClearBit(-2));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testIoobeClearNextSetBit(ReusableBitSet bs) {
        init(bs);
        assertThrows(IndexOutOfBoundsException.class, () -> bs.clearNextSetBit(-1));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testIoobeClearNextNContinuousSetBits(ReusableBitSet bs) {
        init(bs);
        assertThrows(IndexOutOfBoundsException.class, () -> bs.clearNextNContinuousSetBits(-1, 2));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testIoobeSetNextClearBit(ReusableBitSet bs) {
        init(bs);
        assertThrows(IndexOutOfBoundsException.class, () -> bs.setNextClearBit(-1));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testIoobeSetNextNContinuousClearBits(ReusableBitSet bs) {
        init(bs);
        assertThrows(IndexOutOfBoundsException.class, () -> bs.setNextNContinuousClearBits(-1, 2));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testIoobeClearPreviousSetBit(ReusableBitSet bs) {
        init(bs);
        assertThrows(IndexOutOfBoundsException.class, () -> bs.clearPreviousSetBit(-2));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testIoobeClearPreviousNContinuousSetBit(ReusableBitSet bs) {
        init(bs);
        assertThrows(IndexOutOfBoundsException.class, () -> bs.clearPreviousNContinuousSetBits(-2, 2));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testIoobeSetPreviousClearBit(ReusableBitSet bs) {
        init(bs);
        assertThrows(IndexOutOfBoundsException.class, () -> bs.setPreviousClearBit(-2));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testIoobeSetPreviousNContinuousClearBit(ReusableBitSet bs) {
        init(bs);
        assertThrows(IndexOutOfBoundsException.class, () -> bs.setPreviousNContinuousClearBits(-2, 2));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testIoobeSetRangeFromNegative(ReusableBitSet bs) {
        init(bs);
        assertThrows(IndexOutOfBoundsException.class, () -> bs.setRange(-1, 0));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testIoobeSetRangeFromOverTo(ReusableBitSet bs) {
        init(bs);
        assertThrows(IndexOutOfBoundsException.class, () -> bs.setRange(1, 0));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testIoobeSetRangeToOverCapacity(ReusableBitSet bs) {
        init(bs);
        assertThrows(IndexOutOfBoundsException.class, () -> bs.setRange(0, bs.logicalSize() + 1));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testIoobeClearRangeFromNegative(ReusableBitSet bs) {
        init(bs);
        assertThrows(IndexOutOfBoundsException.class, () -> bs.clearRange(-1, 0));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testIoobeClearRangeFromOverTo(ReusableBitSet bs) {
        init(bs);
        assertThrows(IndexOutOfBoundsException.class, () -> bs.clearRange(1, 0));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testIoobeClearRangeToOverCapacity(ReusableBitSet bs) {
        init(bs);
        assertThrows(IndexOutOfBoundsException.class, () -> bs.clearRange(0, bs.logicalSize() + 1));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testIoobeFlipRangeFromNegative(ReusableBitSet bs) {
        init(bs);
        assertThrows(IndexOutOfBoundsException.class, () -> bs.flipRange(-1, 0));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testIoobeFlipRangeFromOverTo(ReusableBitSet bs) {
        init(bs);
        assertThrows(IndexOutOfBoundsException.class, () -> bs.flipRange(1, 0));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testIoobeFlipRangeToOverCapacity(ReusableBitSet bs) {
        init(bs);
        assertThrows(IndexOutOfBoundsException.class, () -> bs.flipRange(0, bs.logicalSize() + 1));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testIaeClearNextNContinuousSetBits(ReusableBitSet bs) {
        init(bs);
        assertThrows(IllegalArgumentException.class, () -> bs.clearNextNContinuousSetBits(0, 0));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testIaeSetNextNContinuousClearBits(ReusableBitSet bs) {
        init(bs);
        assertThrows(IllegalArgumentException.class, () -> bs.setNextNContinuousClearBits(0, 0));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testIaeClearPreviousNContinuousSetBits(ReusableBitSet bs) {
        init(bs);
        assertThrows(IllegalArgumentException.class, () -> bs.clearPreviousNContinuousSetBits(bs.logicalSize(), 0));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testIaeSetPreviousNContinuousClearBits(ReusableBitSet bs) {
        init(bs);
        assertThrows(IllegalArgumentException.class, () -> bs.setPreviousNContinuousClearBits(bs.logicalSize(), 0));
    }
}
