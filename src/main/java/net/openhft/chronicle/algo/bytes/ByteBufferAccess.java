/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */

package net.openhft.chronicle.algo.bytes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Provides an implementation of the {@link Access} interface for {@link ByteBuffer} instances.
 * This class provides methods to read and write various data types to and from a {@link ByteBuffer}.
 */
final class ByteBufferAccess implements Access<ByteBuffer> {

    // Singleton instance of ByteBufferAccess
    public static final ByteBufferAccess INSTANCE = new ByteBufferAccess();

    // Private constructor to enforce singleton pattern
    private ByteBufferAccess() {
    }

    /**
     * Reads a byte from the given {@link ByteBuffer} at the specified offset.
     *
     * @param buffer the ByteBuffer to read from
     * @param offset the offset within the buffer
     * @return the byte value at the specified offset
     */
    @Override
    public byte readByte(ByteBuffer buffer, long offset) {
        return buffer.get((int) offset);
    }

    /**
     * Reads a short from the given {@link ByteBuffer} at the specified offset.
     *
     * @param buffer the ByteBuffer to read from
     * @param offset the offset within the buffer
     * @return the short value at the specified offset
     */
    @Override
    public short readShort(ByteBuffer buffer, long offset) {
        return buffer.getShort((int) offset);
    }

    /**
     * Reads a char from the given {@link ByteBuffer} at the specified offset.
     *
     * @param buffer the ByteBuffer to read from
     * @param offset the offset within the buffer
     * @return the char value at the specified offset
     */
    @Override
    public char readChar(ByteBuffer buffer, long offset) {
        return buffer.getChar((int) offset);
    }

    /**
     * Reads an int from the given {@link ByteBuffer} at the specified offset.
     *
     * @param buffer the ByteBuffer to read from
     * @param offset the offset within the buffer
     * @return the int value at the specified offset
     */
    @Override
    public int readInt(ByteBuffer buffer, long offset) {
        return buffer.getInt((int) offset);
    }

    /**
     * Reads a long from the given {@link ByteBuffer} at the specified offset.
     *
     * @param buffer the ByteBuffer to read from
     * @param offset the offset within the buffer
     * @return the long value at the specified offset
     */
    @Override
    public long readLong(ByteBuffer buffer, long offset) {
        return buffer.getLong((int) offset);
    }

    /**
     * Reads a float from the given {@link ByteBuffer} at the specified offset.
     *
     * @param buffer the ByteBuffer to read from
     * @param offset the offset within the buffer
     * @return the float value at the specified offset
     */
    @Override
    public float readFloat(ByteBuffer buffer, long offset) {
        return buffer.getFloat((int) offset);
    }

    /**
     * Reads a double from the given {@link ByteBuffer} at the specified offset.
     *
     * @param buffer the ByteBuffer to read from
     * @param offset the offset within the buffer
     * @return the double value at the specified offset
     */
    @Override
    public double readDouble(ByteBuffer buffer, long offset) {
        return buffer.getDouble((int) offset);
    }

    /**
     * Writes a byte to the given {@link ByteBuffer} at the specified offset.
     *
     * @param buffer the ByteBuffer to write to
     * @param offset the offset within the buffer
     * @param i8     the byte value to write
     */
    @Override
    public void writeByte(ByteBuffer buffer, long offset, byte i8) {
        buffer.put((int) offset, i8);
    }

    /**
     * Writes a short to the given {@link ByteBuffer} at the specified offset.
     *
     * @param buffer the ByteBuffer to write to
     * @param offset the offset within the buffer
     * @param i      the short value to write
     */
    @Override
    public void writeShort(ByteBuffer buffer, long offset, short i) {
        buffer.putShort((int) offset, i);
    }

    /**
     * Writes a char to the given {@link ByteBuffer} at the specified offset.
     *
     * @param buffer the ByteBuffer to write to
     * @param offset the offset within the buffer
     * @param c      the char value to write
     */
    @Override
    public void writeChar(ByteBuffer buffer, long offset, char c) {
        buffer.putChar((int) offset, c);
    }

    /**
     * Writes an int to the given {@link ByteBuffer} at the specified offset.
     *
     * @param buffer the ByteBuffer to write to
     * @param offset the offset within the buffer
     * @param i      the int value to write
     */
    @Override
    public void writeInt(ByteBuffer buffer, long offset, int i) {
        buffer.putInt((int) offset, i);
    }

    /**
     * Writes a long to the given {@link ByteBuffer} at the specified offset.
     *
     * @param buffer the ByteBuffer to write to
     * @param offset the offset within the buffer
     * @param i      the long value to write
     */
    @Override
    public void writeLong(ByteBuffer buffer, long offset, long i) {
        buffer.putLong((int) offset, i);
    }

    /**
     * Writes a float to the given {@link ByteBuffer} at the specified offset.
     *
     * @param buffer the ByteBuffer to write to
     * @param offset the offset within the buffer
     * @param d      the float value to write
     */
    @Override
    public void writeFloat(ByteBuffer buffer, long offset, float d) {
        buffer.putFloat((int) offset, d);
    }

    /**
     * Writes a double to the given {@link ByteBuffer} at the specified offset.
     *
     * @param buffer the ByteBuffer to write to
     * @param offset the offset within the buffer
     * @param d      the double value to write
     */
    @Override
    public void writeDouble(ByteBuffer buffer, long offset, double d) {
        buffer.putDouble((int) offset, d);
    }

    /**
     * Returns the byte order of the given {@link ByteBuffer}.
     *
     * @param buffer the ByteBuffer whose byte order is to be returned
     * @return the byte order of the given buffer
     */
    @Override
    public ByteOrder byteOrder(ByteBuffer buffer) {
        return buffer.order();
    }

    /**
     * Compares and swaps the int value at the specified offset in the given {@link ByteBuffer}.
     * <p>
     * This method is currently not supported and will throw an {@link UnsupportedOperationException}.
     *
     * @param handle   the ByteBuffer to operate on
     * @param offset   the offset within the buffer
     * @param expected the expected int value
     * @param value    the new int value to set
     * @return true if the swap was successful, false otherwise
     * @throws UnsupportedOperationException currently not supported
     */
    @Override
    public boolean compareAndSwapInt(ByteBuffer handle, long offset, int expected, int value) {
        throw new UnsupportedOperationException("todo");
    }

    /**
     * Compares and swaps the long value at the specified offset in the given {@link ByteBuffer}.
     * <p>
     * This method is currently not supported and will throw an {@link UnsupportedOperationException}.
     *
     * @param handle   the ByteBuffer to operate on
     * @param offset   the offset within the buffer
     * @param expected the expected long value
     * @param value    the new long value to set
     * @return true if the swap was successful, false otherwise
     * @throws UnsupportedOperationException currently not supported
     */
    @Override
    public boolean compareAndSwapLong(ByteBuffer handle, long offset, long expected, long value) {
        throw new UnsupportedOperationException("todo");
    }
}
