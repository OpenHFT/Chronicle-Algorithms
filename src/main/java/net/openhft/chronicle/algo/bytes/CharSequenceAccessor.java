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

import net.openhft.chronicle.core.Jvm;

import java.nio.ByteOrder;

/**
 * Abstract class providing read access to {@link CharSequence} with support for different byte orders.
 * It provides a framework for accessing character sequences in either little-endian or big-endian byte order.
 */
abstract class CharSequenceAccessor
        implements Accessor.Read<CharSequence, CharSequence> {

    // Accessor for String instances, depending on Java version
    static final Accessor.Read<? super String, ?> stringAccessor;

    // Singleton instance for little-endian CharSequenceAccessor
    static final CharSequenceAccessor LITTLE_ENDIAN = new CharSequenceAccessor() {
        @Override
        public ReadAccess<CharSequence> access() {
            return CharSequenceAccess.LittleEndianCharSequenceAccess.INSTANCE;
        }
    };

    // Singleton instance for big-endian CharSequenceAccessor
    static final CharSequenceAccessor BIG_ENDIAN = new CharSequenceAccessor() {
        @Override
        public ReadAccess<CharSequence> access() {
            return CharSequenceAccess.BigEndianCharSequenceAccess.INSTANCE;
        }
    };

    static {
        if (Jvm.isJava9Plus())
            stringAccessor = HotSpotStringAccessor.JAVA9PLUS;
        else
            stringAccessor = HotSpotStringAccessor.JAVA8;
    }

    // Private constructor to prevent instantiation
    private CharSequenceAccessor() {
    }

    /**
     * Returns a native CharSequenceAccessor based on the system's native byte order.
     *
     * @return a CharSequenceAccessor instance corresponding to the native byte order
     */
    static CharSequenceAccessor nativeCharSequenceAccessor() {
        return ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN ? LITTLE_ENDIAN : BIG_ENDIAN;
    }

    /**
     * Returns the given CharSequence as the handle.
     *
     * @param source the source CharSequence
     * @return the given CharSequence
     */
    @Override
    public CharSequence handle(CharSequence source) {
        return source;
    }

    /**
     * Converts the given index to an offset in bytes.
     *
     * @param source the source CharSequence
     * @param index  the index
     * @return the offset in bytes
     */
    @Override
    public long offset(CharSequence source, long index) {
        return index * 2L;
    }

    /**
     * Converts the given size in characters to size in bytes.
     *
     * @param size the size in characters
     * @return the size in bytes
     */
    @Override
    public long size(long size) {
        return size * 2L;
    }
}
