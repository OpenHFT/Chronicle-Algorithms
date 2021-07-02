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

import static net.openhft.chronicle.core.UnsafeMemory.MEMORY;

final class ArrayAccessors {

    static final long BYTE_BASE;
    private static final long BOOLEAN_BASE;
    private static final long CHAR_BASE;
    private static final long SHORT_BASE;
    private static final long INT_BASE;
    private static final long LONG_BASE;

    static {
        try {
            BOOLEAN_BASE = MEMORY.arrayBaseOffset(boolean[].class);
            BYTE_BASE = MEMORY.arrayBaseOffset(byte[].class);
            CHAR_BASE = MEMORY.arrayBaseOffset(char[].class);
            SHORT_BASE = MEMORY.arrayBaseOffset(short[].class);
            INT_BASE = MEMORY.arrayBaseOffset(int[].class);
            LONG_BASE = MEMORY.arrayBaseOffset(long[].class);

        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private ArrayAccessors() {
    }

    enum Boolean implements Accessor.Full<boolean[], boolean[]> {
        INSTANCE;

        @Override
        public Access<boolean[]> access() {
            return NativeAccess.instance();
        }

        @Override
        public boolean[] handle(boolean[] source) {
            return source;
        }

        @Override
        public long offset(boolean[] source, long index) {
            return BOOLEAN_BASE + index;
        }
    }

    enum Byte implements Accessor.Full<byte[], byte[]> {
        INSTANCE;

        @Override
        public Access<byte[]> access() {
            return NativeAccess.instance();
        }

        @Override
        public byte[] handle(byte[] source) {
            return source;
        }

        @Override
        public long offset(byte[] source, long index) {
            return BYTE_BASE + index;
        }
    }

    enum Char implements Accessor.Full<char[], char[]> {
        INSTANCE;

        @Override
        public Access<char[]> access() {
            return NativeAccess.instance();
        }

        @Override
        public char[] handle(char[] source) {
            return source;
        }

        @Override
        public long offset(char[] source, long index) {
            return CHAR_BASE + (index * 2L);
        }

        @Override
        public long size(long size) {
            return size * 2L;
        }
    }

    enum Short implements Accessor.Full<short[], short[]> {
        INSTANCE;

        @Override
        public Access<short[]> access() {
            return NativeAccess.instance();
        }

        @Override
        public short[] handle(short[] source) {
            return source;
        }

        @Override
        public long offset(short[] source, long index) {
            return SHORT_BASE + (index * 2L);
        }

        @Override
        public long size(long size) {
            return size * 2L;
        }
    }

    enum Int implements Accessor.Full<int[], int[]> {
        INSTANCE;

        @Override
        public Access<int[]> access() {
            return NativeAccess.instance();
        }

        @Override
        public int[] handle(int[] source) {
            return source;
        }

        @Override
        public long offset(int[] source, long index) {
            return INT_BASE + (index * 4L);
        }

        @Override
        public long size(long size) {
            return size * 4L;
        }
    }

    enum Long implements Accessor.Full<long[], long[]> {
        INSTANCE;

        @Override
        public Access<long[]> access() {
            return NativeAccess.instance();
        }

        @Override
        public long[] handle(long[] source) {
            return source;
        }

        @Override
        public long offset(long[] source, long index) {
            return LONG_BASE + (index * 8L);
        }

        @Override
        public long size(long size) {
            return size * 8L;
        }
    }
}
