/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo.locks;

import net.openhft.chronicle.algo.bytes.Access;

/**
 * Strategy for manipulating read/write lock state stored in an addressable resource.
 * <p>
 * Supplies primitives for read/write acquisition, upgrades, downgrades, and introspection of the
 * current state; higher-level code composes these to implement lock protocols.
 */
public interface ReadWriteLockingStrategy extends LockingStrategy {

    /**
     * Attempts to acquire a read lock.
     *
     * @param <T>    the type of the object being locked
     * @param access the access mechanism
     * @param t      the object being locked
     * @param offset the offset within the object
     * @return {@code true} if the read lock was successfully acquired, {@code false} otherwise
     */
    <T> boolean tryReadLock(Access<T> access, T t, long offset);

    /**
     * Attempts to acquire a write lock.
     *
     * @param <T>    the type of the object being locked
     * @param access the access mechanism
     * @param t      the object being locked
     * @param offset the offset within the object
     * @return {@code true} if the write lock was successfully acquired, {@code false} otherwise
     */
    <T> boolean tryWriteLock(Access<T> access, T t, long offset);

    /**
     * Attempts to upgrade a read lock to a write lock.
     *
     * @param <T>    the type of the object being locked
     * @param access the access mechanism
     * @param t      the object being locked
     * @param offset the offset within the object
     * @return {@code true} if the lock was successfully upgraded to a write lock, {@code false} otherwise
     */
    <T> boolean tryUpgradeReadToWriteLock(Access<T> access, T t, long offset);

    /**
     * Releases a read lock.
     *
     * @param <T>    the type of the object being locked
     * @param access the access mechanism
     * @param t      the object being locked
     * @param offset the offset within the object
     */
    <T> void readUnlock(Access<T> access, T t, long offset);

    /**
     * Releases a write lock.
     *
     * @param <T>    the type of the object being locked
     * @param access the access mechanism
     * @param t      the object being locked
     * @param offset the offset within the object
     */
    <T> void writeUnlock(Access<T> access, T t, long offset);

    /**
     * Downgrades a write lock to a read lock.
     *
     * @param <T>    the type of the object being locked
     * @param access the access mechanism
     * @param t      the object being locked
     * @param offset the offset within the object
     */
    <T> void downgradeWriteToReadLock(Access<T> access, T t, long offset);

    /**
     * Checks if the state indicates a read lock.
     *
     * @param state the current lock state
     * @return {@code true} if the state indicates a read lock, {@code false} otherwise
     */
    boolean isReadLocked(long state);

    /**
     * Checks if the state indicates a write lock.
     *
     * @param state the current lock state
     * @return {@code true} if the state indicates a write lock, {@code false} otherwise
     */
    boolean isWriteLocked(long state);

    /**
     * Returns the number of read locks currently held.
     *
     * @param state the current lock state
     * @return the number of read locks held
     */
    int readLockCount(long state);
}
