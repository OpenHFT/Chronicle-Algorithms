package net.openhft.chronicle.algo.bytes;

import org.junit.jupiter.api.Test;
import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.*;

public class ZeroAccessTest {

    private final ZeroAccess zeroAccess = ZeroAccess.INSTANCE;

    @Test
    public void testReadBoolean() {
        assertFalse(zeroAccess.readBoolean(null, 0L));
    }

    @Test
    public void testReadByte() {
        assertEquals(0, zeroAccess.readByte(null, 0L));
    }

    @Test
    public void testReadUnsignedByte() {
        assertEquals(0, zeroAccess.readUnsignedByte(null, 0L));
    }

    @Test
    public void testReadShort() {
        assertEquals(0, zeroAccess.readShort(null, 0L));
    }

    @Test
    public void testReadUnsignedShort() {
        assertEquals(0, zeroAccess.readUnsignedShort(null, 0L));
    }

    @Test
    public void testReadChar() {
        assertEquals(0, zeroAccess.readChar(null, 0L));
    }

    @Test
    public void testReadInt() {
        assertEquals(0, zeroAccess.readInt(null, 0L));
    }

    @Test
    public void testReadUnsignedInt() {
        assertEquals(0L, zeroAccess.readUnsignedInt(null, 0L));
    }

    @Test
    public void testReadLong() {
        assertEquals(0L, zeroAccess.readLong(null, 0L));
    }

    @Test
    public void testReadFloat() {
        assertEquals(0.0f, zeroAccess.readFloat(null, 0L), 0.0);
    }

    @Test
    public void testReadDouble() {
        assertEquals(0.0, zeroAccess.readDouble(null, 0L), 0.0);
    }

    @Test
    public void testPrintable() {
        assertEquals("\u0660", zeroAccess.printable(null, 0L));
    }

    @Test
    public void testReadVolatileInt() {
        assertEquals(0, zeroAccess.readVolatileInt(null, 0L));
    }

    @Test
    public void testReadVolatileLong() {
        assertEquals(0L, zeroAccess.readVolatileLong(null, 0L));
    }

    @Test
    public void testByteOrder() {
        assertEquals(ByteOrder.nativeOrder(), zeroAccess.byteOrder(null));
    }
}
