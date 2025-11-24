/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo.locks;

/**
 * Abstraction over a lock's backing state that can be stored and manipulated via {@link LockingStrategy}.
 * <p>
 * Implementations hide whether the state lives in memory, on-heap structures or elsewhere; the
 * strategy knows how to interpret {@link #getState()}.
 */
public interface LockState {

    /**
     * Attempts to acquire the lock.
     *
     * @return {@code true} if the lock was successfully acquired, {@code false} otherwise
     */
    boolean tryLock();

    /**
     * Releases the lock.
     */
    void unlock();

    /**
     * Resets the lock state.
     */
    void reset();

    /**
     * Retrieves the current state of the lock.
     *
     * @return the current state of the lock
     */
    long getState();

    /**
     * Retrieves the locking strategy associated with this lock state.
     *
     * @return the locking strategy
     */
    LockingStrategy lockingStrategy();
}
