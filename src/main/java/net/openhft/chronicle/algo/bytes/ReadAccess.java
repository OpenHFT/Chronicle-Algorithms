/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */

package net.openhft.chronicle.algo.bytes;

/**
 * Provides methods for reading various primitive types from a handle at a specified offset.
 *
 * @param <T> the type of the handle
 */
public interface ReadAccess<T> extends AccessCommon<T> {

    /**
     * Returns a ReadAccess implementation that always returns zero values.
     *
     * @return a ReadAccess implementation that always returns zero values
     */
    static ReadAccess<Void> zeros() {
        return ZeroAccess.INSTANCE;
    }

    /**
     * Reads a boolean value from the given offset.
     *
     * @param handle the object to read from
     * @param offset the offset to read from
     * @return the boolean value read
     */
    default boolean readBoolean(T handle, long offset) {
        return readByte(handle, offset) != 0;
    }

    /**
     * Reads a byte value from the given offset.
     *
     * @param handle the object to read from
     * @param offset the offset to read from
     * @return the byte value read
     */
    byte readByte(T handle, long offset);

    /**
     * Reads an unsigned byte value from the given offset.
     *
     * @param handle the object to read from
     * @param offset the offset to read from
     * @return the unsigned byte value read
     */
    default int readUnsignedByte(T handle, long offset) {
        return readByte(handle, offset) & 0xFF;
    }

    /**
     * Reads a short value from the given offset.
     *
     * @param handle the object to read from
     * @param offset the offset to read from
     * @return the short value read
     */
    short readShort(T handle, long offset);

    /**
     * Reads an unsigned short value from the given offset.
     *
     * @param handle the object to read from
     * @param offset the offset to read from
     * @return the unsigned short value read
     */
    default int readUnsignedShort(T handle, long offset) {
        return readShort(handle, offset) & 0xFFFF;
    }

    /**
     * Reads a char value from the given offset.
     *
     * @param handle the object to read from
     * @param offset the offset to read from
     * @return the char value read
     */
    default char readChar(T handle, long offset) {
        return (char) readShort(handle, offset);
    }

    /**
     * Reads an int value from the given offset.
     *
     * @param handle the object to read from
     * @param offset the offset to read from
     * @return the int value read
     */
    int readInt(T handle, long offset);

    /**
     * Reads an unsigned int value from the given offset.
     *
     * @param handle the object to read from
     * @param offset the offset to read from
     * @return the unsigned int value read
     */
    default long readUnsignedInt(T handle, long offset) {
        return readInt(handle, offset) & 0xFFFFFFFFL;
    }

    /**
     * Reads a long value from the given offset.
     *
     * @param handle the object to read from
     * @param offset the offset to read from
     * @return the long value read
     */
    long readLong(T handle, long offset);

    /**
     * Reads a float value from the given offset.
     * The default implementation converts the bits of an int to a float.
     *
     * @param handle the object to read from
     * @param offset the offset to read from
     * @return the float value read
     */
    default float readFloat(T handle, long offset) {
        return Float.intBitsToFloat(readInt(handle, offset));
    }

    /**
     * Reads a double value from the given offset.
     * The default implementation converts the bits of a long to a double.
     *
     * @param handle the object to read from
     * @param offset the offset to read from
     * @return the double value read
     */
    default double readDouble(T handle, long offset) {
        return Double.longBitsToDouble(readLong(handle, offset));
    }

    /**
     * Reads a printable string representation of the byte at the given offset.
     *
     * @param handle the object to read from
     * @param offset the offset to read from
     * @return the string representation of the byte value read
     */
    default String printable(T handle, long offset) {
        int b = readUnsignedByte(handle, offset);
        if (b == 0)
            return "\u0660";
        else if (b < 21)
            return String.valueOf((char) (b + 0x2487));
        else
            return String.valueOf((char) b);
    }

    /**
     * Reads a volatile int value from the given offset.
     * The default implementation throws {@code UnsupportedOperationException}.
     *
     * @param handle the object to read from
     * @param offset the offset to read from
     * @return the volatile int value read
     * @throws UnsupportedOperationException if the method is not supported
     */
    default int readVolatileInt(T handle, long offset) {
        throw new UnsupportedOperationException();
    }

    /**
     * Reads a volatile long value from the given offset.
     * The default implementation throws {@code UnsupportedOperationException}.
     *
     * @param handle the object to read from
     * @param offset the offset to read from
     * @return the volatile long value read
     * @throws UnsupportedOperationException if the method is not supported
     */
    default long readVolatileLong(T handle, long offset) {
        throw new UnsupportedOperationException();
    }
}
