/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo.locks;

/**
 * Base implementation for {@link ReadWriteLockState} that routes generic lock/unlock to write
 * semantics. Concrete subclasses supply the read/write state handling.
 */
public abstract class AbstractReadWriteLockState implements ReadWriteLockState {

    /**
     * Creates a state holder; concrete subclasses define state representation.
     */
    protected AbstractReadWriteLockState() {
    }

    /**
     * Attempts to acquire a write lock.
     * This method delegates to {@link #tryWriteLock()}.
     *
     * @return true if the write lock was successfully acquired, false otherwise
     */
    @Override
    public boolean tryLock() {
        return tryWriteLock();
    }

    /**
     * Releases a write lock.
     * This method delegates to {@link #writeUnlock()}.
     */
    @Override
    public void unlock() {
        writeUnlock();
    }
}
