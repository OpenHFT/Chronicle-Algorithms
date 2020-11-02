/*
 * Copyright 2014-2020 chronicle.software
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

import net.openhft.chronicle.algo.MemoryUnit;
import net.openhft.chronicle.algo.bytes.Access;
import net.openhft.chronicle.bytes.BytesStore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;

import static net.openhft.chronicle.algo.bytes.Access.checkedByteBufferAccess;
import static org.junit.Assert.*;

@RunWith(value = Parameterized.class)
public class DirectBitSetTest {

    private static final int[] INDICES = new int[]{0, 50, 100, 127, 128, 255};
    private final ReusableBitSet bs;
    private final boolean singleThreaded;

    public DirectBitSetTest(ReusableBitSet bs) {
        this.bs = bs;
        singleThreaded = bs.frame instanceof SingleThreadedFlatBitSetFrame;
        assertTrue(bs.logicalSize() >= 256);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        int capacityInBytes = (int) MemoryUnit.BITS.toBytes(256);
        BytesStore bytes1 = BytesStore.wrap(ByteBuffer.allocateDirect(capacityInBytes));
        BytesStore bytes2 = BytesStore.wrap(ByteBuffer.allocateDirect(capacityInBytes));
        return Arrays.asList(new Object[][]{
                {
                        new ReusableBitSet(
                                new ConcurrentFlatBitSetFrame(256),
                                Access.checkedBytesStoreAccess(),
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
                                Access.checkedBytesStoreAccess(),
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
            assertFalse(message + ", bit: " + i, bs.get(i));
        }
    }

    private void assertRangeIsSet(long from, long to) {
        for (long i = from; i < to; i++) {
            assertTrue(bs.get(i));
        }
    }

    @Test
    public void testGetSetClearAndCardinality() {
        bs.clearAll();
        assertEquals(0, bs.cardinality());
        int c = 0;
        for (int i : INDICES) {
            c++;
            assertFalse("At index " + i, bs.get(i));
            assertFalse("At index " + i, bs.isSet(i));
            assertTrue("At index " + i, bs.isClear(i));
            bs.set(i);
            assertTrue("At index " + i, bs.get(i));
            assertTrue("At index " + i, bs.isSet(i));
            assertFalse("At index " + i, bs.isClear(i));
            assertEquals(c, bs.cardinality());
        }
        for (int i : INDICES) {
            assertTrue("At index " + i, bs.get(i));
            assertTrue("At index " + i, bs.isSet(i));
            assertFalse("At index " + i, bs.isClear(i));
            bs.clear(i);
            assertFalse("At index " + i, bs.get(i));
            assertFalse("At index " + i, bs.isSet(i));
            assertTrue("At index " + i, bs.isClear(i));
        }
        for (int i : INDICES) {
            assertEquals("At index " + i, true, bs.setIfClear(i));
            assertEquals("At index " + i, false, bs.setIfClear(i));
        }
        for (int i : INDICES) {
            assertEquals("At index " + i, true, bs.clearIfSet(i));
            assertEquals("At index " + i, false, bs.clearIfSet(i));
        }
    }

    @Test
    public void testFlip() {
        bs.clearAll();
        for (int i : INDICES) {
            assertEquals("At index " + i, false, bs.get(i));
            bs.flip(i);
            assertEquals("At index " + i, true, bs.get(i));
            bs.flip(i);
            assertEquals("At index " + i, false, bs.get(i));
        }
    }

    @Test
    public void testNextSetBit() {
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

    @Test
    public void testSetBitsIteration() {
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

    @Test
    public void testClearNextSetBit() {
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

    @Test
    public void testClearNext1SetBit() {
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

    @Test
    public void testNextClearBit() {
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

    @Test
    public void testSetNextClearBit() {
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

    @Test
    public void testSetNext1ClearBit() {
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

    @Test
    public void testPreviousSetBit() {
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

    @Test
    public void testClearPreviousSetBit() {
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

    @Test
    public void testClearPrevious1SetBit() {
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

    @Test
    public void testPreviousClearBit() {
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

    @Test
    public void testSetPreviousClearBit() {
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

    @Test
    public void testSetPrevious1ClearBit() {
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

    @Test
    public void testSetAll() {
        bs.clearAll();
        bs.setAll();
        assertEquals(bs.logicalSize(), bs.cardinality());
    }

    @Test
    public void testRangeOpsWithinLongCase() {
        bs.clearAll();
        if (singleThreaded) {
            assertTrue(bs.isRangeClear(0, 0));
            assertTrue(bs.isRangeClear(63, 63));
            assertTrue(bs.isRangeSet(0, 0));
            assertTrue(bs.isRangeSet(63, 63));
        }
        bs.flipRange(0, 0);
        assertEquals(false, bs.get(0));
        assertEquals(0, bs.cardinality());
        bs.flipRange(0, 1);
        assertEquals(true, bs.get(0));
        assertEquals(1, bs.cardinality());
        if (singleThreaded) {
            assertTrue(bs.isRangeSet(0, 1));
            assertFalse(bs.isRangeSet(0, 2));
            assertFalse(bs.isRangeClear(0, 1));
        }
        bs.clearRange(0, 0);
        assertEquals(true, bs.get(0));
        assertEquals(1, bs.cardinality());
        bs.clearRange(0, 1);
        assertEquals(false, bs.get(0));
        assertEquals(0, bs.cardinality());

        bs.setRange(0, 0);
        assertEquals(false, bs.get(0));
        assertEquals(0, bs.cardinality());
        bs.setRange(0, 1);
        assertEquals(true, bs.get(0));
        assertEquals(1, bs.cardinality());
    }

    @Test
    public void testRangeOpsCrossLongCase() {
        bs.clearAll();

        bs.flipRange(63, 64);
        assertEquals(true, bs.get(63));
        assertEquals(false, bs.get(64));
        assertEquals(1, bs.cardinality());
        if (singleThreaded) {
            assertFalse(bs.isRangeSet(63, 65));
            assertFalse(bs.isRangeClear(63, 65));
        }
        bs.flipRange(63, 65);
        assertEquals(false, bs.get(63));
        assertEquals(true, bs.get(64));
        assertEquals(1, bs.cardinality());
        if (singleThreaded) {
            assertFalse(bs.isRangeSet(63, 65));
            assertFalse(bs.isRangeClear(63, 65));
        }
        bs.clear(64);
        bs.setRange(63, 64);
        assertEquals(true, bs.get(63));
        assertEquals(false, bs.get(64));
        assertEquals(1, bs.cardinality());

        bs.set(64);
        bs.clearRange(63, 64);
        assertEquals(false, bs.get(63));
        assertEquals(true, bs.get(64));
        assertEquals(1, bs.cardinality());

        bs.clear(64);
        bs.setRange(63, 65);
        assertEquals(true, bs.get(63));
        assertEquals(true, bs.get(64));
        assertEquals(2, bs.cardinality());
        if (singleThreaded) {
            assertTrue(bs.isRangeSet(63, 65));
            assertFalse(bs.isRangeClear(63, 65));
        }
        bs.clearRange(63, 65);
        assertEquals(false, bs.get(63));
        assertEquals(false, bs.get(64));
        assertEquals(0, bs.cardinality());
        if (singleThreaded) {
            assertFalse(bs.isRangeSet(63, 65));
            assertTrue(bs.isRangeClear(63, 65));
        }
    }

    @Test
    public void testRangeOpsSpanLongCase() {
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

    @Test
    public void testSetNextNContinuousClearBitsWithinLongCase() {
        long size = (bs.logicalSize() + 63) / 64 * 64;
        for (int n = 1; n <= 64; n *= 2) {
            bs.clearAll();
            for (int i = 0; i < size / n; i++) {
                assertRangeIsClear(i * n, i * n + n);
                assertEquals(m(n), i * n, bs.setNextNContinuousClearBits(0L, n));
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

    @Test
    public void testSetNextNContinuousClearBitsCrossLongCase() {
        if (concurrentBS())
            return;
        long size = bs.logicalSize();
        for (int n : new int[]{3, 7, 13, 31, 33, 63, 65, 100, 127, 128, 129, 254, 255}) {
            bs.clearAll();
            for (int i = 0; i < size / n; i++) {
                assertRangeIsClear(i * n, i * n + n);
                assertEquals(m(n), i * n, bs.setNextNContinuousClearBits(0L, n));
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
            assertEquals("" + n, from, bs.setNextNContinuousClearBits(0L, n));
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

    @Test
    public void testClearNextNContinuousSetBitsWithinLongCase() {
        long size = (bs.logicalSize() + 63) / 64 * 64;
        for (int n = 1; n <= 64; n *= 2) {
            bs.setAll();
            long cardinality = bs.cardinality();
            for (int i = 0; i < size / n; i++) {
                assertRangeIsSet(i * n, i * n + n);
                assertEquals(m(n), i * n, bs.clearNextNContinuousSetBits(0L, n));
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

    @Test
    public void testClearNextNContinuousSetBitsCrossLongCase() {
        if (concurrentBS())
            return;
        long size = bs.logicalSize();
        for (int n : new int[]{3, 7, 13, 31, 33, 63}) {
            bs.setAll();
            long cardinality = bs.cardinality();
            for (int i = 0; i < size / n; i++) {
                assertRangeIsSet(i * n, i * n + n);
                assertEquals(m(n), i * n, bs.clearNextNContinuousSetBits(0L, n));
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

    public boolean concurrentBS() {
        return bs.frame instanceof ConcurrentFlatBitSetFrame;
    }

    @Test
    public void testSetPreviousNContinuousClearBitsWithinLongCase() {
        long size = (bs.logicalSize() + 63) / 64 * 64;
        for (int n = 1; n <= 64; n *= 2) {
            bs.clearAll();
            long cardinality = 0;
            for (long i = size / n - 1; i >= 0; i--) {
                assertRangeIsClear(i * n, i * n + n);
                assertEquals(m(n), i * n, bs.setPreviousNContinuousClearBits(size, n));
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

    @Test
    public void testSetPreviousNContinuousClearBitsCrossLongCase() {
        if (concurrentBS())
            return;
        long size = bs.logicalSize();
        for (int n : new int[]{3, 7, 13, 31, 33, 63}) {
            bs.clearAll();
            long cardinality = 0;
            for (long from = size - n; from >= 0; from -= n) {
                assertRangeIsClear(from, from + n);
                assertEquals(m(n), from, bs.setPreviousNContinuousClearBits(size, n));
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

    @Test
    public void testClearPreviousNContinuousSetBitsWithinLongCase() {
        long size = (bs.logicalSize() + 63) / 64 * 64;
        for (int n = 1; n <= 64; n *= 2) {
            bs.setAll();
            long cardinality = bs.cardinality();
            for (long i = size / n - 1; i >= 0; i--) {
                assertRangeIsSet(i * n, i * n + n);
                assertEquals(m(n), i * n, bs.clearPreviousNContinuousSetBits(size, n));
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

    @Test
    public void testClearPreviousNContinuousSetBitsCrossLongCase() {
        if (concurrentBS())
            return;
        long size = bs.logicalSize();
        for (int n : new int[]{3, 7, 13, 31, 33, 63}) {
            bs.setAll();
            long cardinality = bs.cardinality();
            for (long from = size - n; from >= 0; from -= n) {
                assertRangeIsSet(from, from + n);
                assertEquals(m(n), from, bs.clearPreviousNContinuousSetBits(size, n));
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

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeGetNegative() {
        bs.get(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeGetOverCapacity() {
        bs.get(bs.logicalSize());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeSetNegative() {
        bs.set(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeSetOverCapacity() {
        bs.set(bs.logicalSize());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeSetIfClearNegative() {
        bs.setIfClear(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeSetIfClearOverCapacity() {
        bs.setIfClear(bs.logicalSize());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeClearIfSetNegative() {
        bs.clearIfSet(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeClearIfSetOverCapacity() {
        bs.clearIfSet(bs.logicalSize());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeFlipNegative() {
        bs.flip(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeFlipOverCapacity() {
        bs.flip(bs.logicalSize());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeNextSetBit() {
        bs.nextSetBit(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeNextClearBit() {
        bs.nextClearBit(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobePreviousSetBit() {
        bs.previousSetBit(-2);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobePreviousClearBit() {
        bs.previousClearBit(-2);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeClearNextSetBit() {
        bs.clearNextSetBit(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeClearNextNContinuousSetBits() {
        bs.clearNextNContinuousSetBits(-1, 2);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeSetNextClearBit() {
        bs.setNextClearBit(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeSetNextNContinuousClearBits() {
        bs.setNextNContinuousClearBits(-1, 2);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeClearPreviousSetBit() {
        bs.clearPreviousSetBit(-2);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeClearPreviousNContinuousSetBit() {
        bs.clearPreviousNContinuousSetBits(-2, 2);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeSetPreviousClearBit() {
        bs.setPreviousClearBit(-2);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeSetPreviousNContinuousClearBit() {
        bs.setPreviousNContinuousClearBits(-2, 2);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeSetRangeFromNegative() {
        bs.setRange(-1, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeSetRangeFromOverTo() {
        bs.setRange(1, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeSetRangeToOverCapacity() {
        bs.setRange(0, bs.logicalSize() + 1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeClearRangeFromNegative() {
        bs.clearRange(-1, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeClearRangeFromOverTo() {
        bs.clearRange(1, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeClearRangeToOverCapacity() {
        bs.clearRange(0, bs.logicalSize() + 1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeFlipRangeFromNegative() {
        bs.flipRange(-1, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeFlipRangeFromOverTo() {
        bs.flipRange(1, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testIoobeFlipRangeToOverCapacity() {
        bs.flipRange(0, bs.logicalSize() + 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIaeClearNextNContinuousSetBits() {
        bs.clearNextNContinuousSetBits(0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIaeSetNextNContinuousClearBits() {
        bs.setNextNContinuousClearBits(0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIaeClearPreviousNContinuousSetBits() {
        bs.clearPreviousNContinuousSetBits(bs.logicalSize(), 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIaeSetPreviousNContinuousClearBits() {
        bs.setPreviousNContinuousClearBits(bs.logicalSize(), 0);
    }
}
