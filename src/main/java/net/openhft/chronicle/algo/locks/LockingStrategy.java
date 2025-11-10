//
// Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

package net.openhft.chronicle.algo.locks;

import net.openhft.chronicle.algo.bytes.Access;
import net.openhft.chronicle.algo.bytes.ReadAccess;

/**
 * Interface representing a locking strategy for managing locks on resources.
 */
public interface LockingStrategy {

    /**
     * Attempts to acquire a lock on the specified resource.
     *
     * @param access the access mechanism for the resource
     * @param t      the target resource
     * @param offset the offset within the resource
     * @param <T>    the type of the target resource
     * @return {@code true} if the lock was successfully acquired, {@code false} otherwise
     */
    <T> boolean tryLock(Access<T> access, T t, long offset);

    /**
     * Releases the lock on the specified resource.
     *
     * @param access the access mechanism for the resource
     * @param t      the target resource
     * @param offset the offset within the resource
     * @param <T>    the type of the target resource
     */
    <T> void unlock(Access<T> access, T t, long offset);

    /**
     * Resets the lock on the specified resource.
     *
     * @param access the access mechanism for the resource
     * @param t      the target resource
     * @param offset the offset within the resource
     * @param <T>    the type of the target resource
     */
    <T> void reset(Access<T> access, T t, long offset);

    /**
     * Resets the state of the lock.
     *
     * @return the reset state value
     */
    long resetState();

    /**
     * Retrieves the state of the lock on the specified resource.
     *
     * @param access the read access mechanism for the resource
     * @param t      the target resource
     * @param offset the offset within the resource
     * @param <T>    the type of the target resource
     * @return the state of the lock
     */
    <T> long getState(ReadAccess<T> access, T t, long offset);

    /**
     * Checks if the specified state represents a locked state.
     *
     * @param state the state to check
     * @return {@code true} if the state represents a locked state, {@code false} otherwise
     */
    boolean isLocked(long state);

    /**
     * Returns the number of times the lock has been acquired.
     *
     * @param state the state of the lock
     * @return the lock count
     */
    int lockCount(long state);

    /**
     * Converts the lock state to a string representation.
     *
     * @param state the state of the lock
     * @return the string representation of the lock state
     */
    String toString(long state);

    /**
     * Returns the size of the lock state in bytes.
     *
     * @return the size of the lock state in bytes
     */
    int sizeInBytes();
}
