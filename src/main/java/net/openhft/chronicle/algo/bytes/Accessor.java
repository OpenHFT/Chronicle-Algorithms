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

import net.openhft.chronicle.bytes.BytesStore;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * The Accessor interface provides methods to obtain an Access instance and handle for a given source,
 * convert indexes and sizes between the source domain and Access offsets, and work with various types of data sources.
 *
 * @param <S> the source type
 * @param <T> the target type
 * @param <A> the AccessCommon type
 */
public interface Accessor<S, T, A extends AccessCommon<T>> {

    /**
     * Returns an instance of BytesStoreAccessor.
     *
     * @param <B> the type of BytesStore
     * @param <U> the underlying type of the BytesStore
     * @return an instance of BytesStoreAccessor
     */
    @SuppressWarnings("unchecked")
    static <B extends BytesStore<B, U>, U> Accessor.Full<B, ?> checkedBytesStoreAccessor() {
        return (Accessor.Full<B, ?>) BytesAccessors.Generic.INSTANCE;
    }

    /**
     * Returns an instance of ByteBufferAccessor for an unchecked ByteBuffer.
     *
     * @param buffer the ByteBuffer
     * @return an instance of ByteBufferAccessor
     */
    static Accessor.Full<ByteBuffer, ?> uncheckedByteBufferAccessor(
            ByteBuffer buffer) {
        return ByteBufferAccessor.unchecked(buffer);
    }

    /**
     * Returns an instance of Accessor for boolean arrays.
     *
     * @return an instance of Accessor for boolean arrays
     */
    static Accessor.Full<boolean[], boolean[]> booleanArrayAccessor() {
        return ArrayAccessors.Boolean.INSTANCE;
    }

    /**
     * Returns an instance of Accessor for byte arrays.
     *
     * @return an instance of Accessor for byte arrays
     */
    static Accessor.Full<byte[], byte[]> byteArrayAccessor() {
        return ArrayAccessors.Byte.INSTANCE;
    }

    /**
     * Returns an instance of Accessor for char arrays.
     *
     * @return an instance of Accessor for char arrays
     */
    static Accessor.Full<char[], char[]> charArrayAccessor() {
        return ArrayAccessors.Char.INSTANCE;
    }

    /**
     * Returns an instance of Accessor for short arrays.
     *
     * @return an instance of Accessor for short arrays
     */
    static Accessor.Full<short[], short[]> shortArrayAccessor() {
        return ArrayAccessors.Short.INSTANCE;
    }

    /**
     * Returns an instance of Accessor for int arrays.
     *
     * @return an instance of Accessor for int arrays
     */
    static Accessor.Full<int[], int[]> intArrayAccessor() {
        return ArrayAccessors.Int.INSTANCE;
    }

    /**
     * Returns an instance of Accessor for long arrays.
     *
     * @return an instance of Accessor for long arrays
     */
    static Accessor.Full<long[], long[]> longArrayAccessor() {
        return ArrayAccessors.Long.INSTANCE;
    }

    /**
     * Returns an instance of Accessor for strings.
     *
     * @return an instance of Accessor for strings
     */
    @SuppressWarnings("unchecked")
    static Accessor.Read<String, ?> stringAccessor() {
        return (Read<String, ?>) CharSequenceAccessor.stringAccessor;
    }

    /**
     * Returns an instance of Accessor for native char sequences.
     *
     * @return an instance of Accessor for native char sequences
     */
    static Accessor.Read<CharSequence, CharSequence> checkedNativeCharSequenceAccessor() {
        return CharSequenceAccessor.nativeCharSequenceAccessor();
    }

    /**
     * Returns an instance of Accessor for char sequences with the specified byte order.
     *
     * @param order the byte order
     * @return an instance of Accessor for char sequences with the specified byte order
     */
    static Accessor.Read<CharSequence, CharSequence> checkedCharSequenceAccess(ByteOrder order) {
        return order == ByteOrder.LITTLE_ENDIAN ? CharSequenceAccessor.LITTLE_ENDIAN :
                CharSequenceAccessor.BIG_ENDIAN;
    }

    /**
     * Returns {@code Access} for the given source.
     *
     * @return {@code Access} for the given source
     */
    A access();

    /**
     * Returns handle for {@code Access} to the given source.
     *
     * @param source the source
     * @return handle for {@code Access} to the given source
     */
    T handle(S source);

    /**
     * Convert index in the source domain to {@code Access} offset.
     *
     * @param source the source
     * @param index  index in the source type domain
     * @return offset for {@code Access}, corresponding to the given index
     */
    long offset(S source, long index);

    /**
     * Convert size (length) in the source domain to size in bytes.
     * <p>
     * The default implementation returns the given {@code size} back, i. e. assuming
     * byte-indexed source.
     *
     * @param size size (length) in the source type domain
     * @return number of bytes, corresponding to the given size in the source type domain
     */
    default long size(long size) {
        return size;
    }

    /**
     * Read-only Accessor interface.
     *
     * @param <S> the source type
     * @param <T> the target type
     */
    interface Read<S, T> extends Accessor<S, T, ReadAccess<T>> {
    }

    /**
     * Full Accessor interface with both read and write capabilities.
     *
     * @param <S> the source type
     * @param <T> the target type
     */
    interface Full<S, T> extends Accessor<S, T, Access<T>> {
    }
}
