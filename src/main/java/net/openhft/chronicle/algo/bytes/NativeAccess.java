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

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.nio.ByteOrder;

public final class NativeAccess<T> implements Access<T> {

    static final Unsafe U;
    private static final NativeAccess<Object> INSTANCE = new NativeAccess<>();

    static {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            U = (Unsafe) theUnsafe.get(null);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private NativeAccess() {
    }

    public static <T> NativeAccess<T> instance() {
        //noinspection unchecked
        return (NativeAccess<T>) INSTANCE;
    }

    @Override
    public byte readByte(T handle, long offset) {
        return U.getByte(handle, offset);
    }

    @Override
    public short readShort(T handle, long offset) {
        return U.getShort(handle, offset);
    }

    @Override
    public char readChar(T handle, long offset) {
        return U.getChar(handle, offset);
    }

    @Override
    public int readInt(T handle, long offset) {
        return U.getInt(handle, offset);
    }

    @Override
    public long readLong(T handle, long offset) {
        return U.getLong(handle, offset);
    }

    @Override
    public float readFloat(T handle, long offset) {
        return U.getFloat(handle, offset);
    }

    @Override
    public double readDouble(T handle, long offset) {
        return U.getDouble(handle, offset);
    }

    @Override
    public int readVolatileInt(T handle, long offset) {
        return U.getIntVolatile(handle, offset);
    }

    @Override
    public long readVolatileLong(T handle, long offset) {
        return U.getLongVolatile(handle, offset);
    }

    @Override
    public void writeByte(T handle, long offset, byte i8) {
        U.putByte(handle, offset, i8);
    }

    @Override
    public void writeShort(T handle, long offset, short i) {
        U.putShort(handle, offset, i);
    }

    @Override
    public void writeChar(T handle, long offset, char c) {
        U.putChar(handle, offset, c);
    }

    @Override
    public void writeInt(T handle, long offset, int i) {
        U.putInt(handle, offset, i);
    }

    @Override
    public void writeOrderedInt(T handle, long offset, int i) {
        U.putOrderedInt(handle, offset, i);
    }

    @Override
    public void writeLong(T handle, long offset, long i) {
        U.putLong(handle, offset, i);
    }

    @Override
    public void writeOrderedLong(T handle, long offset, long i) {
        U.putOrderedLong(handle, offset, i);
    }

    @Override
    public void writeFloat(T handle, long offset, float d) {
        U.putFloat(handle, offset, d);
    }

    @Override
    public void writeDouble(T handle, long offset, double d) {
        U.putDouble(handle, offset, d);
    }

    @Override
    public boolean compareAndSwapInt(T handle, long offset, int expected, int value) {
        return U.compareAndSwapInt(handle, offset, expected, value);
    }

    @Override
    public boolean compareAndSwapLong(T handle, long offset, long expected, long value) {
        return U.compareAndSwapLong(handle, offset, expected, value);
    }

    @Override
    public ByteOrder byteOrder(T handle) {
        return ByteOrder.nativeOrder();
    }

    @Override
    public <S> void writeFrom(
            T handle, long offset,
            ReadAccess<S> sourceAccess, S source, long sourceOffset, long len) {
        if (sourceAccess instanceof NativeAccess) {
            U.copyMemory(source, sourceOffset, handle, offset, len);

        } else {
            Access.super.writeFrom(handle, offset, sourceAccess, source, sourceOffset, len);
        }
    }

    @Override
    public void writeBytes(T handle, long offset, long len, byte b) {
        U.setMemory(handle, offset, len, b);
    }

    @Override
    public void zeroOut(T handle, long offset, long len) {
        U.setMemory(handle, offset, len, (byte) 0);
    }
}
