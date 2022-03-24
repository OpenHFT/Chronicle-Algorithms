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

import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.bytes.RandomDataInput;

import java.nio.ByteBuffer;

public interface Access<T> extends ReadAccess<T>, WriteAccess<T> {

    static <T> Access<T> nativeAccess() {
        return NativeAccess.instance();
    }

    static Access<ByteBuffer> checkedByteBufferAccess() {
        return ByteBufferAccess.INSTANCE;
    }

    static <B extends BytesStore<B, U>, U> Access<B> checkedBytesStoreAccess() {
        return (Access<B>) BytesAccesses.Full.INSTANCE;
    }

    static ReadAccess<RandomDataInput> checkedRandomDataInputAccess() {
        return BytesAccesses.RandomDataInputReadAccessEnum.INSTANCE;
    }

    static <S, T> void copy(final ReadAccess<S> sourceAccess,
                            final S source,
                            final long sourceOffset,
                            final WriteAccess<T> targetAccess,
                            final T target,
                            final long targetOffset,
                            final long len) {
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
     * Default implementation: throws {@code UnsupportedOperationException}.
     */
    boolean compareAndSwapInt(T handle, long offset, int expected, int value);

    /**
     * Default implementation: throws {@code UnsupportedOperationException}.
     */
    boolean compareAndSwapLong(T handle, long offset, long expected, long value);
}