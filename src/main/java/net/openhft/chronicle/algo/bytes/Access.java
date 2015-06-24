/*
 *     Copyright (C) 2015  higherfrequencytrading.com
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
        return BytesAccesses.Full.INSTANCE;
    }

    static ReadAccess<RandomDataInput> checkedRandomDataInputAccess() {
        return BytesAccesses.RandomDataInputReadAccessEnum.INSTANCE;
    }

    static <S, T> void copy(ReadAccess<S> sourceAccess, S source, long sourceOffset,
                            WriteAccess<T> targetAccess, T target, long targetOffset,
                            long len) {
        targetAccess.writeFrom(target, targetOffset, sourceAccess, source, sourceOffset, len);
    }

    static <T, U> boolean equivalent(ReadAccess<T> access1, T handle1, long offset1,
                                     ReadAccess<U> access2, U handle2, long offset2,
                                     long len) {
        return access1.compareTo(handle1, offset1, access2, handle2, offset2, len);
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