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

import net.openhft.chronicle.core.Maths;

/**
 * Provides methods for writing various primitive types to a handle at a specified offset.
 *
 * @param <T> the type of the handle
 */
interface WriteAccess<T> extends AccessCommon<T> {

    /**
     * Writes a byte value to the given offset.
     *
     * @param handle the object to write to
     * @param offset the offset to write to
     * @param i      the value to write
     */
    default void writeByte(T handle, long offset, int i) {
        writeByte(handle, offset, Maths.toInt8(i));
    }

    /**
     * Writes an unsigned byte value to the given offset.
     *
     * @param handle the object to write to
     * @param offset the offset to write to
     * @param i      the value to write
     */
    default void writeUnsignedByte(T handle, long offset, int i) {
        writeByte(handle, offset, (byte) Maths.toUInt8(i));
    }

    /**
     * Writes a boolean value to the given offset.
     *
     * @param handle the object to write to
     * @param offset the offset to write to
     * @param flag   the boolean value to write
     */
    default void writeBoolean(T handle, long offset, boolean flag) {
        writeByte(handle, offset, flag ? 'Y' : 0);
    }

    /**
     * Writes an unsigned short value to the given offset.
     *
     * @param handle the object to write to
     * @param offset the offset to write to
     * @param i      the value to write
     */
    default void writeUnsignedShort(T handle, long offset, int i) {
        writeShort(handle, offset, (short) Maths.toUInt16(i));
    }

    /**
     * Writes a char value to the given offset.
     *
     * @param handle the object to write to
     * @param offset the offset to write to
     * @param c      the char value to write
     */
    default void writeChar(T handle, long offset, char c) {
        writeShort(handle, offset, (short) c);
    }

    /**
     * Writes an unsigned int value to the given offset.
     *
     * @param handle the object to write to
     * @param offset the offset to write to
     * @param i      the value to write
     */
    default void writeUnsignedInt(T handle, long offset, long i) {
        writeInt(handle, offset, (int) Maths.toUInt32(i));
    }

    /**
     * Writes a byte value to the given offset.
     *
     * @param handle the object to write to
     * @param offset the offset to write to
     * @param i8     the byte value to write
     */
    void writeByte(T handle, long offset, byte i8);

    /**
     * Writes a short value to the given offset.
     *
     * @param handle the object to write to
     * @param offset the offset to write to
     * @param i      the short value to write
     */
    void writeShort(T handle, long offset, short i);

    /**
     * Writes an int value to the given offset.
     *
     * @param handle the object to write to
     * @param offset the offset to write to
     * @param i      the int value to write
     */
    void writeInt(T handle, long offset, int i);

    /**
     * Writes an int value to the given offset with ordered semantics.
     * The default implementation throws {@code UnsupportedOperationException}.
     *
     * @param handle the object to write to
     * @param offset the offset to write to
     * @param i      the int value to write
     * @throws UnsupportedOperationException if the method is not supported
     */
    default void writeOrderedInt(T handle, long offset, int i) {
        throw new UnsupportedOperationException();
    }

    /**
     * Writes a long value to the given offset.
     *
     * @param handle the object to write to
     * @param offset the offset to write to
     * @param i      the long value to write
     */
    void writeLong(T handle, long offset, long i);

    /**
     * Writes a long value to the given offset with ordered semantics.
     * The default implementation throws {@code UnsupportedOperationException}.
     *
     * @param handle the object to write to
     * @param offset the offset to write to
     * @param i      the long value to write
     * @throws UnsupportedOperationException if the method is not supported
     */
    default void writeOrderedLong(T handle, long offset, long i) {
        throw new UnsupportedOperationException();
    }

    /**
     * Writes a float value to the given offset.
     *
     * @param handle the object to write to
     * @param offset the offset to write to
     * @param d      the float value to write
     */
    void writeFloat(T handle, long offset, float d);

    /**
     * Writes a double value to the given offset.
     *
     * @param handle the object to write to
     * @param offset the offset to write to
     * @param d      the double value to write
     */
    void writeDouble(T handle, long offset, double d);

    /**
     * Writes a specified byte value repeatedly to a given length starting from the offset.
     *
     * @param handle the object to write to
     * @param offset the offset to start writing from
     * @param len    the number of bytes to write
     * @param b      the byte value to write repeatedly
     */
    default void writeBytes(T handle, long offset, long len, byte b) {
        char c;
        int i;
        long l;
        switch (b) {
            case 0:
                zeroOut(handle, offset, len);
                return;
            case -1:
                c = Character.MAX_VALUE;
                i = -1;
                l = -1;
                break;
            default:
                int ub = b & 0xFF;
                int ic = ub | (ub << 8);
                c = (char) ic;
                i = ic | (ic << 16);
                long ui = i & 0xFFFFFFFFL;
                l = ui | (ui << 32);
        }
        long index = 0;
        while (len - index >= 8L) {
            writeLong(handle, offset + index, l);
            index += 8L;
        }
        if (len - index >= 4L) {
            writeInt(handle, offset + index, i);
            index += 4L;
        }
        if (len - index >= 2L) {
            writeChar(handle, offset + index, c);
            index += 2L;
        }
        if (index < len)
            writeByte(handle, offset + index, b);
    }

    /**
     * Writes zeros repeatedly to a given length starting from the offset.
     *
     * @param handle the object to write to
     * @param offset the offset to start writing from
     * @param len    the number of bytes to write
     */
    default void zeroOut(T handle, long offset, long len) {
        long index = 0;
        while (len - index >= 8L) {
            writeLong(handle, offset + index, 0L);
            index += 8L;
        }
        if (len - index >= 4L) {
            writeInt(handle, offset + index, 0);
            index += 4L;
        }
        if (len - index >= 2L) {
            writeChar(handle, offset + index, (char) 0);
            index += 2L;
        }
        if (index < len)
            writeByte(handle, offset + index, (byte) 0);
    }
}
