/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo.bytes;

import java.nio.ByteBuffer;

import static net.openhft.chronicle.core.UnsafeMemory.MEMORY;

/**
 * Interface providing accessor implementations for {@link ByteBuffer}.
 *
 * @param <T> the type of the buffer handle
 */
interface ByteBufferAccessor<T> extends Accessor.Full<ByteBuffer, T> {

    /**
     * Returns a ByteBufferAccessor based on whether the buffer is direct or heap-based.
     *
     * @param buffer the ByteBuffer to be accessed
     * @return a ByteBufferAccessor for the given buffer
     */
    static ByteBufferAccessor<?> unchecked(ByteBuffer buffer) {
        return buffer.isDirect() ? Direct.INSTANCE : Heap.INSTANCE;
    }

    /**
     * Returns a generic checked ByteBufferAccessor.
     *
     * @return a generic checked ByteBufferAccessor
     */
    static ByteBufferAccessor<ByteBuffer> checked() {
        return Generic.INSTANCE;
    }

    /**
     * Accessor implementation for direct {@link ByteBuffer}.
     */
    enum Direct implements ByteBufferAccessor<Void> {
        INSTANCE;

        /**
         * Returns the {@link Access} instance for direct ByteBuffer.
         *
         * @return the {@link Access} instance
         */
        @Override
        public Access<Void> access() {
            return NativeAccess.instance();
        }

        /**
         * Returns the handle for the given ByteBuffer.
         *
         * @param buffer the ByteBuffer to handle
         * @return always null for direct ByteBuffer
         */
        @Override
        public Void handle(ByteBuffer buffer) {
            return null;
        }

        /**
         * Returns the offset for the given index in the ByteBuffer.
         *
         * @param buffer      the ByteBuffer
         * @param bufferIndex the index within the buffer
         * @return the memory offset for the given index
         */
        @Override
        public long offset(ByteBuffer buffer, long bufferIndex) {
            return MEMORY.address(buffer) + bufferIndex;
        }
    }

    /**
     * Accessor implementation for heap-based {@link ByteBuffer}.
     */
    enum Heap implements ByteBufferAccessor<byte[]> {
        INSTANCE;

        /**
         * Returns the {@link Access} instance for heap ByteBuffer.
         *
         * @return the {@link Access} instance
         */
        @Override
        public Access<byte[]> access() {
            return NativeAccess.instance();
        }

        /**
         * Returns the handle for the given ByteBuffer.
         *
         * @param buffer the ByteBuffer to handle
         * @return the byte array backing the ByteBuffer
         */
        @Override
        public byte[] handle(ByteBuffer buffer) {
            return buffer.array();
        }

        /**
         * Returns the offset for the given index in the ByteBuffer.
         *
         * @param buffer      the ByteBuffer
         * @param bufferIndex the index within the buffer
         * @return the memory offset for the given index
         */
        @Override
        public long offset(ByteBuffer buffer, long bufferIndex) {
            return ArrayAccessors.BYTE_BASE + buffer.arrayOffset() + bufferIndex;
        }
    }

    /**
     * Generic accessor implementation for {@link ByteBuffer}.
     */
    enum Generic implements ByteBufferAccessor<ByteBuffer> {
        INSTANCE;

        /**
         * Returns the {@link Access} instance for generic ByteBuffer.
         *
         * @return the {@link Access} instance
         */
        @Override
        public Access<ByteBuffer> access() {
            return ByteBufferAccess.INSTANCE;
        }

        /**
         * Returns the handle for the given ByteBuffer.
         *
         * @param buffer the ByteBuffer to handle
         * @return the ByteBuffer itself
         */
        @Override
        public ByteBuffer handle(ByteBuffer buffer) {
            return buffer;
        }

        /**
         * Returns the offset for the given index in the ByteBuffer.
         *
         * @param buffer      the ByteBuffer
         * @param bufferIndex the index within the buffer
         * @return the buffer index itself
         */
        @Override
        public long offset(ByteBuffer buffer, long bufferIndex) {
            return bufferIndex;
        }
    }
}
