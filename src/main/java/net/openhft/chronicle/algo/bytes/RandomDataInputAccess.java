/*
 *     Copyright 2015-2025 chronicle.software
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

import net.openhft.chronicle.bytes.RandomDataInput;

import java.nio.ByteOrder;

/**
 * Provides a default implementation for reading various primitive types and
 * volatile values from a {@link RandomDataInput} handle at a specified offset.
 *
 * @param <S> the type of the object being accessed, extending {@link RandomDataInput}
 */
interface RandomDataInputAccess<S extends RandomDataInput> extends ReadAccess<S> {

    /**
     * Reads a boolean value from the given offset.
     *
     * @param handle the object to read from
     * @param offset the offset to read from
     * @return the boolean value at the specified offset
     */
    @Override
    default boolean readBoolean(S handle, long offset) {
        return handle.readBoolean(offset);
    }

    /**
     * Reads a byte value from the given offset.
     *
     * @param handle the object to read from
     * @param offset the offset to read from
     * @return the byte value at the specified offset
     */
    @Override
    default byte readByte(S handle, long offset) {
        return handle.readByte(offset);
    }

    /**
     * Reads an unsigned byte value from the given offset.
     *
     * @param handle the object to read from
     * @param offset the offset to read from
     * @return the unsigned byte value at the specified offset
     */
    @Override
    default int readUnsignedByte(S handle, long offset) {
        return handle.readUnsignedByte(offset);
    }

    /**
     * Reads a short value from the given offset.
     *
     * @param handle the object to read from
     * @param offset the offset to read from
     * @return the short value at the specified offset
     */
    @Override
    default short readShort(S handle, long offset) {
        return handle.readShort(offset);
    }

    /**
     * Reads an unsigned short value from the given offset.
     *
     * @param handle the object to read from
     * @param offset the offset to read from
     * @return the unsigned short value at the specified offset
     */
    @Override
    default int readUnsignedShort(S handle, long offset) {
        return handle.readUnsignedShort(offset);
    }

    /**
     * Reads an int value from the given offset.
     *
     * @param handle the object to read from
     * @param offset the offset to read from
     * @return the int value at the specified offset
     */
    @Override
    default int readInt(S handle, long offset) {
        return handle.readInt(offset);
    }

    /**
     * Reads an unsigned int value from the given offset.
     *
     * @param handle the object to read from
     * @param offset the offset to read from
     * @return the unsigned int value at the specified offset
     */
    @Override
    default long readUnsignedInt(S handle, long offset) {
        return handle.readUnsignedInt(offset);
    }

    /**
     * Reads a long value from the given offset.
     *
     * @param handle the object to read from
     * @param offset the offset to read from
     * @return the long value at the specified offset
     */
    @Override
    default long readLong(S handle, long offset) {
        return handle.readLong(offset);
    }

    /**
     * Reads a float value from the given offset.
     *
     * @param handle the object to read from
     * @param offset the offset to read from
     * @return the float value at the specified offset
     */
    @Override
    default float readFloat(S handle, long offset) {
        return handle.readFloat(offset);
    }

    /**
     * Reads a double value from the given offset.
     *
     * @param handle the object to read from
     * @param offset the offset to read from
     * @return the double value at the specified offset
     */
    @Override
    default double readDouble(S handle, long offset) {
        return handle.readDouble(offset);
    }

    /**
     * Reads a printable string representation from the given offset.
     *
     * @param handle the object to read from
     * @param offset the offset to read from
     * @return the printable string representation at the specified offset
     */
    @Override
    default String printable(S handle, long offset) {
        return handle.printable(offset);
    }

    /**
     * Reads a volatile int value from the given offset.
     *
     * @param handle the object to read from
     * @param offset the offset to read from
     * @return the volatile int value at the specified offset
     */
    @Override
    default int readVolatileInt(S handle, long offset) {
        return handle.readVolatileInt(offset);
    }

    /**
     * Reads a volatile long value from the given offset.
     *
     * @param handle the object to read from
     * @param offset the offset to read from
     * @return the volatile long value at the specified offset
     */
    @Override
    default long readVolatileLong(S handle, long offset) {
        return handle.readVolatileLong(offset);
    }

    /**
     * Returns the byte order of the underlying data.
     *
     * @param handle the object to read from
     * @return the byte order of the underlying data
     */
    @Override
    default ByteOrder byteOrder(S handle) {
        return handle.byteOrder();
    }
}
