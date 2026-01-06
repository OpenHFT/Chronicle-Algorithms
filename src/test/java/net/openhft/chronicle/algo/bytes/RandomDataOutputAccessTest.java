/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo.bytes;

import net.openhft.chronicle.bytes.RandomDataOutput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class RandomDataOutputAccessTest {
    private RandomDataOutputImpl mockOutput;
    private RandomDataOutputAccess<RandomDataOutputImpl> access;

    @BeforeEach
    void setUp() {
        mockOutput = mock(RandomDataOutputImpl.class);
        access = new RandomDataOutputAccess<RandomDataOutputImpl>() {
            // This class remains empty as the methods are default in the interface
        };
    }

    @Test
    void testWriteByte() {
        access.writeByte(mockOutput, 0L, 1);
        verify(mockOutput).writeByte(0L, 1);
    }

    @Test
    void testWriteUnsignedByte() {
        access.writeUnsignedByte(mockOutput, 0L, 1);
        verify(mockOutput).writeUnsignedByte(0L, 1);
    }

    @Test
    void testWriteBoolean() {
        access.writeBoolean(mockOutput, 0L, true);
        verify(mockOutput).writeBoolean(0L, true);
    }

    @Test
    void testWriteUnsignedShort() {
        access.writeUnsignedShort(mockOutput, 0L, 1);
        verify(mockOutput).writeUnsignedShort(0L, 1);
    }

    @Test
    void testWriteUnsignedInt() {
        access.writeUnsignedInt(mockOutput, 0L, 1L);
        verify(mockOutput).writeUnsignedInt(0L, 1L);
    }

    @Test
    void testWriteByteOverload() {
        access.writeByte(mockOutput, 0L, (byte) 1);
        verify(mockOutput).writeByte(0L, (byte) 1);
    }

    @Test
    void testWriteShort() {
        access.writeShort(mockOutput, 0L, (short) 1);
        verify(mockOutput).writeShort(0L, (short) 1);
    }

    @Test
    void testWriteInt() {
        access.writeInt(mockOutput, 0L, 1);
        verify(mockOutput).writeInt(0L, 1);
    }

    @Test
    void testWriteOrderedInt() {
        access.writeOrderedInt(mockOutput, 0L, 1);
        verify(mockOutput).writeOrderedInt(0L, 1);
    }

    @Test
    void testWriteLong() {
        access.writeLong(mockOutput, 0L, 1L);
        verify(mockOutput).writeLong(0L, 1L);
    }

    @Test
    void testWriteOrderedLong() {
        access.writeOrderedLong(mockOutput, 0L, 1L);
        verify(mockOutput).writeOrderedLong(0L, 1L);
    }

    @Test
    void testWriteFloat() {
        access.writeFloat(mockOutput, 0L, 1.0f);
        verify(mockOutput).writeFloat(0L, 1.0f);
    }

    @Test
    void testWriteDouble() {
        access.writeDouble(mockOutput, 0L, 1.0);
        verify(mockOutput).writeDouble(0L, 1.0);
    }

    @Test
    void testByteOrder() {
        when(mockOutput.byteOrder()).thenReturn(ByteOrder.BIG_ENDIAN);
        assertEquals(ByteOrder.BIG_ENDIAN, access.byteOrder(mockOutput), "byteOrder delegates to output");
        verify(mockOutput).byteOrder();
    }

    // Mock class extending RandomDataOutput with self-referential generic type
    abstract static class RandomDataOutputImpl implements RandomDataOutput<RandomDataOutputImpl> {
    }
}
