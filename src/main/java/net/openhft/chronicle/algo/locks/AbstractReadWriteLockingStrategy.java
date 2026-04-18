/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo.locks;

import net.openhft.chronicle.algo.bytes.Access;

/**
 * Abstract base class for read-write locking strategies.
 * Implements common behavior for acquiring and releasing write locks.
 */
public abstract class AbstractReadWriteLockingStrategy implements ReadWriteLockingStrategy {

    /**
     * Attempts to acquire a write lock.
     * This method delegates to {@link #tryWriteLock(Access, Object, long)}.
     *
     * @param access The Access instance for memory operations
     * @param t      The object to lock
     * @param offset The offset within the object
     * @param <T>    The type of the object
     * @return true if the write lock was successfully acquired, false otherwise
     */
    @Override
    public <T> boolean tryLock(Access<T> access, T t, long offset) {
        return tryWriteLock(access, t, offset);
    }

    /**
     * Releases a write lock.
     * This method delegates to {@link #writeUnlock(Access, Object, long)}.
     *
     * @param access The Access instance for memory operations
     * @param t      The object to unlock
     * @param offset The offset within the object
     * @param <T>    The type of the object
     */
    @Override
    public <T> void unlock(Access<T> access, T t, long offset) {
        writeUnlock(access, t, offset);
    }

    /**
     * Checks if the given state indicates that the lock is read-locked.
     * This method delegates to {@link #readLockCount(long)} and checks if the count is greater than zero.
     *
     * @param state The current lock state
     * @return true if the lock is read-locked, false otherwise
     */
    @Override
    public boolean isReadLocked(long state) {
        return readLockCount(state) > 0;
    }
}
