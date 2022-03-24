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

public interface ReadAccess<T> extends AccessCommon<T> {

    static ReadAccess<Void> zeros() {
        return ZeroAccess.INSTANCE;
    }

    default boolean readBoolean(T handle, long offset) {
        return readByte(handle, offset) != 0;
    }

    byte readByte(T handle, long offset);

    default int readUnsignedByte(T handle, long offset) {
        return readByte(handle, offset) & 0xFF;
    }

    short readShort(T handle, long offset);

    default int readUnsignedShort(T handle, long offset) {
        return readShort(handle, offset) & 0xFFFF;
    }

    default char readChar(T handle, long offset) {
        return (char) readShort(handle, offset);
    }

    int readInt(T handle, long offset);

    default long readUnsignedInt(T handle, long offset) {
        return readInt(handle, offset) & 0xFFFFFFFFL;
    }

    long readLong(T handle, long offset);

    /**
     * Default implementation: {@code Float.intBitsToFloat(readInt(handle, offset))}.
     */
    default float readFloat(T handle, long offset) {
        return Float.intBitsToFloat(readInt(handle, offset));
    }

    /**
     * Default implementation: {@code Double.longBitsToDouble(readLong(handle, offset))}.
     */
    default double readDouble(T handle, long offset) {
        return Double.longBitsToDouble(readLong(handle, offset));
    }

    default String printable(T handle, long offset) {
        int b = readUnsignedByte(handle, offset);
        if (b == 0)
            return "\u0660";
        else if (b < 21)
            return String.valueOf((char) (b + 0x2487));
        else
            return String.valueOf((char) b);
    }

    /**
     * Default implementation: throws {@code UnsupportedOperationException}.
     */
    default int readVolatileInt(T handle, long offset) {
        throw new UnsupportedOperationException();
    }

    /**
     * Default implementation: throws {@code UnsupportedOperationException}.
     */
    default long readVolatileLong(T handle, long offset) {
        throw new UnsupportedOperationException();
    }

}
