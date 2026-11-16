/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo.bitset;

import net.openhft.chronicle.algo.bytes.Access;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class BitSetFrameModelTest {
    private static final int SIZE = 256;
    private static final int OFFSET = 16;
    private static final int[] EDGES = {0, 1, 7, 31, 32, 33, 62, 63, 64, 65, 95, 96,
            97, 126, 127, 128, 129, 191, 192, 254, 255, 256};
    private static final int[] LENGTHS = {1, 2, 3, 7, 16, 31, 32, 33, 63, 64};
    private final CheckedAccess access = new CheckedAccess();

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void iteratorHonoursOffsetAndReset(boolean concurrent) {
        BitSetFrame frame = frame(concurrent);
        BitSetFrame.Bits iterator = frame.setBits();
        for (int pattern = 0; pattern < 24; pattern++) {
            boolean[] bits = pattern(pattern);
            ByteBuffer memory = memory(bits);
            iterator.reset(access, memory, OFFSET);
            for (int bit = 0; bit < SIZE; bit++) {
                if (bits[bit])
                    assertEquals(bit, iterator.next(access, memory, OFFSET), "pattern=" + pattern);
            }
            assertEquals(-1, iterator.next(access, memory, OFFSET));
            assertEquals(-1, iterator.next(access, memory, OFFSET));
            assertMemory(bits, memory, "iteration must not mutate memory");
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void continuousClaimsMatchBitByBitOracle(boolean concurrent) {
        BitSetFrame frame = frame(concurrent);
        int cases = 0;
        for (int pattern = 0; pattern < 24; pattern++) {
            for (int start : EDGES) {
                for (int count : LENGTHS) {
                    for (int operation = 0; operation < 4; operation++) {
                        boolean backwards = operation >= 2;
                        boolean wanted = (operation & 1) != 0;
                        boolean[] bits = pattern(pattern);
                        ByteBuffer memory = memory(bits);
                        int expected = findRun(bits, start, count, wanted, backwards, concurrent);
                        access.failuresLeft = concurrent ? 2 : 0;
                        long actual;
                        switch (operation) {
                            case 0: actual = frame.setNextNContinuousClearBits(access, memory, OFFSET, start, count); break;
                            case 1: actual = frame.clearNextNContinuousSetBits(access, memory, OFFSET, start, count); break;
                            case 2: actual = frame.setPreviousNContinuousClearBits(access, memory, OFFSET, start, count); break;
                            default: actual = frame.clearPreviousNContinuousSetBits(access, memory, OFFSET, start, count);
                        }
                        String description = concurrent + " pattern=" + pattern + " start=" + start +
                                " count=" + count + " operation=" + operation;
                        assertEquals(expected, actual, description);
                        if (expected >= 0)
                            Arrays.fill(bits, expected, expected + count, !wanted);
                        assertMemory(bits, memory, description);
                        cases++;
                    }
                }
            }
        }
        System.out.println("continuous model cases=" + cases + " concurrent=" + concurrent);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void scalarSearchAndClaimsMatchBitByBitOracle(boolean concurrent) {
        BitSetFrame frame = frame(concurrent);
        for (int pattern = 0; pattern < 12; pattern++) {
            for (int start = 0; start <= SIZE; start++) {
                boolean[] original = pattern(pattern);
                ByteBuffer unchanged = memory(original);
                assertEquals(findRun(original, start, 1, true, false, false), frame.nextSetBit(access, unchanged, OFFSET, start));
                assertEquals(findRun(original, start, 1, false, false, false), frame.nextClearBit(access, unchanged, OFFSET, start));
                assertEquals(findRun(original, start, 1, true, true, false), frame.previousSetBit(access, unchanged, OFFSET, start));
                assertEquals(findRun(original, start, 1, false, true, false), frame.previousClearBit(access, unchanged, OFFSET, start));
                for (int op = 0; op < 4; op++) {
                    boolean[] bits = original.clone();
                    ByteBuffer memory = memory(bits);
                    boolean wanted = (op & 1) != 0;
                    int expected = findRun(bits, start, 1, wanted, op >= 2, false);
                    access.failuresLeft = concurrent ? 2 : 0;
                    long actual;
                    switch (op) {
                        case 0: actual = frame.setNextClearBit(access, memory, OFFSET, start); break;
                        case 1: actual = frame.clearNextSetBit(access, memory, OFFSET, start); break;
                        case 2: actual = frame.setPreviousClearBit(access, memory, OFFSET, start); break;
                        default: actual = frame.clearPreviousSetBit(access, memory, OFFSET, start);
                    }
                    assertEquals(expected, actual, "scalar pattern=" + pattern + " start=" + start + " op=" + op);
                    if (expected >= 0)
                        bits[expected] = !wanted;
                    assertMemory(bits, memory, "scalar claim");
                }
            }
        }
        assertEquals(-1, frame.previousSetBit(access, memory(pattern(0)), OFFSET, -1));
        assertEquals(-1, frame.previousClearBit(access, memory(pattern(1)), OFFSET, -1));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void rangeMutationsAndPredicatesMatchBitByBitOracle(boolean concurrent) {
        BitSetFrame frame = frame(concurrent);
        for (int pattern = 0; pattern < 12; pattern++) {
            for (int from : EDGES) {
                for (int to : EDGES) {
                    if (to < from)
                        continue;
                    for (int operation = 0; operation < 3; operation++) {
                        boolean[] bits = pattern(pattern);
                        ByteBuffer memory = memory(bits);
                        access.failuresLeft = concurrent ? 2 : 0;
                        if (operation == 0) frame.setRange(access, memory, OFFSET, from, to);
                        if (operation == 1) frame.clearRange(access, memory, OFFSET, from, to);
                        if (operation == 2) frame.flipRange(access, memory, OFFSET, from, to);
                        for (int bit = from; bit < to; bit++)
                            bits[bit] = operation == 0 || operation == 2 && !bits[bit];
                        String description = "range " + concurrent + " pattern=" + pattern + " from=" + from + " to=" + to + " op=" + operation;
                        assertMemory(bits, memory, description);
                        if (!concurrent) {
                            assertEquals(all(bits, from, to, true), frame.isRangeSet(access, memory, OFFSET, from, to), description);
                            assertEquals(all(bits, from, to, false), frame.isRangeClear(access, memory, OFFSET, from, to), description);
                        }
                        assertEquals(cardinality(bits), frame.cardinality(access, memory, OFFSET), description);
                    }
                }
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void singleBitMutationPreservesNeighbours(boolean concurrent) {
        BitSetFrame frame = frame(concurrent);
        boolean[] bits = pattern(4);
        ByteBuffer memory = memory(bits);
        for (int bit = 0; bit < SIZE; bit++) {
            access.failuresLeft = concurrent ? 1 : 0;
            assertEquals(!bits[bit], frame.setIfClear(access, memory, OFFSET, bit));
            bits[bit] = true;
            assertFalse(frame.setIfClear(access, memory, OFFSET, bit));
            assertTrue(frame.clearIfSet(access, memory, OFFSET, bit));
            bits[bit] = false;
            assertFalse(frame.clearIfSet(access, memory, OFFSET, bit));
            frame.flip(access, memory, OFFSET, bit);
            bits[bit] = true;
            assertMemory(bits, memory, "single-bit " + bit);
        }
    }

    private static BitSetFrame frame(boolean concurrent) {
        return concurrent ? new ConcurrentFlatBitSetFrame(SIZE) : new SingleThreadedFlatBitSetFrame(SIZE);
    }

    private static int findRun(boolean[] bits, int from, int length, boolean wanted,
                               boolean backwards, boolean singleWord) {
        int first = backwards ? Math.min(from, bits.length - 1) - length + 1 : from;
        for (int i = first; i >= 0 && i + length <= bits.length; i += backwards ? -1 : 1) {
            // Concurrent claims intentionally cannot span a native word, as documented by the implementation.
            if (singleWord && i / 64 != (i + length - 1) / 64)
                continue;
            if (all(bits, i, i + length, wanted))
                return i;
        }
        return -1;
    }

    private static boolean all(boolean[] bits, int from, int to, boolean wanted) {
        for (int bit = from; bit < to; bit++)
            if (bits[bit] != wanted)
                return false;
        return true;
    }

    private static int cardinality(boolean[] bits) {
        int result = 0;
        for (boolean bit : bits)
            if (bit) result++;
        return result;
    }

    private static boolean[] pattern(int kind) {
        boolean[] bits = new boolean[SIZE];
        Random random = new Random(7001 + kind);
        for (int bit = 0; bit < SIZE; bit++) {
            switch (kind) {
                case 0: bits[bit] = false; break;
                case 1: bits[bit] = true; break;
                case 2: bits[bit] = bit % 2 == 0; break;
                case 3: bits[bit] = bit / 64 % 2 == 0; break;
                case 4: bits[bit] = bit % 64 == 63; break;
                case 5: bits[bit] = bit % 64 != 63; break;
                case 6: bits[bit] = bit >= 30 && bit < 160; break;
                case 7: bits[bit] = bit >= 63 && bit < 128; break;
                case 8: bits[bit] = bit < 63 || bit >= 128; break;
                case 9: bits[bit] = bit / 8 % 2 == 0; break;
                case 10: bits[bit] = bit == 0 || bit == 255; break;
                case 11: bits[bit] = bit != 0 && bit != 255; break;
                default: bits[bit] = random.nextInt(kind % 3 == 0 ? 8 : 2) == 0;
            }
        }
        return bits;
    }

    private static ByteBuffer memory(boolean[] bits) {
        ByteBuffer b = ByteBuffer.allocate(SIZE / 8 + OFFSET * 2).order(ByteOrder.nativeOrder());
        for (int i = 0; i < b.capacity(); i++) b.put(i, (byte) 0x5a);
        for (int word = 0; word < SIZE / 64; word++) b.putLong(OFFSET + word * 8, word(bits, word));
        return b;
    }

    private static long word(boolean[] bits, int word) {
        long value = 0;
        for (int bit = 0; bit < 64; bit++) if (bits[word * 64 + bit]) value |= 1L << bit;
        return value;
    }

    private static void assertMemory(boolean[] bits, ByteBuffer b, String description) {
        for (int word = 0; word < SIZE / 64; word++)
            assertEquals(word(bits, word), b.getLong(OFFSET + word * 8), description + " word=" + word);
        for (int i = 0; i < OFFSET; i++) {
            assertEquals((byte) 0x5a, b.get(i), "prefix canary");
            assertEquals((byte) 0x5a, b.get(b.capacity() - i - 1), "suffix canary");
        }
    }

    /** Deterministic CAS failures exercise retry loops without relying on scheduler timing. */
    private static class CheckedAccess implements Access<ByteBuffer> {
        int failuresLeft;
        private int index(long offset, int size) {
            if (offset < OFFSET || offset + size > OFFSET + SIZE / 8)
                throw new AssertionError("access outside bitset: " + offset + " size=" + size);
            return (int) offset;
        }
        public byte readByte(ByteBuffer b, long o) { return b.get(index(o, 1)); }
        public short readShort(ByteBuffer b, long o) { return b.getShort(index(o, 2)); }
        public int readInt(ByteBuffer b, long o) { return b.getInt(index(o, 4)); }
        public long readLong(ByteBuffer b, long o) { return b.getLong(index(o, 8)); }
        public long readVolatileLong(ByteBuffer b, long o) { return readLong(b, o); }
        public void writeByte(ByteBuffer b, long o, byte v) { b.put(index(o, 1), v); }
        public void writeShort(ByteBuffer b, long o, short v) { b.putShort(index(o, 2), v); }
        public void writeInt(ByteBuffer b, long o, int v) { b.putInt(index(o, 4), v); }
        public void writeLong(ByteBuffer b, long o, long v) { b.putLong(index(o, 8), v); }
        public void writeOrderedLong(ByteBuffer b, long o, long v) { writeLong(b, o, v); }
        public void writeFloat(ByteBuffer b, long o, float v) { b.putFloat(index(o, 4), v); }
        public void writeDouble(ByteBuffer b, long o, double v) { b.putDouble(index(o, 8), v); }
        public ByteOrder byteOrder(ByteBuffer b) { return b.order(); }
        public boolean compareAndSwapInt(ByteBuffer b, long o, int expected, int value) { throw new AssertionError("unexpected int CAS"); }
        public boolean compareAndSwapLong(ByteBuffer b, long o, long expected, long value) {
            if (failuresLeft > 0) { failuresLeft--; return false; }
            if (readLong(b, o) != expected) return false;
            writeLong(b, o, value);
            return true;
        }
    }
}
