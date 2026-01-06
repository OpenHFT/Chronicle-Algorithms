/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo.bytes;

import org.junit.jupiter.api.Test;

import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ZeroAccessTest {

    private final ZeroAccess zeroAccess = ZeroAccess.INSTANCE;

    @Test
    void testReadBoolean() {
        assertFalse(zeroAccess.readBoolean(null, 0L), "readBoolean returns false");
    }

    @Test
    void testReadByte() {
        assertEquals(0, zeroAccess.readByte(null, 0L), "readByte returns 0");
    }

    @Test
    void testReadUnsignedByte() {
        assertEquals(0, zeroAccess.readUnsignedByte(null, 0L), "readUnsignedByte returns 0");
    }

    @Test
    void testReadShort() {
        assertEquals(0, zeroAccess.readShort(null, 0L), "readShort returns 0");
    }

    @Test
    void testReadUnsignedShort() {
        assertEquals(0, zeroAccess.readUnsignedShort(null, 0L), "readUnsignedShort returns 0");
    }

    @Test
    void testReadChar() {
        assertEquals(0, zeroAccess.readChar(null, 0L), "readChar returns 0");
    }

    @Test
    void testReadInt() {
        assertEquals(0, zeroAccess.readInt(null, 0L), "readInt returns 0");
    }

    @Test
    void testReadUnsignedInt() {
        assertEquals(0L, zeroAccess.readUnsignedInt(null, 0L), "readUnsignedInt returns 0");
    }

    @Test
    void testReadLong() {
        assertEquals(0L, zeroAccess.readLong(null, 0L), "readLong returns 0");
    }

    @Test
    void testReadFloat() {
        assertEquals(0.0f, zeroAccess.readFloat(null, 0L), 0.0f, "readFloat returns 0");
    }

    @Test
    void testReadDouble() {
        assertEquals(0.0, zeroAccess.readDouble(null, 0L), 0.0, "readDouble returns 0");
    }

    @Test
    void testPrintable() {
        assertEquals("\u0660", zeroAccess.printable(null, 0L), "printable returns '0' glyph");
    }

    @Test
    void testReadVolatileInt() {
        assertEquals(0, zeroAccess.readVolatileInt(null, 0L), "readVolatileInt returns 0");
    }

    @Test
    void testReadVolatileLong() {
        assertEquals(0L, zeroAccess.readVolatileLong(null, 0L), "readVolatileLong returns 0");
    }

    @Test
    void testByteOrder() {
        assertEquals(ByteOrder.nativeOrder(), zeroAccess.byteOrder(null), "byteOrder is native");
    }
}
