//
// Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

package net.openhft.chronicle.algo.bytes;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.Maths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.Assume.assumeFalse;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
class WriteAccessTest {

    private WriteAccess<byte[]> writeAccess;
    private byte[] handle;

    @BeforeEach
    void setUp() {
        assumeFalse(Jvm.isJava21Plus());
        writeAccess = Mockito.spy(WriteAccess.class);
        handle = new byte[16];

        // Mock behavior for writeByte
        doAnswer(invocation -> {
            byte[] h = invocation.getArgument(0);
            long offset = invocation.getArgument(1);
            byte value = invocation.getArgument(2);
            h[(int) offset] = value;
            return null;
        }).when(writeAccess).writeByte(any(byte[].class), anyLong(), anyByte());

        // Mock behavior for writeShort
        doAnswer(invocation -> {
            byte[] h = invocation.getArgument(0);
            long offset = invocation.getArgument(1);
            short value = invocation.getArgument(2);
            h[(int) offset] = (byte) (value >> 8);
            h[(int) offset + 1] = (byte) value;
            return null;
        }).when(writeAccess).writeShort(any(byte[].class), anyLong(), anyShort());

        // Mock behavior for writeInt
        doAnswer(invocation -> {
            byte[] h = invocation.getArgument(0);
            long offset = invocation.getArgument(1);
            int value = invocation.getArgument(2);
            h[(int) offset] = (byte) (value >> 24);
            h[(int) offset + 1] = (byte) (value >> 16);
            h[(int) offset + 2] = (byte) (value >> 8);
            h[(int) offset + 3] = (byte) value;
            return null;
        }).when(writeAccess).writeInt(any(byte[].class), anyLong(), anyInt());

        // Mock behavior for writeLong
        doAnswer(invocation -> {
            byte[] h = invocation.getArgument(0);
            long offset = invocation.getArgument(1);
            long value = invocation.getArgument(2);
            h[(int) offset] = (byte) (value >> 56);
            h[(int) offset + 1] = (byte) (value >> 48);
            h[(int) offset + 2] = (byte) (value >> 40);
            h[(int) offset + 3] = (byte) (value >> 32);
            h[(int) offset + 4] = (byte) (value >> 24);
            h[(int) offset + 5] = (byte) (value >> 16);
            h[(int) offset + 6] = (byte) (value >> 8);
            h[(int) offset + 7] = (byte) value;
            return null;
        }).when(writeAccess).writeLong(any(byte[].class), anyLong(), anyLong());

        // Mock behavior for writeFloat
        doAnswer(invocation -> {
            byte[] h = invocation.getArgument(0);
            long offset = invocation.getArgument(1);
            float value = invocation.getArgument(2);
            int intValue = Float.floatToIntBits(value);
            h[(int) offset] = (byte) (intValue >> 24);
            h[(int) offset + 1] = (byte) (intValue >> 16);
            h[(int) offset + 2] = (byte) (intValue >> 8);
            h[(int) offset + 3] = (byte) intValue;
            return null;
        }).when(writeAccess).writeFloat(any(byte[].class), anyLong(), anyFloat());

        // Mock behavior for writeDouble
        doAnswer(invocation -> {
            byte[] h = invocation.getArgument(0);
            long offset = invocation.getArgument(1);
            double value = invocation.getArgument(2);
            long longValue = Double.doubleToLongBits(value);
            h[(int) offset] = (byte) (longValue >> 56);
            h[(int) offset + 1] = (byte) (longValue >> 48);
            h[(int) offset + 2] = (byte) (longValue >> 40);
            h[(int) offset + 3] = (byte) (longValue >> 32);
            h[(int) offset + 4] = (byte) (longValue >> 24);
            h[(int) offset + 5] = (byte) (longValue >> 16);
            h[(int) offset + 6] = (byte) (longValue >> 8);
            h[(int) offset + 7] = (byte) longValue;
            return null;
        }).when(writeAccess).writeDouble(any(byte[].class), anyLong(), anyDouble());
    }

    @Test
    void testWriteByte() {
        writeAccess.writeByte(handle, 0, (byte) 0x7F);
        assertArrayEquals(new byte[]{0x7F, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, handle);
    }

    @Test
    void testWriteUnsignedByte() {
        byte[] expected = new byte[16];  // Ensure expected array has the correct length
        expected[0] = (byte) Maths.toUInt8(0xFF);

        writeAccess.writeUnsignedByte(handle, 0, 0xFF);
        verify(writeAccess).writeByte(handle, 0, (byte) Maths.toUInt8(0xFF));
        assertArrayEquals(expected, handle);
    }

    @Test
    void testWriteBoolean() {
        writeAccess.writeBoolean(handle, 0, true);
        verify(writeAccess).writeByte(handle, 0, (byte) 'Y');
        assertArrayEquals(new byte[]{'Y', 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, handle);

        writeAccess.writeBoolean(handle, 0, false);
        verify(writeAccess).writeByte(handle, 0, (byte) 0);
        assertArrayEquals(new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, handle);
    }

    @Test
    void testWriteUnsignedShort() {
        writeAccess.writeUnsignedShort(handle, 0, 0xFFFF);
        verify(writeAccess).writeShort(handle, 0, (short) Maths.toUInt16(0xFFFF));
        assertArrayEquals(new byte[]{(byte) 0xFF, (byte) 0xFF, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, handle);
    }

    @Test
    void testWriteChar() {
        byte[] expected = new byte[16];  // Ensure expected array has the correct length
        expected[0] = 0x00;
        expected[1] = 0x41;

        writeAccess.writeChar(handle, 0, 'A');
        verify(writeAccess).writeShort(handle, 0, (short) 'A');
        assertArrayEquals(expected, handle);
    }

    @Test
    void testWriteUnsignedInt() {
        byte[] expected = new byte[16];  // Ensure expected array has the correct length
        expected[0] = (byte) 0xFF;
        expected[1] = (byte) 0xFF;
        expected[2] = (byte) 0xFF;
        expected[3] = (byte) 0xFF;

        writeAccess.writeUnsignedInt(handle, 0, 0xFFFFFFFFL);
        verify(writeAccess).writeInt(handle, 0, (int) Maths.toUInt32(0xFFFFFFFFL));
        assertArrayEquals(expected, handle);
    }

    @Test
    void testWriteInt() {
        byte[] expected = new byte[16];  // Ensure expected array has the correct length
        expected[0] = 0x12;
        expected[1] = 0x34;
        expected[2] = 0x56;
        expected[3] = 0x78;

        writeAccess.writeInt(handle, 0, 0x12345678);
        verify(writeAccess).writeInt(handle, 0, 0x12345678);
        assertArrayEquals(expected, handle);
    }

    @Test
    void testWriteLong() {
        byte[] expected = new byte[16];  // Ensure expected array has the correct length
        expected[0] = 0x12;
        expected[1] = 0x34;
        expected[2] = 0x56;
        expected[3] = 0x78;
        expected[4] = (byte) 0x9A;
        expected[5] = (byte) 0xBC;
        expected[6] = (byte) 0xDE;
        expected[7] = (byte) 0xF0;

        writeAccess.writeLong(handle, 0, 0x123456789ABCDEF0L);
        verify(writeAccess).writeLong(handle, 0, 0x123456789ABCDEF0L);
        assertArrayEquals(expected, handle);
    }

    @Test
    void testWriteFloat() {
        byte[] expected = new byte[16];  // Ensure expected array has the correct length
        int intValue = Float.floatToIntBits(1.0f);
        expected[0] = (byte) (intValue >> 24);
        expected[1] = (byte) (intValue >> 16);
        expected[2] = (byte) (intValue >> 8);
        expected[3] = (byte) intValue;

        writeAccess.writeFloat(handle, 0, 1.0f);
        verify(writeAccess).writeFloat(handle, 0, 1.0f);
        assertArrayEquals(expected, handle);
    }

    @Test
    void testWriteDouble() {
        byte[] expected = new byte[16];  // Ensure expected array has the correct length
        long longValue = Double.doubleToLongBits(1.0);
        expected[0] = (byte) (longValue >> 56);
        expected[1] = (byte) (longValue >> 48);
        expected[2] = (byte) (longValue >> 40);
        expected[3] = (byte) (longValue >> 32);
        expected[4] = (byte) (longValue >> 24);
        expected[5] = (byte) (longValue >> 16);
        expected[6] = (byte) (longValue >> 8);
        expected[7] = (byte) longValue;

        writeAccess.writeDouble(handle, 0, 1.0);
        verify(writeAccess).writeDouble(handle, 0, 1.0);
        assertArrayEquals(expected, handle);
    }

    @Test
    void testWriteBytes() {
        byte[] expected = new byte[16];  // Ensure expected array has the correct length
        for (int i = 0; i < 16; i++) {
            expected[i] = (byte) 0xAA;
        }

        writeAccess.writeBytes(handle, 0, 16, (byte) 0xAA);
        for (int i = 0; i < 16; i += 8) {
            verify(writeAccess).writeLong(handle, i, 0xAAAAAAAAAAAAAAAAL);
        }
        assertArrayEquals(expected, handle);
    }

    @Test
    void testZeroOut() {
        byte[] expected = new byte[16];  // Ensure expected array has the correct length

        writeAccess.zeroOut(handle, 0, 16);
        for (int i = 0; i < 16; i += 8) {
            verify(writeAccess).writeLong(handle, i, 0L);
        }
        assertArrayEquals(expected, handle);
    }
}
