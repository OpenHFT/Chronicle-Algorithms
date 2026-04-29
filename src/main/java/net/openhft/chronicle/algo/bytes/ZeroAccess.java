/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo.bytes;

import java.nio.ByteOrder;

/**
 * A {@link ReadAccess} implementation that always returns zero or false for read operations.
 * This is a singleton implementation, accessed via the {@code INSTANCE} enum constant.
 */
enum ZeroAccess implements ReadAccess<Void> {
    INSTANCE;

    /**
     * Always returns {@code false}.
     *
     * @param handle the handle, which is ignored
     * @param offset the offset, which is ignored
     * @return {@code false}
     */
    @Override
    public boolean readBoolean(Void handle, long offset) {
        return false;
    }

    /**
     * Always returns {@code 0}.
     *
     * @param handle the handle, which is ignored
     * @param offset the offset, which is ignored
     * @return {@code 0}
     */
    @Override
    public byte readByte(Void handle, long offset) {
        return 0;
    }

    /**
     * Always returns {@code 0}.
     *
     * @param handle the handle, which is ignored
     * @param offset the offset, which is ignored
     * @return {@code 0}
     */
    @Override
    public int readUnsignedByte(Void handle, long offset) {
        return 0;
    }

    /**
     * Always returns {@code 0}.
     *
     * @param handle the handle, which is ignored
     * @param offset the offset, which is ignored
     * @return {@code 0}
     */
    @Override
    public short readShort(Void handle, long offset) {
        return 0;
    }

    /**
     * Always returns {@code 0}.
     *
     * @param handle the handle, which is ignored
     * @param offset the offset, which is ignored
     * @return {@code 0}
     */
    @Override
    public int readUnsignedShort(Void handle, long offset) {
        return 0;
    }

    /**
     * Always returns {@code 0}.
     *
     * @param handle the handle, which is ignored
     * @param offset the offset, which is ignored
     * @return {@code 0}
     */
    @Override
    public char readChar(Void handle, long offset) {
        return 0;
    }

    /**
     * Always returns {@code 0}.
     *
     * @param handle the handle, which is ignored
     * @param offset the offset, which is ignored
     * @return {@code 0}
     */
    @Override
    public int readInt(Void handle, long offset) {
        return 0;
    }

    /**
     * Always returns {@code 0}.
     *
     * @param handle the handle, which is ignored
     * @param offset the offset, which is ignored
     * @return {@code 0}
     */
    @Override
    public long readUnsignedInt(Void handle, long offset) {
        return 0;
    }

    /**
     * Always returns {@code 0}.
     *
     * @param handle the handle, which is ignored
     * @param offset the offset, which is ignored
     * @return {@code 0}
     */
    @Override
    public long readLong(Void handle, long offset) {
        return 0;
    }

    /**
     * Always returns {@code 0.0f}.
     *
     * @param handle the handle, which is ignored
     * @param offset the offset, which is ignored
     * @return {@code 0.0f}
     */
    @Override
    public float readFloat(Void handle, long offset) {
        return 0;
    }

    /**
     * Always returns {@code 0.0}.
     *
     * @param handle the handle, which is ignored
     * @param offset the offset, which is ignored
     * @return {@code 0.0}
     */
    @Override
    public double readDouble(Void handle, long offset) {
        return 0;
    }

    /**
     * Always returns the Unicode character for zero in Arabic-Indic digits.
     *
     * @param handle the handle, which is ignored
     * @param offset the offset, which is ignored
     * @return the Unicode character for zero in Arabic-Indic digits
     */
    @Override
    public String printable(Void handle, long offset) {
        return "\u0660";
    }

    /**
     * Always returns {@code 0}.
     *
     * @param handle the handle, which is ignored
     * @param offset the offset, which is ignored
     * @return {@code 0}
     */
    @Override
    public int readVolatileInt(Void handle, long offset) {
        return 0;
    }

    /**
     * Always returns {@code 0}.
     *
     * @param handle the handle, which is ignored
     * @param offset the offset, which is ignored
     * @return {@code 0}
     */
    @Override
    public long readVolatileLong(Void handle, long offset) {
        return 0;
    }

    /**
     * Returns the native byte order.
     *
     * @param handle the handle, which is ignored
     * @return the native byte order
     */
    @Override
    public ByteOrder byteOrder(Void handle) {
        return ByteOrder.nativeOrder();
    }
}
