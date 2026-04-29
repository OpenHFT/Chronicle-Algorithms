/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo.bytes;

import net.openhft.chronicle.bytes.RandomDataInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.ByteOrder;

import static org.mockito.Mockito.*;

public class RandomDataInputAccessTest {
    private RandomDataInput mockInput;
    private RandomDataInputAccess<RandomDataInput> access;

    @BeforeEach
    public void setUp() {
        mockInput = mock(RandomDataInput.class);
        access = new RandomDataInputAccess<RandomDataInput>() {
            // This class remains empty as the methods are default in the interface
        };
    }

    @Test
    public void testReadBoolean() {
        when(mockInput.readBoolean(0L)).thenReturn(true);
        assertTrue(access.readBoolean(mockInput, 0L));
        verify(mockInput).readBoolean(0L);
    }

    @Test
    public void testReadByte() {
        when(mockInput.readByte(0L)).thenReturn((byte) 1);
        assertEquals((byte) 1, access.readByte(mockInput, 0L));
        verify(mockInput).readByte(0L);
    }

    @Test
    public void testReadUnsignedByte() {
        when(mockInput.readUnsignedByte(0L)).thenReturn(1);
        assertEquals(1, access.readUnsignedByte(mockInput, 0L));
        verify(mockInput).readUnsignedByte(0L);
    }

    @Test
    public void testReadShort() {
        when(mockInput.readShort(0L)).thenReturn((short) 2);
        assertEquals((short) 2, access.readShort(mockInput, 0L));
        verify(mockInput).readShort(0L);
    }

    @Test
    public void testReadUnsignedShort() {
        when(mockInput.readUnsignedShort(0L)).thenReturn(2);
        assertEquals(2, access.readUnsignedShort(mockInput, 0L));
        verify(mockInput).readUnsignedShort(0L);
    }

    @Test
    public void testReadInt() {
        when(mockInput.readInt(0L)).thenReturn(3);
        assertEquals(3, access.readInt(mockInput, 0L));
        verify(mockInput).readInt(0L);
    }

    @Test
    public void testReadUnsignedInt() {
        when(mockInput.readUnsignedInt(0L)).thenReturn(3L);
        assertEquals(3L, access.readUnsignedInt(mockInput, 0L));
        verify(mockInput).readUnsignedInt(0L);
    }

    @Test
    public void testReadLong() {
        when(mockInput.readLong(0L)).thenReturn(4L);
        assertEquals(4L, access.readLong(mockInput, 0L));
        verify(mockInput).readLong(0L);
    }

    @Test
    public void testReadFloat() {
        when(mockInput.readFloat(0L)).thenReturn(5.0f);
        assertEquals(5.0f, access.readFloat(mockInput, 0L), 0.0);
        verify(mockInput).readFloat(0L);
    }

    @Test
    public void testReadDouble() {
        when(mockInput.readDouble(0L)).thenReturn(6.0);
        assertEquals(6.0, access.readDouble(mockInput, 0L), 0.0);
        verify(mockInput).readDouble(0L);
    }

    @Test
    public void testPrintable() {
        when(mockInput.printable(0L)).thenReturn("test");
        assertEquals("test", access.printable(mockInput, 0L));
        verify(mockInput).printable(0L);
    }

    @Test
    public void testReadVolatileInt() {
        when(mockInput.readVolatileInt(0L)).thenReturn(7);
        assertEquals(7, access.readVolatileInt(mockInput, 0L));
        verify(mockInput).readVolatileInt(0L);
    }

    @Test
    public void testReadVolatileLong() {
        when(mockInput.readVolatileLong(0L)).thenReturn(8L);
        assertEquals(8L, access.readVolatileLong(mockInput, 0L));
        verify(mockInput).readVolatileLong(0L);
    }

    @Test
    public void testByteOrder() {
        when(mockInput.byteOrder()).thenReturn(ByteOrder.BIG_ENDIAN);
        assertSame(ByteOrder.BIG_ENDIAN, access.byteOrder(mockInput));
        verify(mockInput).byteOrder();
    }
}
