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

import java.nio.ByteOrder;

/**
 * A functional interface that defines a common access method for determining the byte order of a given handle.
 *
 * @param <T> the type of the handle
 */
@FunctionalInterface
interface AccessCommon<T> {

    /**
     * Returns the byte order of the given handle.
     *
     * @param handle the handle whose byte order is to be determined
     * @return the byte order of the given handle
     */
    ByteOrder byteOrder(T handle);
}
