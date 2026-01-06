/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo.hashing;

import net.openhft.chronicle.algo.bytes.Access;
import net.openhft.chronicle.algo.bytes.NativeAccess;
import net.openhft.chronicle.core.Jvm;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static java.nio.ByteOrder.nativeOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class HashTestSupport {

    private static final Access<ByteBuffer> BYTE_BUFFER_ACCESS = Access.checkedByteBufferAccess();
    private static final NativeAccess<byte[]> BYTE_ARRAY_ACCESS = NativeAccess.instance();

    private HashTestSupport() {
    }

    static byte[] loopingBytes(int len) {
        byte[] data = new byte[len];
        for (int i = 0; i < len; i++) {
            data[i] = (byte) i;
        }
        return data;
    }

    static void assertHashMatchesVectors(LongHashFunction function, byte[] data, long expected) {
        int len = data.length;
        assertEquals(expected, function.hashBytes(data), "hashBytes");
        assertEquals(expected,
                function.hashBytes(ByteBuffer.wrap(Arrays.copyOf(data, len)).order(nativeOrder())),
                "hashBytes(ByteBuffer)");

        ByteBuffer wrapped = ByteBuffer.allocate(len + 8).order(nativeOrder());
        wrapped.put((byte) 0x7F);
        wrapped.put(data);
        wrapped.flip();
        wrapped.position(1);
        wrapped.limit(1 + len);
        assertEquals(expected, function.hashBytes(wrapped), "hashBytes(ByteBuffer slice)");

        if (len == 0) {
            assertEquals(expected, function.hashVoid(), "hashVoid");
        }

        ByteBuffer bb = ByteBuffer.wrap(Arrays.copyOf(data, Math.max(len, 8))).order(nativeOrder());
        exercisePrimitiveHashes(function, bb, len, expected);
        exerciseArrayHashes(function, bb, len, expected);
        exerciseReadAccess(function, data, expected);
        exerciseNegativePrimitiveHashes(function);
    }

    private static void exercisePrimitiveHashes(LongHashFunction function,
                                                ByteBuffer buffer,
                                                int len,
                                                long expected) {
        if (len >= 1) {
            long byteHash = function.hashByte(buffer.get(0));
            if (len == 1) {
                assertEquals(expected, byteHash, "hashByte");
            }
        }

        if (len >= 2) {
            short value = buffer.getShort(0);
            long shortHash = function.hashShort(value);
            if (len == 2) {
                assertEquals(expected, shortHash, "hashShort");
                assertEquals(expected, function.hashChar((char) value), "hashChar");
            }
        }

        if (len >= 4) {
            int value = buffer.getInt(0);
            long intHash = function.hashInt(value);
            if (len == 4) {
                assertEquals(expected, intHash, "hashInt");
            }
        }

        if (len >= 8) {
            long value = buffer.getLong(0);
            long longHash = function.hashLong(value);
            if (len == 8) {
                assertEquals(expected, longHash, "hashLong");
            }
        }
    }

    private static void exerciseArrayHashes(LongHashFunction function,
                                            ByteBuffer buffer,
                                            int len,
                                            long expected) {
        assertEquals(expected,
                function.hashBytes(padArray(dataWithPadding(buffer, len), 1), 1, len),
                "hashBytes(byte[],off,len)");

        if ((len & 1) == 0) {
            short[] shorts = toShortArray(buffer, len);
            assertEquals(expected, function.hashShorts(shorts), "hashShorts");
            assertEquals(expected,
                    function.hashShorts(padArray(shorts, (short) 1), 1, shorts.length),
                    "hashShorts(off,len)");

            char[] chars = toCharArray(buffer, len);
            assertEquals(expected, function.hashChars(chars), "hashChars");
            assertEquals(expected,
                    function.hashChars(padArray(chars, (char) 1), 1, chars.length),
                    "hashChars(off,len)");
        }

        if ((len & 3) == 0) {
            int[] ints = toIntArray(buffer, len);
            assertEquals(expected, function.hashInts(ints), "hashInts");
            assertEquals(expected,
                    function.hashInts(padArray(ints, 1), 1, ints.length),
                    "hashInts(off,len)");
        }

        if ((len & 7) == 0) {
            long[] longs = toLongArray(buffer, len);
            assertEquals(expected, function.hashLongs(longs), "hashLongs");
            assertEquals(expected,
                    function.hashLongs(padArray(longs, 1L), 1, longs.length),
                    "hashLongs(off,len)");
        }
    }

    private static void exerciseReadAccess(LongHashFunction function, byte[] data, long expected) {
        long baseOffset = Jvm.arrayByteBaseOffset();
        assertEquals(expected,
                function.hash(data, BYTE_ARRAY_ACCESS, baseOffset, data.length),
                "hash(ReadAccess)");

        ByteBuffer buffer = ByteBuffer.wrap(data).order(nativeOrder());
        assertEquals(expected,
                function.hash(buffer, BYTE_BUFFER_ACCESS, buffer.position(), buffer.remaining()),
                "hash(ReadAccess ByteBuffer)");
    }

    private static void exerciseNegativePrimitiveHashes(LongHashFunction function) {
        byte[] bytes = new byte[8];
        Arrays.fill(bytes, (byte) -1);
        long byteHash = function.hashBytes(bytes, 0, 1);
        long shortHash = function.hashBytes(bytes, 0, 2);
        final long intHash = function.hashBytes(bytes, 0, 4);
        final long longHash = function.hashBytes(bytes, 0, 8);

        assertEquals(byteHash, function.hashByte((byte) -1), "hashByte(-1)");
        assertEquals(shortHash, function.hashShort((short) -1), "hashShort(-1)");
        assertEquals(shortHash, function.hashChar((char) -1), "hashChar(-1)");
        assertEquals(intHash, function.hashInt(-1), "hashInt(-1)");
        assertEquals(longHash, function.hashLong(-1L), "hashLong(-1)");
    }

    private static byte[] dataWithPadding(ByteBuffer source, int len) {
        byte[] copy = new byte[len];
        ByteBuffer duplicate = source.duplicate();
        duplicate.position(0);
        duplicate.get(copy, 0, len);
        return copy;
    }

    private static byte[] padArray(byte[] original, int pad) {
        byte[] copy = new byte[original.length + 2 * pad];
        System.arraycopy(original, 0, copy, pad, original.length);
        return copy;
    }

    private static short[] padArray(short[] original, short pad) {
        short[] copy = new short[original.length + 2];
        System.arraycopy(original, 0, copy, 1, original.length);
        copy[0] = pad;
        copy[copy.length - 1] = pad;
        return copy;
    }

    private static char[] padArray(char[] original, char pad) {
        char[] copy = new char[original.length + 2];
        System.arraycopy(original, 0, copy, 1, original.length);
        copy[0] = pad;
        copy[copy.length - 1] = pad;
        return copy;
    }

    private static int[] padArray(int[] original, int pad) {
        int[] copy = new int[original.length + 2];
        System.arraycopy(original, 0, copy, 1, original.length);
        copy[0] = pad;
        copy[copy.length - 1] = pad;
        return copy;
    }

    private static long[] padArray(long[] original, long pad) {
        long[] copy = new long[original.length + 2];
        System.arraycopy(original, 0, copy, 1, original.length);
        copy[0] = pad;
        copy[copy.length - 1] = pad;
        return copy;
    }

    private static short[] toShortArray(ByteBuffer buffer, int len) {
        short[] shorts = new short[len / 2];
        buffer.duplicate().order(nativeOrder()).asShortBuffer().get(shorts);
        return shorts;
    }

    private static char[] toCharArray(ByteBuffer buffer, int len) {
        char[] chars = new char[len / 2];
        buffer.duplicate().order(nativeOrder()).asCharBuffer().get(chars);
        return chars;
    }

    private static int[] toIntArray(ByteBuffer buffer, int len) {
        int[] ints = new int[len / 4];
        buffer.duplicate().order(nativeOrder()).asIntBuffer().get(ints);
        return ints;
    }

    private static long[] toLongArray(ByteBuffer buffer, int len) {
        long[] longs = new long[len / 8];
        buffer.duplicate().order(nativeOrder()).asLongBuffer().get(longs);
        return longs;
    }
}
