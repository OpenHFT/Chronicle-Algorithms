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

import java.nio.ByteOrder;

final class BytesAccesses {

    private BytesAccesses() {
    }

     enum RandomDataInputReadAccessEnum implements RandomDataInputAccess<RandomDataInput> {
        INSTANCE
    }

    static class Full<B extends BytesStore<B, U>, U> implements RandomDataInputAccess<B>,
            RandomDataOutputAccess<B>, Access<B> {
        static final Full INSTANCE = new Full();

        @Override
        public boolean compareAndSwapInt(B handle, long offset, int expected, int value) {
            return handle.compareAndSwapInt(offset, expected, value);
        }

        @Override
        public boolean compareAndSwapLong(B handle, long offset, long expected, long value) {
            return handle.compareAndSwapLong(offset, expected, value);
        }

        @Override
        public ByteOrder byteOrder(B handle) {
            return handle.byteOrder();
        }
    }
}
