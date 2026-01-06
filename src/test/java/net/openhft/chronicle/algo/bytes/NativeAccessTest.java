/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo.bytes;

import net.openhft.chronicle.core.Jvm;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.*;

class NativeAccessTest {

    private final Accessor.Full<byte[], byte[]> byteAccessor = Accessor.byteArrayAccessor();
    private final Access<byte[]> byteAccess = byteAccessor.access();

    @Test
    void arrayAccessReturnsSameHandleInstances() {
        NativeAccess<Object> unsafe = NativeAccess.instance();
        NativeAccess<Object> second = NativeAccess.instance();
        assertNotNull(unsafe, "NativeAccess.instance() returns non-null");
        assertEquals(unsafe, second, "NativeAccess.instance() returns singleton");
    }

    @Test
    void byteArrayReadsRespectNativeOrder() {
        byte[] sample = {
                (byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78,
                (byte) 0x9A, (byte) 0xBC, (byte) 0xDE, (byte) 0xF0
        };
        ByteBuffer expected = ByteBuffer.wrap(sample).order(ByteOrder.nativeOrder());

        long offset = byteAccessor.offset(sample, 0);
        assertEquals(expected.getLong(0), byteAccess.readLong(sample, offset),
                "readLong from byte array offset 0 should match native-order ByteBuffer.getLong(0)");
        assertEquals(expected.getInt(4), byteAccess.readInt(sample, offset + 4),
                "readInt from byte array offset 4 should match native-order ByteBuffer.getInt(4)");
        assertEquals(expected.getShort(2), byteAccess.readShort(sample, offset + 2),
                "readShort from byte array offset 2 should match native-order ByteBuffer.getShort(2)");
        assertEquals(expected.get(5), byteAccess.readByte(sample, offset + 5),
                "readByte from byte array offset 5 should match native-order ByteBuffer.get(5)");
        assertEquals(Byte.toUnsignedInt(sample[6]),
                byteAccess.readUnsignedByte(sample, offset + 6),
                "readUnsignedByte from byte array offset 6 should match unsigned conversion of sample[6]");
        assertEquals(Short.toUnsignedInt(expected.getShort(4)),
                byteAccess.readUnsignedShort(sample, offset + 4),
                "readUnsignedShort from byte array offset 4 should match unsigned conversion of native-order short");
        assertEquals(Integer.toUnsignedLong(expected.getInt(0)),
                byteAccess.readUnsignedInt(sample, offset),
                "readUnsignedInt from byte array offset 0 should match unsigned conversion of native-order int");
    }

    @Test
    void unalignedReadsMatchNativeByteBuffer() {
        byte[] padded = new byte[9];
        System.arraycopy(new byte[]{
                (byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78,
                (byte) 0x9A, (byte) 0xBC, (byte) 0xDE, (byte) 0xF0
        }, 0, padded, 1, 8);

        ByteBuffer expected = ByteBuffer.wrap(padded).order(ByteOrder.nativeOrder());
        long offset = byteAccessor.offset(padded, 1);
        assertEquals(expected.getLong(1), byteAccess.readLong(padded, offset),
                "readLong from unaligned byte array offset 1 should match native-order ByteBuffer.getLong(1)");
        assertEquals(Integer.toUnsignedLong(expected.getInt(1)),
                byteAccess.readUnsignedInt(padded, offset),
                "readUnsignedInt from unaligned byte array offset 1 should match unsigned conversion of native-order int");
    }

    @Test
    void primitiveArrayAccessReadsValues() {
        long[] longs = {0xFEDCBA9876543210L, 0x123456789ABCDEFL};
        Accessor.Full<long[], long[]> longAccessor = Accessor.longArrayAccessor();
        Access<long[]> longAccess = longAccessor.access();
        assertEquals(longs[0],
                longAccess.readLong(longs, longAccessor.offset(longs, 0)),
                "readLong via long array accessor at index 0 should return 0xFEDCBA9876543210L");
        assertEquals(longs[1],
                longAccess.readLong(longs, longAccessor.offset(longs, 1)),
                "readLong via long array accessor at index 1 should return 0x123456789ABCDEFL");

        int[] ints = {0xCAFEBABE, 0xDEADBEEF};
        Accessor.Full<int[], int[]> intAccessor = Accessor.intArrayAccessor();
        Access<int[]> intAccess = intAccessor.access();
        assertEquals(ints[0],
                intAccess.readInt(ints, intAccessor.offset(ints, 0)),
                "readInt via int array accessor at index 0 should return 0xCAFEBABE");
        assertEquals(ints[1],
                intAccess.readInt(ints, intAccessor.offset(ints, 1)),
                "readInt via int array accessor at index 1 should return 0xDEADBEEF");

        short[] shorts = {(short) 0xBEEF, (short) 0xCAFE};
        Accessor.Full<short[], short[]> shortAccessor = Accessor.shortArrayAccessor();
        Access<short[]> shortAccess = shortAccessor.access();
        assertEquals(shorts[0],
                shortAccess.readShort(shorts, shortAccessor.offset(shorts, 0)),
                "readShort via short array accessor at index 0 should return (short) 0xBEEF");
        assertEquals(shorts[1],
                shortAccess.readShort(shorts, shortAccessor.offset(shorts, 1)),
                "readShort via short array accessor at index 1 should return (short) 0xCAFE");

        char[] chars = {(char) 0xF00D, (char) 0xBABE};
        Accessor.Full<char[], char[]> charAccessor = Accessor.charArrayAccessor();
        Access<char[]> charAccess = charAccessor.access();
        assertEquals(chars[0],
                charAccess.readChar(chars, charAccessor.offset(chars, 0)),
                "readChar via char array accessor at index 0 should return (char) 0xF00D");
        assertEquals(chars[1],
                charAccess.readChar(chars, charAccessor.offset(chars, 1)),
                "readChar via char array accessor at index 1 should return (char) 0xBABE");
    }

    @Test
    void compareAndSwapLongSupportsArrayBases() {
        long[] longs = {0L};
        Accessor.Full<long[], long[]> longAccessor = Accessor.longArrayAccessor();
        Access<long[]> longAccess = longAccessor.access();
        long offset = longAccessor.offset(longs, 0);
        assertEquals(0L, longAccess.readLong(longs, offset),
                "Initial value at long array index 0 should be 0L");
        longAccess.writeLong(longs, offset, 42L);
        assertEquals(42L, longAccess.readLong(longs, offset),
                "After writeLong(42L), value at long array index 0 should be 42L");
        // A successful CAS should set the value to 64
        boolean swapped = longAccess.compareAndSwapLong(longs, offset, 42L, 64L);
        assertTrue(swapped,
                "compareAndSwapLong(42L, 64L) should succeed when current value is 42L");
        assertEquals(64L, longAccess.readLong(longs, offset),
                "After successful CAS operation, value at long array index 0 should be 64L");
    }

    @Test
    void readViaAccessInterfaceMatchesNativeOffset() {
        byte[] data = loopingBytes(16);
        long baseOffset = Jvm.arrayByteBaseOffset();
        assertEquals(byteAccess.readInt(data, baseOffset + 4),
                ByteBuffer.wrap(data).order(ByteOrder.nativeOrder()).getInt(4),
                "readInt via Access interface using native base offset + 4 should match native-order ByteBuffer.getInt(4)");
    }

    private static byte[] loopingBytes(int len) {
        byte[] data = new byte[len];
        for (int i = 0; i < len; i++) {
            data[i] = (byte) i;
        }
        return data;
    }
}
