/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo.hashing;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static java.nio.ByteOrder.*;
import static net.openhft.chronicle.algo.bytes.Accessor.checkedCharSequenceAccess;
import static net.openhft.chronicle.core.UnsafeMemory.MEMORY;
import static org.junit.jupiter.api.Assertions.*;

class LongHashFunctionTest {

    private static ByteOrder nonNativeOrder() {
        return nativeOrder() == LITTLE_ENDIAN ? BIG_ENDIAN : LITTLE_ENDIAN;
    }

    public static void test(LongHashFunction f, byte[] data, long eh) {
        int len = data.length;
        testVoid(f, eh, len);
        testBoolean(f, len);
        ByteBuffer bb = ByteBuffer.wrap(data).order(nativeOrder());
        testPrimitives(f, eh, len, bb);
        testArrays(f, data, eh, len, bb);
        testByteBuffers(f, eh, len, bb);
        testCharSequences(f, eh, len, bb);
        testMemory(f, eh, len, bb);
    }

    private static void testVoid(LongHashFunction f, long eh, int len) {
        if (len == 0)
            assertEquals(eh, f.hashVoid(), "void");
    }

    private static void testBoolean(LongHashFunction f, int len) {
        if (len != 1)
            return;
        for (boolean b : new boolean[]{true, false}) {
            boolean[] a = {b};
            long single = f.hashBoolean(b);
            assertEquals(single, f.hashBooleans(a));
        }
    }

    private static void testPrimitives(LongHashFunction f, long eh, int len, ByteBuffer bb) {
        if (len == 1)
            assertEquals(eh, f.hashByte(bb.get(0)), "byte hash");

        if (len == 2) {
            assertEquals(eh, f.hashShort(bb.getShort(0)), "short hash");
            assertEquals(eh, f.hashChar(bb.getChar(0)), "char hash");
        }
        if (len == 4)
            assertEquals(eh, f.hashInt(bb.getInt(0)), "int hash");

        if (len == 8)
            assertEquals(eh, f.hashLong(bb.getLong(0)), "long hash");
    }

    private static void testArrays(LongHashFunction f, byte[] data, long eh, int len,
                                   ByteBuffer bb) {
        assertEquals(eh, f.hashBytes(data), "byte array");

        byte[] data2 = new byte[len + 2];
        System.arraycopy(data, 0, data2, 1, len);
        assertEquals(eh, f.hashBytes(data2, 1, len), "byte array off len");

        if ((len & 1) == 0) {
            int shortLen = len / 2;

            short[] shorts = new short[shortLen];
            bb.asShortBuffer().get(shorts);
            assertEquals(eh, f.hashShorts(shorts), "short array");

            short[] shorts2 = new short[shortLen + 2];
            System.arraycopy(shorts, 0, shorts2, 1, shortLen);
            assertEquals(eh, f.hashShorts(shorts2, 1, shortLen), "short array off len");

            char[] chars = new char[shortLen];
            bb.asCharBuffer().get(chars);
            assertEquals(eh, f.hashChars(chars), "char array");

            char[] chars2 = new char[shortLen + 2];
            System.arraycopy(chars, 0, chars2, 1, shortLen);
            assertEquals(eh, f.hashChars(chars2, 1, shortLen), "char array off len");
        }
        if ((len & 3) == 0) {
            int intLen = len / 4;
            int[] ints = new int[intLen];
            bb.asIntBuffer().get(ints);
            assertEquals(eh, f.hashInts(ints), "int array");

            int[] ints2 = new int[intLen + 2];
            System.arraycopy(ints, 0, ints2, 1, intLen);
            assertEquals(eh, f.hashInts(ints2, 1, intLen), "int array off len");
        }
        if ((len & 7) == 0) {
            int longLen = len / 8;
            long[] longs = new long[longLen];
            bb.asLongBuffer().get(longs);
            assertEquals(eh, f.hashLongs(longs), "long array");

            long[] longs2 = new long[longLen + 2];
            System.arraycopy(longs, 0, longs2, 1, longLen);
            assertEquals(eh, f.hashLongs(longs2, 1, longLen), "long array off len");
        }
    }

    private static void testByteBuffers(LongHashFunction f, long eh, int len, ByteBuffer bb) {
        bb.order(LITTLE_ENDIAN);
        assertEquals(eh, f.hashBytes(bb), "byte buffer little endian");
        ByteBuffer bb2 = ByteBuffer.allocate(len + 2).order(LITTLE_ENDIAN);
        bb2.position(1);
        bb2.put(bb);
        assertEquals(eh, f.hashBytes(bb2, 1, len), "byte buffer little endian off len");

        bb.order(BIG_ENDIAN).clear();

        assertEquals(eh, f.hashBytes(bb), "byte buffer big endian");
        bb2.order(BIG_ENDIAN);
        assertEquals(eh, f.hashBytes(bb2, 1, len), "byte buffer big endian off len");

        bb.order(nativeOrder()).clear();
    }

    private static void testCharSequences(LongHashFunction f, long eh, int len, ByteBuffer bb) {
        if ((len & 1) == 0) {
            String s = bb.asCharBuffer().toString();
            for (int i = 0; i < s.length(); i++)
                if (!Character.isValidCodePoint(s.charAt(i)))
                    return;
            assertEquals(eh, f.hashChars(s), "string");

            StringBuilder sb = new StringBuilder();
            sb.append(s);
            assertEquals(eh, f.hashChars(sb), "string builder");

            sb.insert(0, 'a');
            sb.append('b');
            assertEquals(eh, f.hashChars(sb, 1, len / 2), "string builder off len");

            // Test for OpenJDK < 7u6, where substring wasn't copied char[] array
            assertEquals(eh, f.hashChars(sb.substring(1, len / 2 + 1)), "substring");

            if (len >= 6) {
                bb.order(nonNativeOrder());
                String s2 = bb.asCharBuffer().toString();
                assertEquals(s.charAt(0), Character.reverseBytes(bb.getChar(0)));
                assertNotEquals(eh, f.hashChars(s2), "string wrong order");

                assertEquals(eh,
                        f.hash(checkedCharSequenceAccess(nonNativeOrder()), s2, 0, len / 2), "string wrong order fixed");

                bb.order(nativeOrder()).clear();
            }
        }
    }

    private static void testMemory(LongHashFunction f, long eh, int len, ByteBuffer bb) {
        ByteBuffer directBB = ByteBuffer.allocateDirect(len);
        directBB.put(bb);
        assertEquals(eh, f.hashMemory(MEMORY.address(directBB), len), "memory");
        bb.clear();
    }
}
