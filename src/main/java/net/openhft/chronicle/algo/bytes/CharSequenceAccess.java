//
// Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

/*
 *     Copyright (C) 2015-2020 chronicle.software
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.openhft.chronicle.algo.bytes;

import java.nio.ByteOrder;

import static java.nio.ByteOrder.BIG_ENDIAN;
import static java.nio.ByteOrder.LITTLE_ENDIAN;

/**
 * Abstract class providing read access to {@link CharSequence} with support for different byte orders.
 */
abstract class CharSequenceAccess implements ReadAccess<CharSequence> {

    // Private constructor to prevent instantiation
    private CharSequenceAccess() {
    }

    /**
     * Returns an instance of CharSequenceAccess based on the given byte order.
     *
     * @param order the byte order
     * @return an instance of CharSequenceAccess
     */
    public static CharSequenceAccess charSequenceAccess(ByteOrder order) {
        return order == LITTLE_ENDIAN ?
                LittleEndianCharSequenceAccess.INSTANCE :
                BigEndianCharSequenceAccess.INSTANCE;
    }

    /**
     * Converts the given offset to an index.
     *
     * @param offset the offset
     * @return the index
     */
    private static int ix(long offset) {
        return (int) (offset >> 1);
    }

    /**
     * Reads a long value from the input CharSequence at the given offset with specified character offsets.
     *
     * @param input    the input CharSequence
     * @param offset   the offset
     * @param char0Off the offset for the first character
     * @param char1Off the offset for the second character
     * @param char2Off the offset for the third character
     * @param char3Off the offset for the fourth character
     * @return the long value
     */
    private static long getLong(CharSequence input, long offset,
                                int char0Off, int char1Off, int char2Off, int char3Off) {
        int base = ix(offset);
        long char0 = input.charAt(base + char0Off);
        long char1 = input.charAt(base + char1Off);
        long char2 = input.charAt(base + char2Off);
        long char3 = input.charAt(base + char3Off);
        return char0 | (char1 << 16) | (char2 << 32) | (char3 << 48);
    }

    /**
     * Reads an unsigned int value from the input CharSequence at the given offset with specified character offsets.
     *
     * @param input    the input CharSequence
     * @param offset   the offset
     * @param char0Off the offset for the first character
     * @param char1Off the offset for the second character
     * @return the unsigned int value
     */
    private static long getUnsignedInt(CharSequence input, long offset,
                                       int char0Off, int char1Off) {
        int base = ix(offset);
        long char0 = input.charAt(base + char0Off);
        long char1 = input.charAt(base + char1Off);
        return char0 | (char1 << 16);
    }

    /**
     * Reads an unsigned byte value from the input CharSequence at the given offset and shift.
     *
     * @param input  the input CharSequence
     * @param offset the offset
     * @param shift  the shift value
     * @return the unsigned byte value
     */
    private static int getUnsignedByte(CharSequence input, long offset, int shift) {
        return (input.charAt(ix(offset)) >> shift) & 0xFF;
    }

    /**
     * Reads an int value from the input CharSequence at the given offset.
     *
     * @param input  the input CharSequence
     * @param offset the offset
     * @return the int value
     */
    @Override
    public int readInt(CharSequence input, long offset) {
        return (int) readUnsignedInt(input, offset);
    }

    /**
     * Reads an unsigned short value from the input CharSequence at the given offset.
     *
     * @param input  the input CharSequence
     * @param offset the offset
     * @return the unsigned short value
     */
    @Override
    public int readUnsignedShort(CharSequence input, long offset) {
        return input.charAt(ix(offset));
    }

    /**
     * Reads a short value from the input CharSequence at the given offset.
     *
     * @param input  the input CharSequence
     * @param offset the offset
     * @return the short value
     */
    @Override
    public short readShort(CharSequence input, long offset) {
        return (short) input.charAt(ix(offset));
    }

    /**
     * Reads a byte value from the input CharSequence at the given offset.
     *
     * @param input  the input CharSequence
     * @param offset the offset
     * @return the byte value
     */
    @Override
    public byte readByte(CharSequence input, long offset) {
        return (byte) readUnsignedByte(input, offset);
    }

    /**
     * Little-endian implementation of CharSequenceAccess.
     */
    static class LittleEndianCharSequenceAccess extends CharSequenceAccess {
        static final CharSequenceAccess INSTANCE = new LittleEndianCharSequenceAccess();

        private LittleEndianCharSequenceAccess() {
        }

        @Override
        public long readLong(CharSequence input, long offset) {
            return getLong(input, offset, 0, 1, 2, 3);
        }

        @Override
        public long readUnsignedInt(CharSequence input, long offset) {
            return getUnsignedInt(input, offset, 0, 1);
        }

        @Override
        public int readUnsignedByte(CharSequence input, long offset) {
            return getUnsignedByte(input, offset, ((int) offset & 1) << 3);
        }

        @Override
        public ByteOrder byteOrder(CharSequence input) {
            return LITTLE_ENDIAN;
        }
    }

    /**
     * Big-endian implementation of CharSequenceAccess.
     */
    static class BigEndianCharSequenceAccess extends CharSequenceAccess {
        static final CharSequenceAccess INSTANCE = new BigEndianCharSequenceAccess();

        private BigEndianCharSequenceAccess() {
        }

        @Override
        public long readLong(CharSequence input, long offset) {
            return getLong(input, offset, 3, 2, 1, 0);
        }

        @Override
        public long readUnsignedInt(CharSequence input, long offset) {
            return getUnsignedInt(input, offset, 1, 0);
        }

        @Override
        public int readUnsignedByte(CharSequence input, long offset) {
            return getUnsignedByte(input, offset, (((int) offset & 1) ^ 1) << 3);
        }

        @Override
        public ByteOrder byteOrder(CharSequence input) {
            return BIG_ENDIAN;
        }
    }
}
