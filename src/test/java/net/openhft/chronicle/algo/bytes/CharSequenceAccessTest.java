//
// Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

package net.openhft.chronicle.algo.bytes;

import junit.framework.TestCase;
import org.junit.jupiter.api.Test;

import java.nio.ByteOrder;

public class CharSequenceAccessTest extends TestCase {
    private static final CharSequence TEST_SEQUENCE = "0123456789ABCDEF";

    @Test
    public void testReadInt() {
        CharSequenceAccess access = CharSequenceAccess.charSequenceAccess(ByteOrder.BIG_ENDIAN);
        int result = access.readInt(TEST_SEQUENCE, 0);
        long expected = CharSequenceAccess.charSequenceAccess(ByteOrder.BIG_ENDIAN)
                .readUnsignedInt(TEST_SEQUENCE, 0);
        assertEquals((int) expected, result);
    }

    @Test
    public void testReadUnsignedShort() {
        CharSequenceAccess access = CharSequenceAccess.charSequenceAccess(ByteOrder.BIG_ENDIAN);
        int result = access.readUnsignedShort(TEST_SEQUENCE, 0);
        int expected = TEST_SEQUENCE.charAt(0);
        assertEquals(expected, result);
    }

    @Test
    public void testReadShort() {
        CharSequenceAccess access = CharSequenceAccess.charSequenceAccess(ByteOrder.BIG_ENDIAN);
        short result = access.readShort(TEST_SEQUENCE, 0);
        short expected = (short) TEST_SEQUENCE.charAt(0);
        assertEquals(expected, result);
    }

    @Test
    public void testReadByte() {
        CharSequenceAccess access = CharSequenceAccess.charSequenceAccess(ByteOrder.BIG_ENDIAN);
        byte result = access.readByte(TEST_SEQUENCE, 0);
        int expected = CharSequenceAccess.charSequenceAccess(ByteOrder.BIG_ENDIAN)
                .readUnsignedByte(TEST_SEQUENCE, 0);
        assertEquals((byte) expected, result);
    }
}