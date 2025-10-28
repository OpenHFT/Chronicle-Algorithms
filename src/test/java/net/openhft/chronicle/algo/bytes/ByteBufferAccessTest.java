/*
 * Copyright 2014-2025 chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.algo.bytes;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static java.nio.ByteOrder.BIG_ENDIAN;
import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ByteBufferAccessTest {

    private static final byte[] SAMPLE = {
            (byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78,
            (byte) 0x9A, (byte) 0xBC, (byte) 0xDE, (byte) 0xF0
    };

    private final ByteBufferAccess access = ByteBufferAccess.INSTANCE;

    @Test
    public void littleEndianAccessReadsExpectedValues() {
        ByteBuffer buffer = ByteBuffer.wrap(SAMPLE).order(LITTLE_ENDIAN);

        assertEquals(ByteOrder.LITTLE_ENDIAN, access.byteOrder(buffer));
        assertEquals(0xF0DEBC9A78563412L, access.readLong(buffer, 0));
        assertEquals(0xF0DEBC9AL, access.readUnsignedInt(buffer, 4));
        assertEquals(0x78563412, access.readInt(buffer, 0));
        assertEquals(0xBC9A, access.readUnsignedShort(buffer, 4));
        assertEquals(0x5634, access.readShort(buffer, 1));
        assertEquals(0xDE, access.readUnsignedByte(buffer, 6));
        assertEquals(-68, access.readByte(buffer, 5));
    }

    @Test
    public void bigEndianAccessReadsExpectedValues() {
        ByteBuffer buffer = ByteBuffer.wrap(SAMPLE).order(BIG_ENDIAN);

        assertEquals(ByteOrder.BIG_ENDIAN, access.byteOrder(buffer));
        assertEquals(0x123456789ABCDEF0L, access.readLong(buffer, 0));
        assertEquals(0x12345678L, access.readUnsignedInt(buffer, 0));
        assertEquals((int) 0x9ABCDEF0L, access.readInt(buffer, 4));
        assertEquals(0x5678, access.readUnsignedShort(buffer, 2));
        assertEquals(0x789A, access.readShort(buffer, 3));
        assertEquals(0x34, access.readUnsignedByte(buffer, 1));
        assertEquals(-102, access.readByte(buffer, 4));
    }
}
