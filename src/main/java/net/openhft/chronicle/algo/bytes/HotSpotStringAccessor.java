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

import java.lang.reflect.Field;

final class HotSpotStringAccessor<T> implements Accessor.Read<String, T> {
    public static final HotSpotStringAccessor<char[]> JAVA8 = new HotSpotStringAccessor<>();
    public static final HotSpotStringAccessor<byte[]> JAVA9PLUS = new HotSpotStringAccessor<>();

    private static final long valueOffset;

    static {
        try {
            Field valueField = String.class.getDeclaredField("value");
            valueOffset = NativeAccess.U.objectFieldOffset(valueField);
        } catch (NoSuchFieldException e) {
            throw new AssertionError(e);
        }
    }

    private HotSpotStringAccessor() {
    }

    @Override
    public ReadAccess<T> access() {
        return NativeAccess.instance();
    }

    @Override
    public T handle(String source) {
        return (T) NativeAccess.U.getObject(source, valueOffset);
    }

    @Override
    public long offset(String source, long index) {
        return ArrayAccessors.Char.INSTANCE.offset(null, index);
    }

    @Override
    public long size(long size) {
        return size * 2L;
    }
}
