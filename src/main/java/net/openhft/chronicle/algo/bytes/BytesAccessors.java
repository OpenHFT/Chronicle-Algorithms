/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo.bytes;

import net.openhft.chronicle.bytes.BytesStore;

/**
 * Accessor helpers for bridging {@link BytesStore} into the Chronicle algorithms access model.
 */
final class BytesAccessors {

    // Private constructor to prevent instantiation
    private BytesAccessors() {
    }

    /**
     * Generic accessor implementation for {@link BytesStore} instances.
     *
     * @param <S> the type of BytesStore
     */
    static class Generic<S extends BytesStore<?, ?>> implements Accessor.Full<S, S> {

        static final Generic<?> INSTANCE = new Generic<>();

        /**
         * Returns the access implementation for the BytesStore.
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
         */
        @Override
        public long offset(S source, long index) {
            return index;
        }
    }
}
