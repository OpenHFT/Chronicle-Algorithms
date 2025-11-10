//
// Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

package net.openhft.chronicle.algo.locks;

/**
 * Abstract base class for managing read-write lock state.
 * Implements common behavior for acquiring and releasing write locks.
 */
public abstract class AbstractReadWriteLockState implements ReadWriteLockState {

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
