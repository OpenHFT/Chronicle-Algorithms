/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo.bytes;

import org.junit.jupiter.api.Test;

import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.*;

class ZeroAccessTest {

    private final ZeroAccess zeroAccess = ZeroAccess.INSTANCE;

    @Test
    void testReadBoolean() {
        assertFalse(zeroAccess.readBoolean(null, 0L));
    }

    @Test
    void testReadByte() {
        assertEquals(0, zeroAccess.readByte(null, 0L));
    }

    @Test
    void testReadUnsignedByte() {
        assertEquals(0, zeroAccess.readUnsignedByte(null, 0L));
    }

    @Test
    void testReadShort() {
        assertEquals(0, zeroAccess.readShort(null, 0L));
    }

    @Test
    void testReadUnsignedShort() {
        assertEquals(0, zeroAccess.readUnsignedShort(null, 0L));
    }

    @Test
    void testReadChar() {
        assertEquals(0, zeroAccess.readChar(null, 0L));
    }

    @Test
    void testReadInt() {
        assertEquals(0, zeroAccess.readInt(null, 0L));
    }

    @Test
    void testReadUnsignedInt() {
        assertEquals(0L, zeroAccess.readUnsignedInt(null, 0L));
    }

    @Test
    void testReadLong() {
        assertEquals(0L, zeroAccess.readLong(null, 0L));
    }

    @Test
    void testReadFloat() {
        assertEquals(0.0f, zeroAccess.readFloat(null, 0L), 0.0);
    }

    @Test
    void testReadDouble() {
        assertEquals(0.0, zeroAccess.readDouble(null, 0L), 0.0);
    }

    @Test
    void testPrintable() {
        assertEquals("\u0660", zeroAccess.printable(null, 0L));
    }

    @Test
    void testReadVolatileInt() {
        assertEquals(0, zeroAccess.readVolatileInt(null, 0L));
    }

    @Test
    void testReadVolatileLong() {
        assertEquals(0L, zeroAccess.readVolatileLong(null, 0L));
    }

    @Test
    void testByteOrder() {
        assertSame(ByteOrder.nativeOrder(), zeroAccess.byteOrder(null));
    }
}
