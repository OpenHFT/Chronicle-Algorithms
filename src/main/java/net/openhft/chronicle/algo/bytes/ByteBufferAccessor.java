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


import java.nio.ByteBuffer;

import static net.openhft.chronicle.core.UnsafeMemory.MEMORY;

interface ByteBufferAccessor<T> extends Accessor.Full<ByteBuffer, T> {

    static ByteBufferAccessor<?> unchecked(ByteBuffer buffer) {
        return buffer.isDirect() ? Direct.INSTANCE : Heap.INSTANCE;
    }

    static ByteBufferAccessor<ByteBuffer> checked() {
        return Generic.INSTANCE;
    }

    enum Direct implements ByteBufferAccessor<Void> {
        INSTANCE;

        @Override
        public Access<Void> access() {
            return NativeAccess.instance();
        }

        @Override
        public Void handle(ByteBuffer buffer) {
            return null;
        }

        @Override
        public long offset(ByteBuffer buffer, long bufferIndex) {
            return MEMORY.address(buffer) + bufferIndex;
        }
    }

    enum Heap implements ByteBufferAccessor<byte[]> {
        INSTANCE;

        @Override
        public Access<byte[]> access() {
            return NativeAccess.instance();
        }

        @Override
        public byte[] handle(ByteBuffer buffer) {
            return buffer.array();
        }

        @Override
        public long offset(ByteBuffer buffer, long bufferIndex) {
            return ArrayAccessors.BYTE_BASE + buffer.arrayOffset() + bufferIndex;
        }
    }

    enum Generic implements ByteBufferAccessor<ByteBuffer> {
        INSTANCE;

        @Override
        public Access<ByteBuffer> access() {
            return ByteBufferAccess.INSTANCE;
        }

        @Override
        public ByteBuffer handle(ByteBuffer buffer) {
            return buffer;
        }

        @Override
        public long offset(ByteBuffer buffer, long bufferIndex) {
            return bufferIndex;
        }
    }
}
