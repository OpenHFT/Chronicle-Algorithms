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
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ReusableBitSetTest {

    private static final int LOGICAL_SIZE = 256;
    private static final int STORAGE_BYTES = (int) MemoryUnit.BITS.toBytes(LOGICAL_SIZE);
    private static final int BUFFER_BYTES = STORAGE_BYTES + 16; // headroom for offset tests

    static Stream<Arguments> bitSets() {
        List<Arguments> scenarios = new ArrayList<>();
        scenarios.add(Arguments.of(
                "single-threaded heap buffer",
                (Supplier<BitSetFixture>) () -> BitSetFixture.singleThreaded(ByteBuffer.allocate(BUFFER_BYTES))
        ));
        scenarios.add(Arguments.of(
                "single-threaded direct buffer",
                (Supplier<BitSetFixture>) () -> BitSetFixture.singleThreaded(ByteBuffer.allocateDirect(BUFFER_BYTES))
        ));
        scenarios.add(Arguments.of(
                "concurrent bytes store",
                (Supplier<BitSetFixture>) BitSetFixture::concurrentStore
        ));
        return scenarios.stream();
    }

    @ParameterizedTest(name = "{0} - basic mutations")
    @MethodSource("bitSets")
    void basicMutationsCoverAllOperations(String name, Supplier<BitSetFixture> factory) {
        try (BitSetFixture fixture = factory.get()) {
            ReusableBitSet bs = fixture.bitSet();
            bs.clearAll();
            assertEquals(0, bs.cardinality());
            assertRangeClear(bs, 0, LOGICAL_SIZE);

            bs.set(5);
            assertTrue(bs.get(5));
            assertTrue(bs.isSet(5));
            assertFalse(bs.isClear(5));
            assertEquals(1, bs.cardinality());

            assertFalse(bs.setIfClear(5));
            assertTrue(bs.clearIfSet(5));
            assertEquals(0, bs.cardinality());
            bs.set(5, true);
            assertTrue(bs.get(5));
            bs.set(5, false);
            assertFalse(bs.get(5));

            bs.setRange(10, 20);
            assertRangeSet(bs, 10, 20);
            assertEquals(10, bs.cardinality());

            bs.clearRange(12, 18);
            assertEquals(4, bs.cardinality());
            bs.setRange(12, 18, true);
            assertEquals(10, bs.cardinality());

            bs.flip(12);
            assertFalse(bs.get(12));
            bs.flipRange(12, 16);
            assertFalse(bs.get(13));
            assertFalse(bs.get(14));
            assertFalse(bs.get(15));

            assertEquals(10, bs.nextSetBit(0));
            assertEquals(0, bs.nextClearBit(0));
            assertEquals(19, bs.previousSetBit(LOGICAL_SIZE - 1));
            assertEquals(15, bs.previousClearBit(18));

            assertEquals(0, bs.setNextClearBit(0));
            assertTrue(bs.get(0));
            assertEquals(0, bs.clearNextSetBit(0));
            assertFalse(bs.get(0));
            assertEquals(10, bs.clearNextSetBit(1));
            assertFalse(bs.get(10));

            bs.setRange(32, 48);
            assertRangeSet(bs, 32, 48);
            assertEquals(31, bs.setPreviousClearBit(47));
            assertTrue(bs.get(31));
            assertEquals(47, bs.clearPreviousSetBit(LOGICAL_SIZE - 1));
            assertFalse(bs.get(47));

            bs.clearAll();
            bs.setRange(10, 20);
            long blockStart = bs.setNextNContinuousClearBits(0, 6);
            assertEquals(0, blockStart);
            assertEquals(blockStart, bs.clearNextNContinuousSetBits(blockStart, 6));
            assertRangeClear(bs, blockStart, blockStart + 6);
            long prevBlockStart = bs.setPreviousNContinuousClearBits(9, 4);
            assertEquals(6, prevBlockStart);
            assertRangeSet(bs, prevBlockStart, prevBlockStart + 4);
            long clearedPrevStart = bs.clearPreviousNContinuousSetBits(18, 4);
            assertEquals(15, clearedPrevStart);
            assertRangeClear(bs, clearedPrevStart, clearedPrevStart + 4);

            BitSet.Bits bits = bs.setBits().reset();
            while (bits.next() >= 0) {
                // iterate to exercise iterator implementation
            }

            bs.setAll();
            assertEquals(LOGICAL_SIZE, bs.cardinality());
            bs.clearAll();
            assertEquals(0, bs.cardinality());
        }
    }

    private static final class BitSetFixture implements AutoCloseable {
        private final ReusableBitSet bitSet;
        private final Access<?> access;
        private final Object handle;
        private final BytesStore<?, ?> bytesStore;
        private final long offset;

        private BitSetFixture(ReusableBitSet bitSet, Access<?> access, Object handle, BytesStore<?, ?> bytesStore, long offset) {
            this.bitSet = bitSet;
            this.access = access;
            this.handle = handle;
            this.bytesStore = bytesStore;
            this.offset = offset;
        }

        static BitSetFixture singleThreaded(ByteBuffer buffer) {
            buffer.order(ByteOrder.nativeOrder());
            long offset = 0L;
            SingleThreadedFlatBitSetFrame frame = new SingleThreadedFlatBitSetFrame(LOGICAL_SIZE);
            Access<ByteBuffer> bbAccess = Access.checkedByteBufferAccess();
            ReusableBitSet bitSet = new ReusableBitSet(frame, bbAccess, buffer, offset);
            return new BitSetFixture(bitSet, bbAccess, buffer, null, offset);
        }

        @SuppressWarnings("unchecked")
        static BitSetFixture concurrentStore() {
            long offset = 0L;
            BytesStore<?, ?> store = BytesStore.nativeStoreWithFixedCapacity(BUFFER_BYTES);
            ConcurrentFlatBitSetFrame frame = new ConcurrentFlatBitSetFrame(LOGICAL_SIZE);
            Access<?> storeAccess = Access.checkedBytesStoreAccess();
            @SuppressWarnings("rawtypes")
            ReusableBitSet bitSet = new ReusableBitSet(frame, (Access) storeAccess, store, offset);
            return new BitSetFixture(bitSet, storeAccess, store, store, offset);
        }

        ReusableBitSet bitSet() {
            return bitSet;
        }

        Access<?> access() {
            return access;
        }

        Object handle() {
            return handle;
        }

        @Override
        public void close() {
            if (bytesStore != null) {
                bytesStore.releaseLast();
            }
        }
    }

    private static void assertRangeSet(BitSet bitSet, long from, long to) {
        for (long i = from; i < to; i++) {
            assertTrue(bitSet.get(i), "bit " + i + " expected to be set");
        }
    }

    private static void assertRangeClear(BitSet bitSet, long from, long to) {
        for (long i = from; i < to; i++) {
            assertFalse(bitSet.get(i), "bit " + i + " expected to be clear");
        }
    }
}
