/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo.bytes;

import net.openhft.chronicle.bytes.RandomDataInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RandomDataInputAccessTest {
    private RandomDataInput mockInput;
    private RandomDataInputAccess<RandomDataInput> access;

    @BeforeEach
    void setUp() {
        mockInput = mock(RandomDataInput.class);
        access = new RandomDataInputAccess<RandomDataInput>() {
            // This class remains empty as the methods are default in the interface
        };
    }

    @Test
    void testReadBoolean() {
        when(mockInput.readBoolean(0L)).thenReturn(true);
        assertTrue(access.readBoolean(mockInput, 0L), "readBoolean delegates to RandomDataInput");
        verify(mockInput).readBoolean(0L);
    }

    @Test
    void testReadByte() {
        when(mockInput.readByte(0L)).thenReturn((byte) 1);
        assertEquals((byte) 1, access.readByte(mockInput, 0L), "readByte delegates to RandomDataInput");
        verify(mockInput).readByte(0L);
    }

    @Test
    void testReadUnsignedByte() {
        when(mockInput.readUnsignedByte(0L)).thenReturn(1);
        assertEquals(1, access.readUnsignedByte(mockInput, 0L), "readUnsignedByte delegates to RandomDataInput");
        verify(mockInput).readUnsignedByte(0L);
    }

    @Test
    void testReadShort() {
        when(mockInput.readShort(0L)).thenReturn((short) 2);
        assertEquals((short) 2, access.readShort(mockInput, 0L), "readShort delegates to RandomDataInput");
        verify(mockInput).readShort(0L);
    }

    @Test
    void testReadUnsignedShort() {
        when(mockInput.readUnsignedShort(0L)).thenReturn(2);
        assertEquals(2, access.readUnsignedShort(mockInput, 0L), "readUnsignedShort delegates to RandomDataInput");
        verify(mockInput).readUnsignedShort(0L);
    }

    @Test
    void testReadInt() {
        when(mockInput.readInt(0L)).thenReturn(3);
        assertEquals(3, access.readInt(mockInput, 0L), "readInt delegates to RandomDataInput");
        verify(mockInput).readInt(0L);
    }

    @Test
    void testReadUnsignedInt() {
        when(mockInput.readUnsignedInt(0L)).thenReturn(3L);
        assertEquals(3L, access.readUnsignedInt(mockInput, 0L), "readUnsignedInt delegates to RandomDataInput");
        verify(mockInput).readUnsignedInt(0L);
    }

    @Test
    void testReadLong() {
        when(mockInput.readLong(0L)).thenReturn(4L);
        assertEquals(4L, access.readLong(mockInput, 0L), "readLong delegates to RandomDataInput");
        verify(mockInput).readLong(0L);
    }

    @Test
    void testReadFloat() {
        when(mockInput.readFloat(0L)).thenReturn(5.0f);
        assertEquals(5.0f, access.readFloat(mockInput, 0L), "readFloat delegates to RandomDataInput");
        verify(mockInput).readFloat(0L);
    }

    @Test
    void testReadDouble() {
        when(mockInput.readDouble(0L)).thenReturn(6.0);
        assertEquals(6.0, access.readDouble(mockInput, 0L), "readDouble delegates to RandomDataInput");
        verify(mockInput).readDouble(0L);
    }

    @Test
    void testPrintable() {
        when(mockInput.printable(0L)).thenReturn("test");
        assertEquals("test", access.printable(mockInput, 0L), "printable delegates to RandomDataInput");
        verify(mockInput).printable(0L);
    }

    @Test
    void testReadVolatileInt() {
        when(mockInput.readVolatileInt(0L)).thenReturn(7);
        assertEquals(7, access.readVolatileInt(mockInput, 0L), "readVolatileInt delegates to RandomDataInput");
        verify(mockInput).readVolatileInt(0L);
    }

    @Test
    void testReadVolatileLong() {
        when(mockInput.readVolatileLong(0L)).thenReturn(8L);
        assertEquals(8L, access.readVolatileLong(mockInput, 0L), "readVolatileLong delegates to RandomDataInput");
        verify(mockInput).readVolatileLong(0L);
    }

    @Test
    void testByteOrder() {
        when(mockInput.byteOrder()).thenReturn(ByteOrder.BIG_ENDIAN);
        assertEquals(ByteOrder.BIG_ENDIAN, access.byteOrder(mockInput), "byteOrder delegates to RandomDataInput");
        verify(mockInput).byteOrder();
    }
}
