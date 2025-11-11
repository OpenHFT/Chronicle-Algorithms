/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo.bytes;

import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.bytes.RandomDataInput;

import java.nio.ByteBuffer;

/**
 * The Access interface combines read and write access capabilities for a given type {@code T}.
 * It provides various utility methods for native access, byte buffer access, and bytes store access,
 * as well as methods for copying and checking equivalence between different access types.
 *
 * @param <T> the type of the object to be accessed
 */
public interface Access<T> extends ReadAccess<T>, WriteAccess<T> {

    /**
     * Returns an instance of NativeAccess.
     *
     * @param <T> the type of the object to be accessed
     * @return an instance of NativeAccess
     */
    static <T> Access<T> nativeAccess() {
        return NativeAccess.instance();
    }

    /**
     * Returns an instance of ByteBufferAccess.
     *
     * @return an instance of ByteBufferAccess
     */
    static Access<ByteBuffer> checkedByteBufferAccess() {
        return ByteBufferAccess.INSTANCE;
    }

    /**
     * Returns an instance of BytesAccess for BytesStore.
     *
     * @param <B> the type of BytesStore
     * @param <U> the underlying type of the BytesStore
     * @return an instance of BytesAccess for BytesStore
     */
    @SuppressWarnings("unchecked")
    static <B extends BytesStore<B, U>, U> Access<B> checkedBytesStoreAccess() {
        return (Access<B>) BytesAccesses.Full.INSTANCE;
    }

    /**
     * Returns an instance of RandomDataInputReadAccess.
     *
     * @return an instance of RandomDataInputReadAccess
     */
    static ReadAccess<RandomDataInput> checkedRandomDataInputAccess() {
        return BytesAccesses.RandomDataInputReadAccessEnum.INSTANCE;
    }

    /**
     * Copies data from the source to the target using the provided access interfaces.
     *
     * @param sourceAccess the source access interface
     * @param source       the source handle
     * @param sourceOffset the source offset
     * @param targetAccess the target access interface
     * @param target       the target handle
     * @param targetOffset the target offset
     * @param len          the length of data to copy
     * @param <S>          the source type
     * @param <T>          the target type
     */
    static <S, T> void copy(final ReadAccess<S> sourceAccess,
                            final S source,
                            final long sourceOffset,
                            final WriteAccess<T> targetAccess,
                            final T target,
                            final long targetOffset,
                            final long len) {
        // If the source and target are the same and the offsets are the same, no need to copy
        if (targetAccess == sourceAccess && target == source && targetOffset == sourceOffset)
            return;
        long i = 0;
        while (len - i >= 8L) {
            targetAccess.writeLong(target, targetOffset + i, sourceAccess.readLong(source, sourceOffset + i));
            i += 8L;
        }
        if (len - i >= 4L) {
            targetAccess.writeInt(target, targetOffset + i, sourceAccess.readInt(source, sourceOffset + i));
            i += 4L;
        }
        if (len - i >= 2L) {
            targetAccess.writeShort(target, targetOffset + i, sourceAccess.readShort(source, sourceOffset + i));
            i += 2L;
        }
        if (i < len) {
            targetAccess.writeByte(target, targetOffset + i, sourceAccess.readByte(source, sourceOffset + i));
        }
    }

    /**
     * Checks if the data in two different access types are equivalent.
     *
     * @param access1 the first access interface
     * @param handle1 the first handle
     * @param offset1 the first offset
     * @param access2 the second access interface
     * @param handle2 the second handle
     * @param offset2 the second offset
     * @param len     the length of data to compare
     * @param <T>     the type of the first handle
     * @param <U>     the type of the second handle
     * @return true if the data is equivalent, false otherwise
     */
    static <T, U> boolean equivalent(final ReadAccess<T> access1,
                                     final T handle1,
                                     final long offset1,
                                     final ReadAccess<U> access2,
                                     final U handle2,
                                     final long offset2,
                                     final long len) {
        long i = 0;
        while (len - i >= 8L) {
            if (access1.readLong(handle1, offset1 + i) != access2.readLong(handle2, offset2 + i))
                return false;
            i += 8L;
        }
        if (len - i >= 4L) {
            if (access1.readInt(handle1, offset1 + i) != access2.readInt(handle2, offset2 + i))
                return false;
            i += 4L;
        }
        if (len - i >= 2L) {
            if (access1.readShort(handle1, offset1 + i) != access2.readShort(handle2, offset2 + i))
                return false;
            i += 2L;
        }
        if (i < len) {
            return access1.readByte(handle1, offset1 + i) == access2.readByte(handle2, offset2 + i);
        }
        return true;
    }

    /**
     * Compares and swaps an int value atomically.
     *
     * @param handle   the handle to the underlying data structure
     * @param offset   the offset within the data structure
     * @param expected the expected int value
     * @param value    the new int value to set if the current value equals the expected value
     * @return true if the swap was successful, false otherwise
     */
    boolean compareAndSwapInt(T handle, long offset, int expected, int value);

    /**
     * Compares and swaps a long value atomically.
     *
     * @param handle   the handle to the underlying data structure
     * @param offset   the offset within the data structure
     * @param expected the expected long value
     * @param value    the new long value to set if the current value equals the expected value
     * @return true if the swap was successful, false otherwise
     */
    boolean compareAndSwapLong(T handle, long offset, long expected, long value);
}
