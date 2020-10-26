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

import net.openhft.chronicle.core.Jvm;

import java.nio.ByteOrder;

abstract class CharSequenceAccessor
        implements Accessor.Read<CharSequence, CharSequence> {

    static final Accessor.Read<? super String, ?> stringAccessor;
    static final CharSequenceAccessor LITTLE_ENDIAN = new CharSequenceAccessor() {
        @Override
        public ReadAccess<CharSequence> access() {
            return CharSequenceAccess.LittleEndianCharSequenceAccess.INSTANCE;
        }
    };
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

    private CharSequenceAccessor() {
    }

    static CharSequenceAccessor nativeCharSequenceAccessor() {
        return ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN ? LITTLE_ENDIAN : BIG_ENDIAN;
    }

    @Override
    public CharSequence handle(CharSequence source) {
        return source;
    }

    @Override
    public long offset(CharSequence source, long index) {
        return index * 2L;
    }

    @Override
    public long size(long size) {
        return size * 2L;
    }
}
