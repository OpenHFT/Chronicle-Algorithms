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

package net.openhft.chronicle.algorithms.measures;

import net.openhft.chronicle.algo.bytes.NativeAccess;
import net.openhft.chronicle.algo.hashing.LongHashFunction;
import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.bytes.PointerBytesStore;
import net.openhft.chronicle.bytes.algo.OptimisedBytesStoreHash;

import java.security.SecureRandom;
import java.util.Random;

/**
 * Created by peter on 21/08/15.
 */
public enum AddressWrappers implements AddressWrapper {
    RANDOM {
        Random rand = new Random();

        @Override
        public void setAddress(long address, long length) {
        }
@Override
        public long hash() {
            return rand.nextLong();
        }
    },
    SECURE_RANDOM {
        SecureRandom rand = new SecureRandom();

        @Override
        public void setAddress(long address, long length) {
        }
@Override
        public long hash() {
            return rand.nextLong();
        }
    },

    VANILLA {
        int length;
        Bytes bytes;

        @Override
        public void setAddress(long address, long length) {
            this.length = (int) length;
            PointerBytesStore pbs = BytesStore.nativePointer();
            pbs.set(address, length);
            bytes = pbs.bytesForRead().unchecked(true);
        }
@Override
        public long hash() {
            return OptimisedBytesStoreHash.applyAsLong32bytesMultiple(bytes, length);
        }
    },
    CITY_1_1 {
        long address,length;

        @Override
        public void setAddress(long address, long length) {
            this.address = address;
            this.length = length;
        }
@Override
        public long hash() {
            return LongHashFunction.city_1_1().hash((Object) null, NativeAccess.instance(), address, length);
        }
    },
    MURMUR_3 {
        long address,length;

        @Override
        public void setAddress(long address, long length) {
            this.address = address;
            this.length = length;
        }
@Override
        public long hash() {
            return LongHashFunction.murmur_3().hash((Object) null, NativeAccess.instance(), address, length);
        }
    },
    STRING32 {
        Bytes bytes;

        @Override
        public void setAddress(long address, long length) {
            PointerBytesStore pbs = BytesStore.nativePointer();
            pbs.set(address, length);
            bytes = pbs.bytesForRead().unchecked(true);
        }
@Override
        public long hash() {
            int hc = 0;
            for (int i = 0; i < bytes.length(); i++)
                hc = hc * 31 + bytes.charAt(i);

            return hash(hc);
        }
// from the hash() function in HashMap
        int hash(int h) {
            // This function ensures that hashCodes that differ only by
            // constant multiples at each bit position have a bounded
            // number of collisions (approximately 8 at default load factor).
            h ^= (h >>> 20) ^ (h >>> 12);
            return h ^ (h >>> 7) ^ (h >>> 4);
        }
    },
    STRING64 {
        Bytes bytes;

        @Override
        public void setAddress(long address, long length) {
            PointerBytesStore pbs = BytesStore.nativePointer();
            pbs.set(address, length);
            bytes = pbs.bytesForRead().unchecked(true);
        }
@Override
        public long hash() {
            long hc = 0;
            for (int i = 0; i < bytes.length(); i++)
                hc = hc * 31 + bytes.charAt(i);
            return hash(hc);
        }
// based on the hash() function in HashMap
        long hash(long h) {
            h ^= (h >>> 41) ^ (h >>> 23);
            return h ^ (h >>> 14) ^ (h >>> 7);
        }
    },
    STRING32_WITHOUT_AGITATE {
        Bytes bytes;

        @Override
        public void setAddress(long address, long length) {
            PointerBytesStore pbs = BytesStore.nativePointer();
            pbs.set(address, length);
            bytes = pbs.bytesForRead().unchecked(true);
        }
@Override
        public long hash() {
            int hc = 0;
            for (int i = 0; i < bytes.length(); i++)
                hc = hc * 31 + bytes.charAt(i);

            return hc;
        }
    }
}
