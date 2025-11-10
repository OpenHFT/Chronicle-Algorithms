//
// Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

package net.openhft.chronicle.algo.locks;

/**
 * Interface representing the state of a lock.
 * Provides methods for attempting to acquire the lock, releasing the lock, and resetting the lock state.
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
