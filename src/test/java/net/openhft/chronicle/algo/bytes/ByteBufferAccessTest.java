/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo.bytes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ByteBufferAccessTest {
    private ByteBufferAccess access;
    private ByteBuffer buffer;

    @BeforeEach
    public void setUp() {
        access = ByteBufferAccess.INSTANCE;
        buffer = ByteBuffer.allocate(64).order(ByteOrder.LITTLE_ENDIAN);
    }

    @Test
    public void testReadByte() {
        buffer.put(0, (byte) 123);
        assertEquals(123, access.readByte(buffer, 0));
    }

    @Test
    public void testReadShort() {
        buffer.putShort(0, (short) 12345);
        assertEquals(12345, access.readShort(buffer, 0));
    }

    @Test
    public void testReadChar() {
        buffer.putChar(0, 'a');
        assertEquals('a', access.readChar(buffer, 0));
    }

    @Test
    public void testReadInt() {
        buffer.putInt(0, 123456789);
        assertEquals(123456789, access.readInt(buffer, 0));
    }

    @Test
    public void testReadLong() {
        buffer.putLong(0, 1234567890123456789L);
        assertEquals(1234567890123456789L, access.readLong(buffer, 0));
    }

    @Test
    public void testReadFloat() {
        buffer.putFloat(0, 12345.6789f);
        assertEquals(12345.6789f, access.readFloat(buffer, 0), 0.0);
    }

    @Test
    public void testReadDouble() {
        buffer.putDouble(0, 1234567890.123456789);
        assertEquals(1234567890.123456789, access.readDouble(buffer, 0), 0.0);
    }

    @Test
    public void testWriteByte() {
        access.writeByte(buffer, 0, (byte) 123);
        assertEquals(123, buffer.get(0));
    }

    @Test
    public void testWriteShort() {
        access.writeShort(buffer, 0, (short) 12345);
        assertEquals(12345, buffer.getShort(0));
    }

    @Test
    public void testWriteChar() {
        access.writeChar(buffer, 0, 'a');
        assertEquals('a', buffer.getChar(0));
    }

    @Test
    public void testWriteInt() {
        access.writeInt(buffer, 0, 123456789);
        assertEquals(123456789, buffer.getInt(0));
    }

    @Test
    public void testWriteLong() {
        access.writeLong(buffer, 0, 1234567890123456789L);
        assertEquals(1234567890123456789L, buffer.getLong(0));
    }

    @Test
    public void testWriteFloat() {
        access.writeFloat(buffer, 0, 12345.6789f);
        assertEquals(12345.6789f, buffer.getFloat(0), 0.0);
    }

    @Test
    public void testWriteDouble() {
        access.writeDouble(buffer, 0, 1234567890.123456789);
        assertEquals(1234567890.123456789, buffer.getDouble(0), 0.0);
    }
}