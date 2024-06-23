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

import java.nio.ByteOrder;

/**
 * Utility class providing various access implementations for {@link BytesStore} and {@link RandomDataInput}.
 */
final class BytesAccesses {

    // Private constructor to prevent instantiation
    private BytesAccesses() {
    }

    /**
     * Enum providing {@link RandomDataInputAccess} implementation for {@link RandomDataInput}.
     */
    enum RandomDataInputReadAccessEnum implements RandomDataInputAccess<RandomDataInput> {
        INSTANCE
    }

    /**
     * Class providing full access implementations for {@link BytesStore}.
     *
     * @param <B> the type of BytesStore
     * @param <U> the type of underlying bytes
     */
    static class Full<B extends BytesStore<B, U>, U> implements RandomDataInputAccess<B>,
            RandomDataOutputAccess<B>, Access<B> {

        static final Full<?, ?> INSTANCE = new Full<>();

        /**
         * Compares the current value of the int at the given offset with the expected value,
         * and if they are equal, sets the int value to the given value.
         *
         * @param handle  the BytesStore handle
         * @param offset  the offset in the BytesStore
         * @param expected the expected int value
         * @param value   the new int value to set
         * @return true if the value was swapped, false otherwise
         */
        @Override
        public boolean compareAndSwapInt(B handle, long offset, int expected, int value) {
            return handle.compareAndSwapInt(offset, expected, value);
        }

        /**
         * Compares the current value of the long at the given offset with the expected value,
         * and if they are equal, sets the long value to the given value.
         *
         * @param handle  the BytesStore handle
         * @param offset  the offset in the BytesStore
         * @param expected the expected long value
         * @param value   the new long value to set
         * @return true if the value was swapped, false otherwise
         */
        @Override
        public boolean compareAndSwapLong(B handle, long offset, long expected, long value) {
            return handle.compareAndSwapLong(offset, expected, value);
        }

        /**
         * Returns the byte order of the BytesStore.
         *
         * @param handle the BytesStore handle
         * @return the byte order of the BytesStore
         */
        @Override
        public ByteOrder byteOrder(B handle) {
            return handle.byteOrder();
        }
    }
}
