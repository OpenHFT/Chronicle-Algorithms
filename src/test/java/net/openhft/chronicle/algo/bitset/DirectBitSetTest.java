/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo.bitset;

import net.openhft.chronicle.algo.MemoryUnit;
import net.openhft.chronicle.algo.bytes.Access;
import net.openhft.chronicle.bytes.BytesStore;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.ByteBuffer;
import java.util.stream.Stream;

import static net.openhft.chronicle.algo.bytes.Access.checkedByteBufferAccess;
import static org.junit.jupiter.api.Assertions.*;

public class DirectBitSetTest {

    private static final int LOGICAL_SIZE = 256;
    private static final int[] INDICES = {0, 50, 100, 127, 128, 255};

    @SuppressWarnings("unchecked")
    static Stream<Arguments> bitSets() {
        int capacityInBytes = (int) MemoryUnit.BITS.toBytes(LOGICAL_SIZE);
        return Stream.of(
                Arguments.of("concurrent bytesStore #1",
                        new ReusableBitSet(
                                new ConcurrentFlatBitSetFrame(LOGICAL_SIZE),
                                (Access) Access.checkedBytesStoreAccess(),
                                BytesStore.wrap(ByteBuffer.allocateDirect(capacityInBytes)),
                                0)),
                Arguments.of("single-threaded heap buffer",
                        new ReusableBitSet(
                                new SingleThreadedFlatBitSetFrame(LOGICAL_SIZE),
                                checkedByteBufferAccess(),
                                ByteBuffer.allocate(capacityInBytes),
                                0)),
                Arguments.of("concurrent bytesStore #2",
                        new ReusableBitSet(
                                new ConcurrentFlatBitSetFrame(LOGICAL_SIZE),
                                (Access) Access.checkedBytesStoreAccess(),
                                BytesStore.wrap(ByteBuffer.allocateDirect(capacityInBytes)),
                                0)),
                Arguments.of("single-threaded direct buffer",
                        new ReusableBitSet(
                                new SingleThreadedFlatBitSetFrame(LOGICAL_SIZE),
                                checkedByteBufferAccess(),
                                ByteBuffer.allocateDirect(capacityInBytes),
                                0))
        );
    }

    private static void setIndices(ReusableBitSet bs) {
        bs.clearAll();
        for (int i : INDICES) {
            bs.set(i);
        }
    }

    private static void setIndicesComplement(ReusableBitSet bs) {
        setIndices(bs);
        bs.flipRange(0, bs.logicalSize());
    }

    private static void assertRangeIsClear(ReusableBitSet bs, long from, long to) {
        for (long i = from; i < to; i++) {
            assertFalse(bs.get(i), "bit clear at " + i);
        }
    }

    private static void assertRangeIsClear(ReusableBitSet bs, String message, long from, long to) {
        for (long i = from; i < to; i++) {
            assertFalse(bs.get(i), message + ", bit: " + i);
        }
    }

    private static void assertRangeIsSet(ReusableBitSet bs, long from, long to) {
        for (long i = from; i < to; i++) {
            assertTrue(bs.get(i), "bit set at " + i);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testGetSetClearAndCardinality(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        bs.clearAll();
        assertEquals(0L, bs.cardinality(), "cardinality after clearAll");
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
            assertEquals(c, bs.cardinality(), "cardinality after set i=" + i);
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
            assertTrue(bs.setIfClear(i), "setIfClear true at index " + i);
            assertFalse(bs.setIfClear(i), "setIfClear false when already set at index " + i);
        }
        for (int i : INDICES) {
            assertTrue(bs.clearIfSet(i), "clearIfSet true at index " + i);
            assertFalse(bs.clearIfSet(i), "clearIfSet false when already clear at index " + i);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testFlip(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        bs.clearAll();
        for (int i : INDICES) {
            assertFalse(bs.get(i), "At index " + i);
            bs.flip(i);
            assertTrue(bs.get(i), "At index " + i);
            bs.flip(i);
            assertFalse(bs.get(i), "At index " + i);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testNextSetBit(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        setIndices(bs);
        int order = 0;
        for (long i = bs.nextSetBit(0L); i >= 0; i = bs.nextSetBit(i + 1)) {
            assertEquals(INDICES[order], i, "nextSetBit order=" + order);
            order++;
        }
        assertEquals(-1L, bs.nextSetBit(bs.logicalSize()), "nextSetBit at logicalSize");

        bs.clearAll();
        assertEquals(-1L, bs.nextSetBit(0L), "nextSetBit empty");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testSetBitsIteration(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        setIndices(bs);

        BitSet.Bits bits = bs.setBits().reset();
        long i;
        int order = 0;
        while ((i = bits.next()) >= 0) {
            assertEquals(INDICES[order], i, "setBits order=" + order);
            order++;
        }
        assertEquals(-1L, bits.next(), "setBits end-of-iteration");

        // reset support
        bits.reset();
        order = 0;
        while ((i = bits.next()) >= 0) {
            assertEquals(INDICES[order], i, "setBits reset order=" + order);
            order++;
        }
        assertEquals(-1L, bits.next(), "setBits reset end-of-iteration");

        bs.clearAll();
        assertEquals(-1L, bits.reset().next(), "setBits empty after clearAll");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testClearNextSetBit(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        setIndices(bs);
        long cardinality = bs.cardinality();
        int order = 0;
        for (long i = bs.clearNextSetBit(0L); i >= 0;
             i = bs.clearNextSetBit(i + 1)) {
            assertEquals(INDICES[order], i, "clearNextSetBit order=" + order);
            assertFalse(bs.get(i), "bit cleared at " + i);
            order++;
            cardinality--;
            assertEquals(cardinality, bs.cardinality(), "cardinality after clearing " + i);
        }
        assertEquals(-1L, bs.clearNextSetBit(bs.logicalSize()), "clearNextSetBit at logicalSize");
        assertEquals(0L, bs.cardinality(), "cardinality after clearing all");
        assertEquals(-1L, bs.clearNextSetBit(0L), "clearNextSetBit empty");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testClearNext1SetBit(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        setIndices(bs);
        long cardinality = bs.cardinality();
        int order = 0;
        for (long i = bs.clearNextNContinuousSetBits(0L, 1); i >= 0;
             i = bs.clearNextNContinuousSetBits(i + 1, 1)) {
            assertEquals(INDICES[order], i, "clearNextNContinuousSetBits(1) order=" + order);
            assertFalse(bs.get(i), "bit cleared at " + i);
            order++;
            cardinality--;
            assertEquals(cardinality, bs.cardinality(), "cardinality after clearing " + i);
        }
        assertEquals(-1L, bs.clearNextNContinuousSetBits(bs.logicalSize(), 1), "clearNextNContinuousSetBits at end");
        assertEquals(0L, bs.cardinality(), "cardinality after clearing all");
        assertEquals(-1L, bs.clearNextNContinuousSetBits(0L, 1), "clearNextNContinuousSetBits empty");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testNextClearBit(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        setIndicesComplement(bs);
        int order = 0;
        for (long i = bs.nextClearBit(0L); i >= 0; i = bs.nextClearBit(i + 1)) {
            assertEquals(INDICES[order], i, "nextClearBit order=" + order);
            order++;
        }
        assertEquals(-1L, bs.nextClearBit(bs.logicalSize()), "nextClearBit at logicalSize");

        bs.setAll();
        assertEquals(-1L, bs.nextClearBit(0L), "nextClearBit when all set");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testSetNextClearBit(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        setIndicesComplement(bs);
        long cardinality = bs.cardinality();
        int order = 0;
        for (long i = bs.setNextClearBit(0L); i >= 0;
             i = bs.setNextClearBit(i + 1)) {
            assertEquals(INDICES[order], i, "setNextClearBit order=" + order);
            assertTrue(bs.get(i), "bit set at " + i);
            order++;
            cardinality++;
            assertEquals(cardinality, bs.cardinality(), "cardinality after setting " + i);
        }
        assertEquals(-1L, bs.setNextClearBit(bs.logicalSize()), "setNextClearBit at logicalSize");
        assertEquals(bs.logicalSize(), bs.cardinality(), "cardinality after setting all");
        assertEquals(-1L, bs.setNextClearBit(0L), "setNextClearBit when all set");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testSetNext1ClearBit(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        setIndicesComplement(bs);
        long cardinality = bs.cardinality();
        int order = 0;
        for (long i = bs.setNextNContinuousClearBits(0L, 1); i >= 0;
             i = bs.setNextNContinuousClearBits(i + 1, 1)) {
            assertEquals(INDICES[order], i, "setNextNContinuousClearBits(1) order=" + order);
            assertTrue(bs.get(i), "bit set at " + i);
            order++;
            cardinality++;
            assertEquals(cardinality, bs.cardinality(), "cardinality after setting " + i);
        }
        assertEquals(-1L, bs.setNextNContinuousClearBits(bs.logicalSize(), 1), "setNextNContinuousClearBits at end");
        assertEquals(bs.logicalSize(), bs.cardinality(), "cardinality after setting all");
        assertEquals(-1L, bs.setNextNContinuousClearBits(0L, 1), "setNextNContinuousClearBits when all set");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testPreviousSetBit(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        setIndices(bs);
        int order = INDICES.length;
        for (long i = bs.logicalSize(); (i = bs.previousSetBit(i - 1)) >= 0; ) {
            order--;
            assertEquals(INDICES[order], i, "previousSetBit order=" + order);
        }
        assertEquals(-1L, bs.previousSetBit(-1), "previousSetBit from negative");

        bs.clearAll();
        assertEquals(-1L, bs.previousSetBit(bs.logicalSize()), "previousSetBit empty");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testClearPreviousSetBit(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        setIndices(bs);
        long cardinality = bs.cardinality();
        int order = INDICES.length;
        for (long i = bs.logicalSize(); (i = bs.clearPreviousSetBit(i - 1)) >= 0; ) {
            order--;
            cardinality--;
            assertEquals(INDICES[order], i, "clearPreviousSetBit order=" + order);
            assertFalse(bs.get(i), "bit cleared at " + i);
            assertEquals(cardinality, bs.cardinality(), "cardinality after clearing " + i);
        }
        assertEquals(-1L, bs.clearPreviousSetBit(-1), "clearPreviousSetBit from negative");
        assertEquals(0L, bs.cardinality(), "cardinality after clearing all");
        assertEquals(-1L, bs.clearPreviousSetBit(bs.logicalSize()), "clearPreviousSetBit empty");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testClearPrevious1SetBit(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        setIndices(bs);
        long cardinality = bs.cardinality();
        int order = INDICES.length;
        for (long i = bs.logicalSize();
             (i = bs.clearPreviousNContinuousSetBits(i - 1, 1)) >= 0; ) {
            order--;
            cardinality--;
            assertEquals(INDICES[order], i, "clearPreviousNContinuousSetBits(1) order=" + order);
            assertFalse(bs.get(i), "bit cleared at " + i);
            assertEquals(cardinality, bs.cardinality(), "cardinality after clearing " + i);
        }
        assertEquals(-1L, bs.clearPreviousNContinuousSetBits(-1, 1), "clearPreviousNContinuousSetBits from negative");
        assertEquals(0L, bs.cardinality(), "cardinality after clearing all");
        assertEquals(-1L, bs.clearPreviousNContinuousSetBits(bs.logicalSize(), 1), "clearPreviousNContinuousSetBits empty");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testPreviousClearBit(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        setIndicesComplement(bs);
        int order = INDICES.length;
        for (long i = bs.logicalSize(); (i = bs.previousClearBit(i - 1)) >= 0; ) {
            order--;
            assertEquals(INDICES[order], i, "previousClearBit order=" + order);
        }
        assertEquals(-1L, bs.previousClearBit(-1), "previousClearBit from negative");

        bs.setAll();
        assertEquals(-1L, bs.previousClearBit(bs.logicalSize()), "previousClearBit when all set");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testSetPreviousClearBit(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        setIndicesComplement(bs);
        long cardinality = bs.cardinality();
        int order = INDICES.length;
        for (long i = bs.logicalSize(); (i = bs.setPreviousClearBit(i - 1)) >= 0; ) {
            order--;
            cardinality++;
            assertEquals(INDICES[order], i, "setPreviousClearBit order=" + order);
            assertTrue(bs.get(i), "bit set at " + i);
            assertEquals(cardinality, bs.cardinality(), "cardinality after setting " + i);
        }
        assertEquals(-1L, bs.setPreviousClearBit(-1), "setPreviousClearBit from negative");
        assertEquals(bs.logicalSize(), bs.cardinality(), "cardinality after setting all");
        assertEquals(-1L, bs.setPreviousClearBit(bs.logicalSize()), "setPreviousClearBit when all set");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testSetPrevious1ClearBit(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        setIndicesComplement(bs);
        long cardinality = bs.cardinality();
        int order = INDICES.length;
        for (long i = bs.logicalSize();
             (i = bs.setPreviousNContinuousClearBits(i - 1, 1)) >= 0; ) {
            order--;
            cardinality++;
            assertEquals(INDICES[order], i, "setPreviousNContinuousClearBits(1) order=" + order);
            assertTrue(bs.get(i), "bit set at " + i);
            assertEquals(cardinality, bs.cardinality(), "cardinality after setting " + i);
        }
        assertEquals(-1L, bs.setPreviousNContinuousClearBits(-1, 1), "setPreviousNContinuousClearBits from negative");
        assertEquals(bs.logicalSize(), bs.cardinality(), "cardinality after setting all");
        assertEquals(-1L, bs.setPreviousNContinuousClearBits(bs.logicalSize(), 1), "setPreviousNContinuousClearBits when all set");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testSetAll(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        bs.clearAll();
        bs.setAll();
        assertEquals(bs.logicalSize(), bs.cardinality(), "cardinality after setAll");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testRangeOpsWithinLongCase(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        boolean singleThreaded = bs.frame instanceof SingleThreadedFlatBitSetFrame;
        bs.clearAll();
        if (singleThreaded) {
            assertTrue(bs.isRangeClear(0, 0), "isRangeClear empty range at start");
            assertTrue(bs.isRangeClear(63, 63), "isRangeClear empty range at 63");
            assertTrue(bs.isRangeSet(0, 0), "isRangeSet empty range at start");
            assertTrue(bs.isRangeSet(63, 63), "isRangeSet empty range at 63");
        }
        bs.flipRange(0, 0);
        assertFalse(bs.get(0), "flipRange(0,0) no-op");
        assertEquals(0L, bs.cardinality(), "cardinality after flipRange(0,0)");
        bs.flipRange(0, 1);
        assertTrue(bs.get(0), "bit 0 set after flipRange(0,1)");
        assertEquals(1L, bs.cardinality(), "cardinality after flipRange(0,1)");
        if (singleThreaded) {
            assertTrue(bs.isRangeSet(0, 1), "isRangeSet(0,1) after flip");
            assertFalse(bs.isRangeSet(0, 2), "isRangeSet(0,2) after flip");
            assertFalse(bs.isRangeClear(0, 1), "isRangeClear(0,1) after flip");
        }
        bs.clearRange(0, 0);
        assertTrue(bs.get(0), "clearRange(0,0) no-op");
        assertEquals(1L, bs.cardinality(), "cardinality after clearRange(0,0)");
        bs.clearRange(0, 1);
        assertFalse(bs.get(0), "bit 0 clear after clearRange(0,1)");
        assertEquals(0L, bs.cardinality(), "cardinality after clearRange(0,1)");

        bs.setRange(0, 0);
        assertFalse(bs.get(0), "setRange(0,0) no-op");
        assertEquals(0L, bs.cardinality(), "cardinality after setRange(0,0)");
        bs.setRange(0, 1);
        assertTrue(bs.get(0), "bit 0 set after setRange(0,1)");
        assertEquals(1L, bs.cardinality(), "cardinality after setRange(0,1)");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testRangeOpsCrossLongCase(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        boolean singleThreaded = bs.frame instanceof SingleThreadedFlatBitSetFrame;
        bs.clearAll();

        bs.flipRange(63, 64);
        assertTrue(bs.get(63), "bit 63 set after flipRange(63,64)");
        assertFalse(bs.get(64), "bit 64 unchanged after flipRange(63,64)");
        assertEquals(1L, bs.cardinality(), "cardinality after flipRange(63,64)");
        if (singleThreaded) {
            assertFalse(bs.isRangeSet(63, 65), "isRangeSet spans mixed values");
            assertFalse(bs.isRangeClear(63, 65), "isRangeClear spans mixed values");
        }
        bs.flipRange(63, 65);
        assertFalse(bs.get(63), "bit 63 toggled off after flipRange(63,65)");
        assertTrue(bs.get(64), "bit 64 toggled on after flipRange(63,65)");
        assertEquals(1L, bs.cardinality(), "cardinality after flipRange(63,65)");
        if (singleThreaded) {
            assertFalse(bs.isRangeSet(63, 65), "isRangeSet spans mixed values");
            assertFalse(bs.isRangeClear(63, 65), "isRangeClear spans mixed values");
        }
        bs.clear(64);
        bs.setRange(63, 64);
        assertTrue(bs.get(63), "bit 63 set after setRange(63,64)");
        assertFalse(bs.get(64), "bit 64 clear after clear(64)");
        assertEquals(1L, bs.cardinality(), "cardinality after setRange(63,64)");

        bs.set(64);
        bs.clearRange(63, 64);
        assertFalse(bs.get(63), "bit 63 cleared after clearRange(63,64)");
        assertTrue(bs.get(64), "bit 64 set after set(64)");
        assertEquals(1L, bs.cardinality(), "cardinality after clearRange(63,64)");

        bs.clear(64);
        bs.setRange(63, 65);
        assertTrue(bs.get(63), "bit 63 set after setRange(63,65)");
        assertTrue(bs.get(64), "bit 64 set after setRange(63,65)");
        assertEquals(2L, bs.cardinality(), "cardinality after setRange(63,65)");
        if (singleThreaded) {
            assertTrue(bs.isRangeSet(63, 65), "isRangeSet after setRange");
            assertFalse(bs.isRangeClear(63, 65), "isRangeClear after setRange");
        }
        bs.clearRange(63, 65);
        assertFalse(bs.get(63), "bit 63 clear after clearRange");
        assertFalse(bs.get(64), "bit 64 clear after clearRange");
        assertEquals(0L, bs.cardinality(), "cardinality after clearRange(63,65)");
        if (singleThreaded) {
            assertFalse(bs.isRangeSet(63, 65), "isRangeSet after clearRange");
            assertTrue(bs.isRangeClear(63, 65), "isRangeClear after clearRange");
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testRangeOpsSpanLongCase(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        boolean singleThreaded = bs.frame instanceof SingleThreadedFlatBitSetFrame;
        bs.clearAll();
        if (singleThreaded) {
            assertTrue(bs.isRangeClear(0, bs.logicalSize()), "isRangeClear after clearAll");
            assertFalse(bs.isRangeSet(0, bs.logicalSize()), "isRangeSet after clearAll");
        }
        bs.setRange(0, bs.logicalSize());
        assertEquals(bs.logicalSize(), bs.cardinality(), "cardinality after setRange full");
        if (singleThreaded) {
            assertFalse(bs.isRangeClear(0, bs.logicalSize()), "isRangeClear after setRange full");
            assertTrue(bs.isRangeSet(0, bs.logicalSize()), "isRangeSet after setRange full");
        }
        bs.clearRange(0, bs.logicalSize());
        assertEquals(0L, bs.cardinality(), "cardinality after clearRange full");

        bs.flipRange(0, bs.logicalSize());
        assertEquals(bs.logicalSize(), bs.cardinality(), "cardinality after flipRange full");
    }

    private static String m(ReusableBitSet bs, int n) {
        return "N: " + n + ", " + bs.getClass().getSimpleName();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testSetNextNContinuousClearBitsWithinLongCase(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        long size = (bs.logicalSize() + 63) / 64 * 64;
        for (int n = 1; n <= 64; n *= 2) {
            bs.clearAll();
            for (int i = 0; i < size / n; i++) {
                assertRangeIsClear(bs, (long) i * n, (long) i * n + n);
                assertEquals((long) i * n,
                        bs.setNextNContinuousClearBits(0L, n),
                        m(bs, n) + ", i=" + i);
                assertRangeIsSet(bs, (long) i * n, (long) i * n + n);
                assertEquals((long) i * n + n, bs.cardinality(), m(bs, n) + ", i=" + i + " cardinality");
            }
        }
        for (int n = 2; n <= 64; n *= 2) {
            bs.setAll();
            bs.clearRange(size - n, size);
            assertEquals(size - n, bs.setNextNContinuousClearBits(0L, n), m(bs, n));
            assertRangeIsSet(bs, size - n, size);

            long offset = (64 - n) / 2;
            long from = size - n - offset;
            long to = size - offset;
            bs.clearRange(from, to);
            assertEquals(from, bs.setNextNContinuousClearBits(from, n), m(bs, n));
            assertRangeIsSet(bs, from, to);

            bs.clearRange(from, to);
            for (long i = from - 2; i >= 0; i -= 2) {
                bs.clear(i);
            }
            long cardinality = bs.cardinality();
            assertEquals(from, bs.setNextNContinuousClearBits(0L, n), m(bs, n));
            assertEquals(cardinality + n, bs.cardinality(), m(bs, n) + " cardinality");
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testSetNextNContinuousClearBitsCrossLongCase(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        if (bs.frame instanceof ConcurrentFlatBitSetFrame)
            return;
        long size = bs.logicalSize();
        for (int n : new int[]{3, 7, 13, 31, 33, 63, 65, 100, 127, 128, 129, 254, 255}) {
            bs.clearAll();
            for (int i = 0; i < size / n; i++) {
                assertRangeIsClear(bs, (long) i * n, (long) i * n + n);
                assertEquals((long) i * n,
                        bs.setNextNContinuousClearBits(0L, n),
                        m(bs, n) + ", i=" + i);
                assertRangeIsSet(bs, (long) i * n, (long) i * n + n);
                assertEquals((long) i * n + n, bs.cardinality(), m(bs, n) + ", i=" + i + " cardinality");
            }
        }
        long lastBound = size - (size % 64 == 0 ? 64 : size % 64);
        for (int n : new int[]{2, 3, 7, 13, 31, 33, 63, 64, 65, 100, 127, 128, 129}) {
            bs.setAll();
            long from = n <= 64 ? lastBound - (n / 2) : 30;
            long to = from + n;
            bs.clearRange(from, to);
            assertEquals(from, bs.setNextNContinuousClearBits(0L, n), "n=" + n);
            assertRangeIsSet(bs, from, to);

            bs.clearRange(from, to);
            for (long i = from - 2; i >= 0; i -= 2) {
                bs.clear(i);
            }
            for (long i = to + 1; i < bs.logicalSize(); i += 2) {
                bs.clear(i);
            }
            long cardinality = bs.cardinality();
            assertEquals(from, bs.setNextNContinuousClearBits(from, n), m(bs, n));
            assertEquals(cardinality + n, bs.cardinality(), m(bs, n) + " cardinality");
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testClearNextNContinuousSetBitsWithinLongCase(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        long size = (bs.logicalSize() + 63) / 64 * 64;
        for (int n = 1; n <= 64; n *= 2) {
            bs.setAll();
            long cardinality = bs.cardinality();
            for (int i = 0; i < size / n; i++) {
                assertRangeIsSet(bs, (long) i * n, (long) i * n + n);
                assertEquals((long) i * n,
                        bs.clearNextNContinuousSetBits(0L, n),
                        m(bs, n) + ", i=" + i);
                assertRangeIsClear(bs, (long) i * n, (long) i * n + n);
                assertEquals(cardinality - ((long) i * n + n), bs.cardinality(), m(bs, n) + ", i=" + i);
            }
        }
        for (int n = 2; n <= 64; n *= 2) {
            bs.clearAll();
            bs.setRange(size - n, size);
            assertEquals(size - n, bs.clearNextNContinuousSetBits(0L, n), m(bs, n));
            assertRangeIsClear(bs, size - n, size);

            long offset = (64 - n) / 2;
            long from = size - n - offset;
            long to = size - offset;
            bs.setRange(from, to);
            assertEquals(from, bs.clearNextNContinuousSetBits(from, n), m(bs, n));
            assertRangeIsClear(bs, from, to);

            bs.setRange(from, to);
            for (long i = from - 2; i >= 0; i -= 2) {
                bs.set(i);
            }
            long cardinality = bs.cardinality();
            assertEquals(from, bs.clearNextNContinuousSetBits(0L, n), m(bs, n));
            assertEquals(cardinality - n, bs.cardinality(), m(bs, n) + " cardinality");
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testClearNextNContinuousSetBitsCrossLongCase(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        if (bs.frame instanceof ConcurrentFlatBitSetFrame)
            return;
        long size = bs.logicalSize();
        for (int n : new int[]{3, 7, 13, 31, 33, 63}) {
            bs.setAll();
            long cardinality = bs.cardinality();
            for (int i = 0; i < size / n; i++) {
                assertRangeIsSet(bs, (long) i * n, (long) i * n + n);
                assertEquals((long) i * n,
                        bs.clearNextNContinuousSetBits(0L, n),
                        m(bs, n) + ", i=" + i);
                assertRangeIsClear(bs, (long) i * n, (long) i * n + n);
                assertEquals(cardinality -= n, bs.cardinality(), m(bs, n) + ", i=" + i);
            }
        }
        long lastBound = size - (size % 64 == 0 ? 64 : size % 64);
        for (int n : new int[]{2, 3, 7, 13, 31, 33, 63, 64}) {
            bs.clearAll();
            long from = lastBound - (n / 2);
            long to = from + n;
            bs.setRange(from, to);
            assertEquals(from, bs.clearNextNContinuousSetBits(0L, n), m(bs, n));
            assertRangeIsClear(bs, from, to);

            bs.setRange(from, to);
            for (long i = from - 2; i >= 0; i -= 2) {
                bs.set(i);
            }
            for (long i = to + 1; i < bs.logicalSize(); i += 2) {
                bs.set(i);
            }
            long cardinality = bs.cardinality();
            assertEquals(from, bs.clearNextNContinuousSetBits(from, n), m(bs, n));
            assertEquals(cardinality - n, bs.cardinality(), m(bs, n) + " cardinality");
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testSetPreviousNContinuousClearBitsWithinLongCase(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        long size = (bs.logicalSize() + 63) / 64 * 64;
        for (int n = 1; n <= 64; n *= 2) {
            bs.clearAll();
            long cardinality = 0;
            for (long i = size / n - 1; i >= 0; i--) {
                assertRangeIsClear(bs, i * n, i * n + n);
                assertEquals(i * n,
                        bs.setPreviousNContinuousClearBits(size, n),
                        m(bs, n) + ", i=" + i);
                assertRangeIsSet(bs, i * n, i * n + n);
                assertEquals(cardinality += n, bs.cardinality(), m(bs, n) + ", i=" + i + " cardinality");
            }
        }
        for (int n = 2; n <= 64; n *= 2) {
            bs.setAll();
            bs.clearRange(0, n);
            assertEquals(0L, bs.setPreviousNContinuousClearBits(bs.logicalSize(), n), m(bs, n));
            assertRangeIsSet(bs, 0, n);

            long from = (64 - n) / 2;
            long to = from + n;
            bs.clearRange(from, to);
            assertEquals(from, bs.setPreviousNContinuousClearBits(to - 1, n), m(bs, n));
            assertRangeIsSet(bs, from, to);

            bs.clearRange(from, to);
            for (long i = to + 1; i < bs.logicalSize(); i += 2) {
                bs.clear(i);
            }
            long cardinality = bs.cardinality();
            assertEquals(from, bs.setPreviousNContinuousClearBits(bs.logicalSize(), n), m(bs, n));
            assertEquals(cardinality + n, bs.cardinality(), m(bs, n) + " cardinality");
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testSetPreviousNContinuousClearBitsCrossLongCase(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        if (bs.frame instanceof ConcurrentFlatBitSetFrame)
            return;
        long size = bs.logicalSize();
        for (int n : new int[]{3, 7, 13, 31, 33, 63}) {
            bs.clearAll();
            long cardinality = 0;
            for (long from = size - n; from >= 0; from -= n) {
                assertRangeIsClear(bs, from, from + n);
                assertEquals(from,
                        bs.setPreviousNContinuousClearBits(size, n),
                        m(bs, n) + ", from=" + from);
                assertRangeIsSet(bs, from, from + n);
                assertEquals(cardinality += n, bs.cardinality(), m(bs, n) + " cardinality");
            }
        }
        for (int n : new int[]{2, 3, 7, 13, 31, 33, 63, 64}) {
            bs.setAll();
            long from = 64 - (n / 2);
            long to = from + n;
            bs.clearRange(from, to);
            assertEquals(from, bs.setPreviousNContinuousClearBits(size, n), m(bs, n));
            assertRangeIsSet(bs, from, to);

            bs.clearRange(from, to);
            for (long i = from - 2; i >= 0; i -= 2) {
                bs.clear(i);
            }
            for (long i = to + 1; i < bs.logicalSize(); i += 2) {
                bs.clear(i);
            }
            long cardinality = bs.cardinality();
            assertEquals(from, bs.setPreviousNContinuousClearBits(to - 1, n), m(bs, n));
            assertEquals(cardinality + n, bs.cardinality(), m(bs, n) + " cardinality");
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testClearPreviousNContinuousSetBitsWithinLongCase(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        long size = (bs.logicalSize() + 63) / 64 * 64;
        for (int n = 1; n <= 64; n *= 2) {
            bs.setAll();
            long cardinality = bs.cardinality();
            for (long i = size / n - 1; i >= 0; i--) {
                assertRangeIsSet(bs, i * n, i * n + n);
                assertEquals(i * n,
                        bs.clearPreviousNContinuousSetBits(size, n),
                        m(bs, n) + ", i=" + i);
                assertRangeIsClear(bs, m(bs, n), i * n, i * n + n);
                assertEquals(cardinality -= n, bs.cardinality(), m(bs, n) + " cardinality");
            }
        }
        for (int n = 2; n <= 64; n *= 2) {
            bs.clearAll();
            bs.setRange(0, n);
            assertEquals(0L, bs.clearPreviousNContinuousSetBits(bs.logicalSize(), n), m(bs, n));
            assertRangeIsClear(bs, 0, n);

            long from = (64 - n) / 2;
            long to = from + n;
            bs.setRange(from, to);
            assertEquals(from, bs.clearPreviousNContinuousSetBits(to - 1, n), m(bs, n));
            assertRangeIsClear(bs, from, to);

            bs.setRange(from, to);
            for (long i = to + 1; i < bs.logicalSize(); i += 2) {
                bs.set(i);
            }
            long cardinality = bs.cardinality();
            assertEquals(from, bs.clearPreviousNContinuousSetBits(bs.logicalSize(), n), m(bs, n));
            assertEquals(cardinality - n, bs.cardinality(), m(bs, n) + " cardinality");
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testClearPreviousNContinuousSetBitsCrossLongCase(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        if (bs.frame instanceof ConcurrentFlatBitSetFrame)
            return;
        long size = bs.logicalSize();
        for (int n : new int[]{3, 7, 13, 31, 33, 63}) {
            bs.setAll();
            long cardinality = bs.cardinality();
            for (long from = size - n; from >= 0; from -= n) {
                assertRangeIsSet(bs, from, from + n);
                assertEquals(from,
                        bs.clearPreviousNContinuousSetBits(size, n),
                        m(bs, n) + ", from=" + from);
                assertRangeIsClear(bs, from, from + n);
                assertEquals(cardinality -= n, bs.cardinality(), m(bs, n) + " cardinality");
            }
        }
        for (int n : new int[]{2, 3, 7, 13, 31, 33, 63, 64}) {
            bs.clearAll();
            long from = 64 - (n / 2);
            long to = from + n;
            bs.setRange(from, to);
            assertEquals(from, bs.clearPreviousNContinuousSetBits(size, n), m(bs, n));
            assertRangeIsClear(bs, from, to);

            bs.setRange(from, to);
            for (long i = from - 2; i >= 0; i -= 2) {
                bs.set(i);
            }
            for (long i = to + 1; i < bs.logicalSize(); i += 2) {
                bs.set(i);
            }
            long cardinality = bs.cardinality();
            assertEquals(from, bs.clearPreviousNContinuousSetBits(to - 1, n), m(bs, n));
            assertEquals(cardinality - n, bs.cardinality(), m(bs, n) + " cardinality");
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testIoobeGetNegative(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        assertThrows(IndexOutOfBoundsException.class, () -> bs.get(-1), "get(-1) out of bounds");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testIoobeGetOverCapacity(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        assertThrows(IndexOutOfBoundsException.class, () -> bs.get(bs.logicalSize()), "get(logicalSize) out of bounds");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testIoobeSetNegative(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        assertThrows(IndexOutOfBoundsException.class, () -> bs.set(-1), "set(-1) out of bounds");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testIoobeSetOverCapacity(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        assertThrows(IndexOutOfBoundsException.class, () -> bs.set(bs.logicalSize()), "set(logicalSize) out of bounds");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testIoobeSetIfClearNegative(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        assertThrows(IndexOutOfBoundsException.class, () -> bs.setIfClear(-1), "setIfClear(-1) out of bounds");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testIoobeSetIfClearOverCapacity(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        assertThrows(IndexOutOfBoundsException.class,
                () -> bs.setIfClear(bs.logicalSize()),
                "setIfClear(logicalSize) out of bounds");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testIoobeClearIfSetNegative(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        assertThrows(IndexOutOfBoundsException.class, () -> bs.clearIfSet(-1), "clearIfSet(-1) out of bounds");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testIoobeClearIfSetOverCapacity(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        assertThrows(IndexOutOfBoundsException.class,
                () -> bs.clearIfSet(bs.logicalSize()),
                "clearIfSet(logicalSize) out of bounds");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testIoobeFlipNegative(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        assertThrows(IndexOutOfBoundsException.class, () -> bs.flip(-1), "flip(-1) out of bounds");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testIoobeFlipOverCapacity(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        assertThrows(IndexOutOfBoundsException.class, () -> bs.flip(bs.logicalSize()), "flip(logicalSize) out of bounds");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testIoobeNextSetBit(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        assertThrows(IndexOutOfBoundsException.class, () -> bs.nextSetBit(-1), "nextSetBit(-1) out of bounds");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testIoobeNextClearBit(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        assertThrows(IndexOutOfBoundsException.class, () -> bs.nextClearBit(-1), "nextClearBit(-1) out of bounds");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testIoobePreviousSetBit(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        assertThrows(IndexOutOfBoundsException.class, () -> bs.previousSetBit(-2), "previousSetBit(-2) out of bounds");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testIoobePreviousClearBit(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        assertThrows(IndexOutOfBoundsException.class, () -> bs.previousClearBit(-2), "previousClearBit(-2) out of bounds");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testIoobeClearNextSetBit(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        assertThrows(IndexOutOfBoundsException.class, () -> bs.clearNextSetBit(-1), "clearNextSetBit(-1) out of bounds");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testIoobeClearNextNContinuousSetBits(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        assertThrows(IndexOutOfBoundsException.class,
                () -> bs.clearNextNContinuousSetBits(-1, 2),
                "clearNextNContinuousSetBits(-1,2) out of bounds");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testIoobeSetNextClearBit(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        assertThrows(IndexOutOfBoundsException.class, () -> bs.setNextClearBit(-1), "setNextClearBit(-1) out of bounds");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testIoobeSetNextNContinuousClearBits(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        assertThrows(IndexOutOfBoundsException.class,
                () -> bs.setNextNContinuousClearBits(-1, 2),
                "setNextNContinuousClearBits(-1,2) out of bounds");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testIoobeClearPreviousSetBit(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        assertThrows(IndexOutOfBoundsException.class,
                () -> bs.clearPreviousSetBit(-2),
                "clearPreviousSetBit(-2) out of bounds");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testIoobeClearPreviousNContinuousSetBit(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        assertThrows(IndexOutOfBoundsException.class,
                () -> bs.clearPreviousNContinuousSetBits(-2, 2),
                "clearPreviousNContinuousSetBits(-2,2) out of bounds");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testIoobeSetPreviousClearBit(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        assertThrows(IndexOutOfBoundsException.class,
                () -> bs.setPreviousClearBit(-2),
                "setPreviousClearBit(-2) out of bounds");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testIoobeSetPreviousNContinuousClearBit(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        assertThrows(IndexOutOfBoundsException.class,
                () -> bs.setPreviousNContinuousClearBits(-2, 2),
                "setPreviousNContinuousClearBits(-2,2) out of bounds");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testIoobeSetRangeFromNegative(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        assertThrows(IndexOutOfBoundsException.class, () -> bs.setRange(-1, 0), "setRange(-1,0) out of bounds");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testIoobeSetRangeFromOverTo(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        assertThrows(IndexOutOfBoundsException.class, () -> bs.setRange(1, 0), "setRange(1,0) invalid range");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testIoobeSetRangeToOverCapacity(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        assertThrows(IndexOutOfBoundsException.class,
                () -> bs.setRange(0, bs.logicalSize() + 1),
                "setRange to > logicalSize out of bounds");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testIoobeClearRangeFromNegative(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        assertThrows(IndexOutOfBoundsException.class, () -> bs.clearRange(-1, 0), "clearRange(-1,0) out of bounds");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testIoobeClearRangeFromOverTo(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        assertThrows(IndexOutOfBoundsException.class, () -> bs.clearRange(1, 0), "clearRange(1,0) invalid range");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testIoobeClearRangeToOverCapacity(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        assertThrows(IndexOutOfBoundsException.class,
                () -> bs.clearRange(0, bs.logicalSize() + 1),
                "clearRange to > logicalSize out of bounds");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testIoobeFlipRangeFromNegative(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        assertThrows(IndexOutOfBoundsException.class, () -> bs.flipRange(-1, 0), "flipRange(-1,0) out of bounds");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testIoobeFlipRangeFromOverTo(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        assertThrows(IndexOutOfBoundsException.class, () -> bs.flipRange(1, 0), "flipRange(1,0) invalid range");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testIoobeFlipRangeToOverCapacity(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        assertThrows(IndexOutOfBoundsException.class,
                () -> bs.flipRange(0, bs.logicalSize() + 1),
                "flipRange to > logicalSize out of bounds");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testIaeClearNextNContinuousSetBits(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        assertThrows(IllegalArgumentException.class,
                () -> bs.clearNextNContinuousSetBits(0, 0),
                "clearNextNContinuousSetBits requires n > 0");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testIaeSetNextNContinuousClearBits(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        assertThrows(IllegalArgumentException.class,
                () -> bs.setNextNContinuousClearBits(0, 0),
                "setNextNContinuousClearBits requires n > 0");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testIaeClearPreviousNContinuousSetBits(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        assertThrows(IllegalArgumentException.class,
                () -> bs.clearPreviousNContinuousSetBits(bs.logicalSize(), 0),
                "clearPreviousNContinuousSetBits requires n > 0");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("bitSets")
    public void testIaeSetPreviousNContinuousClearBits(String name, ReusableBitSet bs) {
        assertTrue(bs.logicalSize() >= LOGICAL_SIZE, name + ": logicalSize >= " + LOGICAL_SIZE);
        assertThrows(IllegalArgumentException.class,
                () -> bs.setPreviousNContinuousClearBits(bs.logicalSize(), 0),
                "setPreviousNContinuousClearBits requires n > 0");
    }
}
