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

import java.nio.ByteOrder;

import static net.openhft.chronicle.core.UnsafeMemory.MEMORY;

public final class NativeAccess<T> implements Access<T> {

    private static final NativeAccess<Object> INSTANCE = new NativeAccess<>();

    private NativeAccess() {
    }

    public static <T> NativeAccess<T> instance() {
        //noinspection unchecked
        return (NativeAccess<T>) INSTANCE;
    }

    @Override
    public byte readByte(T handle, long offset) {
        return MEMORY.readByte(handle, offset);
    }

    @Override
    public short readShort(T handle, long offset) {
        return MEMORY.readShort(handle, offset);
    }

    @Override
    public char readChar(T handle, long offset) {
        return (char) MEMORY.readShort(handle, offset);
    }

    @Override
    public int readInt(T handle, long offset) {
        return MEMORY.readInt(handle, offset);
    }

    @Override
    public long readLong(T handle, long offset) {
        return MEMORY.readLong(handle, offset);
    }

    @Override
    public float readFloat(T handle, long offset) {
        return MEMORY.readFloat(handle, offset);
    }

    @Override
    public double readDouble(T handle, long offset) {
        return MEMORY.readDouble(handle, offset);
    }

    @Override
    public int readVolatileInt(T handle, long offset) {
        return MEMORY.readVolatileInt(handle, offset);
    }

    @Override
    public long readVolatileLong(T handle, long offset) {
        return MEMORY.readVolatileLong(handle, offset);
    }

    @Override
    public void writeByte(T handle, long offset, byte i8) {
        MEMORY.writeByte(handle, offset, i8);
    }

    @Override
    public void writeShort(T handle, long offset, short i) {
        MEMORY.writeShort(handle, offset, i);
    }

    @Override
    public void writeChar(T handle, long offset, char c) {
        MEMORY.writeShort(handle, offset, (short) c);
    }

    @Override
    public void writeInt(T handle, long offset, int i) {
        MEMORY.writeInt(handle, offset, i);
    }

    @Override
    public void writeOrderedInt(T handle, long offset, int i) {
        MEMORY.writeOrderedInt(handle, offset, i);
    }

    @Override
    public void writeLong(T handle, long offset, long i) {
        MEMORY.writeLong(handle, offset, i);
    }

    @Override
    public void writeOrderedLong(T handle, long offset, long i) {
        MEMORY.writeOrderedLong(handle, offset, i);
    }

    @Override
    public void writeFloat(T handle, long offset, float d) {
        MEMORY.writeFloat(handle, offset, d);
    }

    @Override
    public void writeDouble(T handle, long offset, double d) {
        MEMORY.writeDouble(handle, offset, d);
    }

    @Override
    public boolean compareAndSwapInt(T handle, long offset, int expected, int value) {
        return MEMORY.compareAndSwapInt(handle, offset, expected, value);
    }

    @Override
    public boolean compareAndSwapLong(T handle, long offset, long expected, long value) {
        return MEMORY.compareAndSwapLong(handle, offset, expected, value);
    }

    @Override
    public ByteOrder byteOrder(T handle) {
        return ByteOrder.nativeOrder();
    }

    @Override
    public void writeBytes(T handle, long offset, long len, byte b) {
        MEMORY.setMemory(handle, offset, len, b);
    }

    @Override
    public void zeroOut(T handle, long offset, long len) {
        MEMORY.setMemory(handle, offset, len, (byte) 0);
    }
}
