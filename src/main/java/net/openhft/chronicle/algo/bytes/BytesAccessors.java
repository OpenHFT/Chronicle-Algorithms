/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo.bytes;

import net.openhft.chronicle.bytes.BytesStore;

/**
 * Utility class for providing accessor implementations for {@link BytesStore}.
 */
final class BytesAccessors {

    // Private constructor to prevent instantiation
    private BytesAccessors() {
    }

    /**
     * Generic accessor implementation for {@link BytesStore}.
     *
     * @param <S> the type of BytesStore
     */
    static class Generic<S extends BytesStore<?, ?>> implements Accessor.Full<S, S> {

        static final Generic<?> INSTANCE = new Generic<>();

        /**
         * Returns the access implementation for the BytesStore.
         *
         * @return the access implementation for the BytesStore
         */
        @SuppressWarnings("unchecked")
        @Override
        public Access<S> access() {
            return (Access<S>) BytesAccesses.Full.INSTANCE;
        }

        /**
         * Returns the handle for the given source BytesStore.
         *
         * @param source the source BytesStore
         * @return the handle for the source BytesStore
         */
        @Override
        public S handle(S source) {
            return source;
        }

        /**
         * Converts the index in the source domain to an access offset.
         *
         * @param source the source BytesStore
         * @param index  the index in the source type domain
         * @return the offset for access corresponding to the given index
         */
        @Override
        public long offset(S source, long index) {
            return index;
        }
    }
}
