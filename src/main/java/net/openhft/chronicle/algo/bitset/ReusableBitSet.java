/*
 * Copyright 2014 Higher Frequency Trading http://www.higherfrequencytrading.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.algo.bitset;

import net.openhft.chronicle.bytes.Access;

@SuppressWarnings("unchecked")
public class ReusableBitSet implements BitSet {
    protected BitSetFrame frame;
    protected Access access;
    protected Object handle;
    protected long offset;

    public <T> ReusableBitSet(
            BitSetFrame frame, Access<T> access, T handle, long offset) {
        reuse(frame, access, handle, offset);
    }

    public <T> ReusableBitSet reuse(
            BitSetFrame frame, Access<T> access, T handle, long offset) {
        this.frame = frame;
        this.access = access;
        this.handle = handle;
        this.offset = offset;
        return this;
    }


    @Override
    public void flip(long bitIndex) {
        frame.flip(access, handle, offset, bitIndex);
    }

    @Override
    public void flipRange(long fromIndex, long toIndex) {
        frame.flipRange(access, handle, offset, fromIndex, toIndex);
    }

    @Override
    public void set(long bitIndex) {
        frame.set(access, handle, offset, bitIndex);
    }

    @Override
    public boolean setIfClear(long bitIndex) {
        return frame.setIfClear(access, handle, offset, bitIndex);
    }

    @Override
    public boolean clearIfSet(long bitIndex) {
        return frame.clearIfSet(access, handle, offset, bitIndex);
    }

    @Override
    public void setRange(long fromIndex, long toIndex) {
        frame.setRange(access, handle, offset, fromIndex, toIndex);
    }

    @Override
    public boolean isRangeSet(long fromIndex, long toIndex) {
        return frame.isRangeSet(access, handle, offset, fromIndex, toIndex);
    }

    @Override
    public void setAll() {
        frame.setAll(access, handle, offset);
    }

    @Override
    public void clear(long bitIndex) {
        frame.clear(access, handle, offset, bitIndex);
    }

    @Override
    public void clearRange(long fromIndex, long toIndex) {
        frame.clearRange(access, handle, offset, fromIndex, toIndex);
    }

    @Override
    public boolean isRangeClear(long fromIndex, long toIndex) {
        return frame.isRangeClear(access, handle, offset, fromIndex, toIndex);
    }

    @Override
    public void clearAll() {
        frame.clearAll(access, handle, offset);
    }

    @Override
    public boolean get(long bitIndex) {
        return frame.get(access, handle, offset, bitIndex);
    }

    @Override
    public long nextSetBit(long fromIndex) {
        return frame.nextSetBit(access, handle, offset, fromIndex);
    }

    @Override
    public long nextClearBit(long fromIndex) {
        return frame.nextClearBit(access, handle, offset, fromIndex);
    }

    @Override
    public long previousSetBit(long fromIndex) {
        return frame.previousSetBit(access, handle, offset, fromIndex);
    }

    @Override
    public long previousClearBit(long fromIndex) {
        return frame.previousClearBit(access, handle, offset, fromIndex);
    }

    @Override
    public long logicalSize() {
        return frame.logicalSize();
    }

    @Override
    public long cardinality() {
        return frame.cardinality(access, handle, offset);
    }

    @Override
    public long setNextClearBit(long fromIndex) {
        return frame.setNextClearBit(access, handle, offset, fromIndex);
    }

    @Override
    public long clearNextSetBit(long fromIndex) {
        return frame.clearNextSetBit(access, handle, offset, fromIndex);
    }

    @Override
    public long setPreviousClearBit(long fromIndex) {
        return frame.setPreviousClearBit(access, handle, offset, fromIndex);
    }

    @Override
    public long clearPreviousSetBit(long fromIndex) {
        return frame.clearPreviousSetBit(access, handle, offset, fromIndex);
    }

    @Override
    public long setNextNContinuousClearBits(long fromIndex, int numberOfBits) {
        return frame.setNextNContinuousClearBits(access, handle, offset, fromIndex, numberOfBits);
    }

    @Override
    public long clearNextNContinuousSetBits(long fromIndex, int numberOfBits) {
        return frame.clearNextNContinuousSetBits(access, handle, offset, fromIndex, numberOfBits);
    }

    @Override
    public long setPreviousNContinuousClearBits(long fromIndex, int numberOfBits) {
        return frame.setPreviousNContinuousClearBits(access, handle, offset,
                fromIndex, numberOfBits);
    }

    @Override
    public long clearPreviousNContinuousSetBits(long fromIndex, int numberOfBits) {
        return frame.clearPreviousNContinuousSetBits(access, handle, offset,
                fromIndex, numberOfBits);
    }

    protected class Bits implements BitSet.Bits {
        protected BitSetFrame.Bits frameBits;

        public Bits(BitSetFrame.Bits frameBits) {
            this.frameBits = frameBits;
        }

        @Override
        public BitSet.Bits reset() {
            frameBits.reset(access, handle, offset);
            return this;
        }

        @Override
        public long next() {
            return frameBits.next(access, handle, offset);
        }
    }

    @Override
    public Bits setBits() {
        return new Bits(frame.setBits());
    }
}
