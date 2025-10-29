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

import static net.openhft.chronicle.core.UnsafeMemory.MEMORY;

/**
 * NativeAccess provides low-level access to memory using the Unsafe API.
 * This class supports various read and write operations on different primitive types.
 *
 * @param <T> the type of the object being accessed
 */
public final class NativeAccess<T> implements Access<T> {

    // Singleton instance of NativeAccess
    private static final NativeAccess<Object> INSTANCE = new NativeAccess<>();

    // Private constructor to prevent instantiation
    private NativeAccess() {
    }

    /**
     * Returns the singleton instance of NativeAccess.
     *
     * @param <T> the type of the object being accessed
     * @return the singleton instance of NativeAccess
     */
    @SuppressWarnings("unchecked")
    public static <T> NativeAccess<T> instance() {
        //noinspection unchecked
        return (NativeAccess<T>) INSTANCE;
    }

    /**
     * Reads a byte from the given offset.
     *
     * @param handle the object to read from
     * @param offset the offset to read from
     * @return the byte value at the specified offset
     */
    @Override
    public byte readByte(T handle, long offset) {
        return MEMORY.readByte(handle, offset);
    }

    /**
     * Reads a short from the given offset.
     *
     * @param handle the object to read from
     * @param offset the offset to read from
     * @return the short value at the specified offset
     */
    @Override
    public short readShort(T handle, long offset) {
        return MEMORY.readShort(handle, offset);
    }

    /**
     * Reads a char from the given offset.
     *
     * @param handle the object to read from
     * @param offset the offset to read from
     * @return the char value at the specified offset
     */
    @Override
    public char readChar(T handle, long offset) {
        return (char) MEMORY.readShort(handle, offset);
    }

    /**
     * Reads an int from the given offset.
     *
     * @param handle the object to read from
     * @param offset the offset to read from
     * @return the int value at the specified offset
     */
    @Override
    public int readInt(T handle, long offset) {
        return MEMORY.readInt(handle, offset);
    }

    /**
     * Reads a long from the given offset.
     *
     * @param handle the object to read from
     * @param offset the offset to read from
     * @return the long value at the specified offset
     */
    @Override
    public long readLong(T handle, long offset) {
        return MEMORY.readLong(handle, offset);
    }

    /**
     * Reads a float from the given offset.
     *
     * @param handle the object to read from
     * @param offset the offset to read from
     * @return the float value at the specified offset
     */
    @Override
    public float readFloat(T handle, long offset) {
        return MEMORY.readFloat(handle, offset);
    }

    /**
     * Reads a double from the given offset.
     *
     * @param handle the object to read from
     * @param offset the offset to read from
     * @return the double value at the specified offset
     */
    @Override
    public double readDouble(T handle, long offset) {
        return MEMORY.readDouble(handle, offset);
    }

    /**
     * Reads a volatile int from the given offset.
     *
     * @param handle the object to read from
     * @param offset the offset to read from
     * @return the volatile int value at the specified offset
     */
    @Override
    public int readVolatileInt(T handle, long offset) {
        return MEMORY.readVolatileInt(handle, offset);
    }

    /**
     * Reads a volatile long from the given offset.
     *
     * @param handle the object to read from
     * @param offset the offset to read from
     * @return the volatile long value at the specified offset
     */
    @Override
    public long readVolatileLong(T handle, long offset) {
        return MEMORY.readVolatileLong(handle, offset);
    }

    /**
     * Writes a byte to the given offset.
     *
     * @param handle the object to write to
     * @param offset the offset to write to
     * @param i8     the byte value to write
     */
    @Override
    public void writeByte(T handle, long offset, byte i8) {
        MEMORY.writeByte(handle, offset, i8);
    }

    /**
     * Writes a short to the given offset.
     *
     * @param handle the object to write to
     * @param offset the offset to write to
     * @param i      the short value to write
     */
    @Override
    public void writeShort(T handle, long offset, short i) {
        MEMORY.writeShort(handle, offset, i);
    }

    /**
     * Writes a char to the given offset.
     *
     * @param handle the object to write to
     * @param offset the offset to write to
     * @param c      the char value to write
     */
    @Override
    public void writeChar(T handle, long offset, char c) {
        MEMORY.writeShort(handle, offset, (short) c);
    }

    /**
     * Writes an int to the given offset.
     *
     * @param handle the object to write to
     * @param offset the offset to write to
     * @param i      the int value to write
     */
    @Override
    public void writeInt(T handle, long offset, int i) {
        MEMORY.writeInt(handle, offset, i);
    }

    /**
     * Writes an ordered int to the given offset.
     *
     * @param handle the object to write to
     * @param offset the offset to write to
     * @param i      the ordered int value to write
     */
    @Override
    public void writeOrderedInt(T handle, long offset, int i) {
        MEMORY.writeOrderedInt(handle, offset, i);
    }

    /**
     * Writes a long to the given offset.
     *
     * @param handle the object to write to
     * @param offset the offset to write to
     * @param i      the long value to write
     */
    @Override
    public void writeLong(T handle, long offset, long i) {
        MEMORY.writeLong(handle, offset, i);
    }

    /**
     * Writes an ordered long to the given offset.
     *
     * @param handle the object to write to
     * @param offset the offset to write to
     * @param i      the ordered long value to write
     */
    @Override
    public void writeOrderedLong(T handle, long offset, long i) {
        MEMORY.writeOrderedLong(handle, offset, i);
    }

    /**
     * Writes a float to the given offset.
     *
     * @param handle the object to write to
     * @param offset the offset to write to
     * @param d      the float value to write
     */
    @Override
    public void writeFloat(T handle, long offset, float d) {
        MEMORY.writeFloat(handle, offset, d);
    }

    /**
     * Writes a double to the given offset.
     *
     * @param handle the object to write to
     * @param offset the offset to write to
     * @param d      the double value to write
     */
    @Override
    public void writeDouble(T handle, long offset, double d) {
        MEMORY.writeDouble(handle, offset, d);
    }

    /**
     * Performs a compare-and-swap operation on an int value at the given offset.
     *
     * @param handle   the object to modify
     * @param offset   the offset to modify
     * @param expected the expected int value
     * @param value    the new int value
     * @return true if the swap was successful, false otherwise
     */
    @Override
    public boolean compareAndSwapInt(T handle, long offset, int expected, int value) {
        return MEMORY.compareAndSwapInt(handle, offset, expected, value);
    }

    /**
     * Performs a compare-and-swap operation on a long value at the given offset.
     *
     * @param handle   the object to modify
     * @param offset   the offset to modify
     * @param expected the expected long value
     * @param value    the new long value
     * @return true if the swap was successful, false otherwise
     */
    @Override
    public boolean compareAndSwapLong(T handle, long offset, long expected, long value) {
        return MEMORY.compareAndSwapLong(handle, offset, expected, value);
    }

    /**
     * Returns the byte order of the native platform.
     *
     * @param handle the object being accessed
     * @return the byte order of the native platform
     */
    @Override
    public ByteOrder byteOrder(T handle) {
        return ByteOrder.nativeOrder();
    }

    /**
     * Writes bytes to the specified memory region.
     *
     * @param handle the object to write to
     * @param offset the starting offset
     * @param len    the length of the memory region
     * @param b      the byte value to write
     */
    @Override
    public void writeBytes(T handle, long offset, long len, byte b) {
        MEMORY.setMemory(handle, offset, len, b);
    }

    /**
     * Sets the specified memory region to zero.
     *
     * @param handle the object to modify
     * @param offset the starting offset
     * @param len    the length of the memory region
     */
    @Override
    public void zeroOut(T handle, long offset, long len) {
        MEMORY.setMemory(handle, offset, len, (byte) 0);
    }
}
