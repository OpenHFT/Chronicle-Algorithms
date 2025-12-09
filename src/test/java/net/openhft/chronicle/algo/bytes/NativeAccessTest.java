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
        assertNotNull(unsafe);
        assertEquals(unsafe, second);
    }

    @Test
    void byteArrayReadsRespectNativeOrder() {
        byte[] sample = {
                (byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78,
                (byte) 0x9A, (byte) 0xBC, (byte) 0xDE, (byte) 0xF0
        };
        ByteBuffer expected = ByteBuffer.wrap(sample).order(ByteOrder.nativeOrder());

        long offset = byteAccessor.offset(sample, 0);
        assertEquals(expected.getLong(0), byteAccess.readLong(sample, offset));
        assertEquals(expected.getInt(4), byteAccess.readInt(sample, offset + 4));
        assertEquals(expected.getShort(2), byteAccess.readShort(sample, offset + 2));
        assertEquals(expected.get(5), byteAccess.readByte(sample, offset + 5));
        assertEquals(Byte.toUnsignedInt(sample[6]), byteAccess.readUnsignedByte(sample, offset + 6));
        assertEquals(Short.toUnsignedInt(expected.getShort(4)),
                byteAccess.readUnsignedShort(sample, offset + 4));
        assertEquals(Integer.toUnsignedLong(expected.getInt(0)),
                byteAccess.readUnsignedInt(sample, offset));
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
        assertEquals(expected.getLong(1), byteAccess.readLong(padded, offset));
        assertEquals(Integer.toUnsignedLong(expected.getInt(1)),
                byteAccess.readUnsignedInt(padded, offset));
    }

    @Test
    void primitiveArrayAccessReadsValues() {
        long[] longs = {0xFEDCBA9876543210L, 0x123456789ABCDEFL};
        Accessor.Full<long[], long[]> longAccessor = Accessor.longArrayAccessor();
        Access<long[]> longAccess = longAccessor.access();
        assertEquals(longs[0], longAccess.readLong(longs, longAccessor.offset(longs, 0)));
        assertEquals(longs[1], longAccess.readLong(longs, longAccessor.offset(longs, 1)));

        int[] ints = {0xCAFEBABE, 0xDEADBEEF};
        Accessor.Full<int[], int[]> intAccessor = Accessor.intArrayAccessor();
        Access<int[]> intAccess = intAccessor.access();
        assertEquals(ints[0], intAccess.readInt(ints, intAccessor.offset(ints, 0)));
        assertEquals(ints[1], intAccess.readInt(ints, intAccessor.offset(ints, 1)));

        short[] shorts = {(short) 0xBEEF, (short) 0xCAFE};
        Accessor.Full<short[], short[]> shortAccessor = Accessor.shortArrayAccessor();
        Access<short[]> shortAccess = shortAccessor.access();
        assertEquals(shorts[0], shortAccess.readShort(shorts, shortAccessor.offset(shorts, 0)));
        assertEquals(shorts[1], shortAccess.readShort(shorts, shortAccessor.offset(shorts, 1)));

        char[] chars = {(char) 0xF00D, (char) 0xBABE};
        Accessor.Full<char[], char[]> charAccessor = Accessor.charArrayAccessor();
        Access<char[]> charAccess = charAccessor.access();
        assertEquals(chars[0], charAccess.readChar(chars, charAccessor.offset(chars, 0)));
        assertEquals(chars[1], charAccess.readChar(chars, charAccessor.offset(chars, 1)));
    }

    @Test
    void compareAndSwapLongSupportsArrayBases() {
        long[] longs = {0L};
        Accessor.Full<long[], long[]> longAccessor = Accessor.longArrayAccessor();
        Access<long[]> longAccess = longAccessor.access();
        long offset = longAccessor.offset(longs, 0);
        assertEquals(0L, longAccess.readLong(longs, offset));
        longAccess.writeLong(longs, offset, 42L);
        assertEquals(42L, longAccess.readLong(longs, offset));
        // A successful CAS should set the value to 64
        boolean swapped = longAccess.compareAndSwapLong(longs, offset, 42L, 64L);
        assertTrue(swapped);
        assertEquals(64L, longAccess.readLong(longs, offset));
    }

    @Test
    void readViaAccessInterfaceMatchesNativeOffset() {
        byte[] data = loopingBytes(16);
        long baseOffset = Jvm.arrayByteBaseOffset();
        assertEquals(byteAccess.readInt(data, baseOffset + 4),
                ByteBuffer.wrap(data).order(ByteOrder.nativeOrder()).getInt(4));
    }

    private static byte[] loopingBytes(int len) {
        byte[] data = new byte[len];
        for (int i = 0; i < len; i++) {
            data[i] = (byte) i;
        }
        return data;
    }
}
