/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo.bytes;

import net.openhft.chronicle.bytes.RandomDataOutput;

import java.nio.ByteOrder;

/**
 * Provides a default implementation for writing various primitive types
 * and volatile values to a {@link RandomDataOutput} handle at a specified offset.
 *
 * @param <R> the type of the object being accessed, extending {@link RandomDataOutput}
 */
interface RandomDataOutputAccess<R extends RandomDataOutput<R>>
        extends WriteAccess<R> {

    /**
     * Writes a byte value to the given offset.
     *
     * @param handle the object to write to
     * @param offset the offset to write to
     * @param i      the byte value to write
     */
    @Override
    default void writeByte(R handle, long offset, int i) {
        handle.writeByte(offset, i);
    }

    /**
     * Writes an unsigned byte value to the given offset.
     *
     * @param handle the object to write to
     * @param offset the offset to write to
     * @param i      the unsigned byte value to write
     */
    @Override
    default void writeUnsignedByte(R handle, long offset, int i) {
        handle.writeUnsignedByte(offset, i);
    }

    /**
     * Writes a boolean value to the given offset.
     *
     * @param handle the object to write to
     * @param offset the offset to write to
     * @param flag   the boolean value to write
     */
    @Override
    default void writeBoolean(R handle, long offset, boolean flag) {
        handle.writeBoolean(offset, flag);
    }

    /**
     * Writes an unsigned short value to the given offset.
     *
     * @param handle the object to write to
     * @param offset the offset to write to
     * @param i      the unsigned short value to write
     */
    @Override
    default void writeUnsignedShort(R handle, long offset, int i) {
        handle.writeUnsignedShort(offset, i);
    }

    /**
     * Writes an unsigned int value to the given offset.
     *
     * @param handle the object to write to
     * @param offset the offset to write to
     * @param i      the unsigned int value to write
     */
    @Override
    default void writeUnsignedInt(R handle, long offset, long i) {
        handle.writeUnsignedInt(offset, i);
    }

    /**
     * Writes a byte value to the given offset.
     *
     * @param handle the object to write to
     * @param offset the offset to write to
     * @param i8     the byte value to write
     */
    @Override
    default void writeByte(R handle, long offset, byte i8) {
        handle.writeByte(offset, i8);
    }

    /**
     * Writes a short value to the given offset.
     *
     * @param handle the object to write to
     * @param offset the offset to write to
     * @param i      the short value to write
     */
    @Override
    default void writeShort(R handle, long offset, short i) {
        handle.writeShort(offset, i);
    }

    /**
     * Writes an int value to the given offset.
     *
     * @param handle the object to write to
     * @param offset the offset to write to
     * @param i      the int value to write
     */
    @Override
    default void writeInt(R handle, long offset, int i) {
        handle.writeInt(offset, i);
    }

    /**
     * Writes an ordered int value to the given offset.
     *
     * @param handle the object to write to
     * @param offset the offset to write to
     * @param i      the ordered int value to write
     */
    @Override
    default void writeOrderedInt(R handle, long offset, int i) {
        handle.writeOrderedInt(offset, i);
    }

    /**
     * Writes a long value to the given offset.
     *
     * @param handle the object to write to
     * @param offset the offset to write to
     * @param i      the long value to write
     */
    @Override
    default void writeLong(R handle, long offset, long i) {
        handle.writeLong(offset, i);
    }

    /**
     * Writes an ordered long value to the given offset.
     *
     * @param handle the object to write to
     * @param offset the offset to write to
     * @param i      the ordered long value to write
     */
    @Override
    default void writeOrderedLong(R handle, long offset, long i) {
        handle.writeOrderedLong(offset, i);
    }

    /**
     * Writes a float value to the given offset.
     *
     * @param handle the object to write to
     * @param offset the offset to write to
     * @param d      the float value to write
     */
    @Override
    default void writeFloat(R handle, long offset, float d) {
        handle.writeFloat(offset, d);
    }

    /**
     * Writes a double value to the given offset.
     *
     * @param handle the object to write to
     * @param offset the offset to write to
     * @param d      the double value to write
     */
    @Override
    default void writeDouble(R handle, long offset, double d) {
        handle.writeDouble(offset, d);
    }

    /**
     * Returns the byte order of the underlying data.
     *
     * @param handle the object to write to
     * @return the byte order of the underlying data
     */
    @Override
    default ByteOrder byteOrder(R handle) {
        return handle.byteOrder();
    }
}
