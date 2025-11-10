//
// Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

package net.openhft.chronicle.algo.locks;

import net.openhft.chronicle.algo.bytes.Access;
import net.openhft.chronicle.algo.bytes.ReadAccess;

/**
 * Vanilla implementation of a read-write lock with waits.
 * This class provides the locking mechanism to handle read and write operations with wait strategies.
 */
public final class VanillaReadWriteWithWaitsLockingStrategy extends AbstractReadWriteLockingStrategy
        implements ReadWriteWithWaitsLockingStrategy {

    // Constants defining lock limits and masks
    static final int RW_LOCK_LIMIT = 30; // Limit for read-write locks
    static final long RW_READ_LOCKED = 1L; // Bit mask for read lock
    static final long RW_WRITE_WAITING = 1L << RW_LOCK_LIMIT; // Bit mask for write waiting
    static final long RW_WRITE_LOCKED = 1L << 2 * RW_LOCK_LIMIT; // Bit mask for write lock
    static final int RW_LOCK_MASK = (1 << RW_LOCK_LIMIT) - 1; // Mask for read-write locks
    private static final ReadWriteWithWaitsLockingStrategy INSTANCE =
            new VanillaReadWriteWithWaitsLockingStrategy();

    // Private constructor to prevent instantiation
    private VanillaReadWriteWithWaitsLockingStrategy() {
    }

    /**
     * Returns the singleton instance of this locking strategy.
     *
     * @return The singleton instance of the VanillaReadWriteWithWaitsLockingStrategy
     */
    public static ReadWriteWithWaitsLockingStrategy instance() {
        return INSTANCE;
    }

    /**
     * Returns the number of read locks from the lock state.
     *
     * @param lock The lock state
     * @return The number of read locks
     */
    static int rwReadLocked(long lock) {
        return (int) (lock & RW_LOCK_MASK);
    }

    /**
     * Returns the number of write waits from the lock state.
     *
     * @param lock The lock state
     * @return The number of write waits
     */
    static int rwWriteWaiting(long lock) {
        return (int) ((lock >>> RW_LOCK_LIMIT) & RW_LOCK_MASK);
    }

    /**
     * Returns the number of write locks from the lock state.
     *
     * @param lock The lock state
     * @return The number of write locks
     */
    static int rwWriteLocked(long lock) {
        return (int) (lock >>> (2 * RW_LOCK_LIMIT));
    }

    /**
     * Reads the lock state from the given offset.
     *
     * @param access The ReadAccess instance
     * @param t      The input handle
     * @param offset The offset within the input
     * @param <T>    The type of the input handle
     * @return The lock state read from the input
     */
    static <T> long read(ReadAccess<T> access, T t, long offset) {
        return access.readVolatileLong(t, offset);
    }

    /**
     * Performs a compare-and-swap operation on the lock state.
     *
     * @param access   The Access instance
     * @param t        The input handle
     * @param offset   The offset within the input
     * @param expected The expected value
     * @param x        The new value
     * @param <T>      The type of the input handle
     * @return True if the operation was successful, false otherwise
     */
    static <T> boolean cas(Access<T> access, T t, long offset, long expected, long x) {
        return access.compareAndSwapLong(t, offset, expected, x);
    }

    /**
     * Attempts to acquire a read lock.
     *
     * @param access The Access instance
     * @param t      The input handle
     * @param offset The offset within the input
     * @param <T>    The type of the input handle
     * @return True if the read lock was acquired, false otherwise
     */
    @Override
    public <T> boolean tryReadLock(Access<T> access, T t, long offset) {
        long lock = read(access, t, offset); // Reads the current lock state
        int writersWaiting = rwWriteWaiting(lock); // Gets the number of writers waiting
        int writersLocked = rwWriteLocked(lock); // Gets the number of writers locked
        // Readers wait for waiting writers
        if (writersLocked <= 0 && writersWaiting <= 0) {
            // Increment readers locked
            int readersLocked = rwReadLocked(lock);
            if (readersLocked >= RW_LOCK_MASK)
                throw new IllegalMonitorStateException("readersLocked has reached a limit of " +
                        readersLocked);
            // Attempts to acquire the read lock
            return cas(access, t, offset, lock, lock + RW_READ_LOCKED);
        }
        return false; // Read lock acquisition failed
    }

    /**
     * Attempts to acquire a write lock.
     *
     * @param access The Access instance
     * @param t      The input handle
     * @param offset The offset within the input
     * @param <T>    The type of the input handle
     * @return True if the write lock was acquired, false otherwise
     */
    @Override
    public <T> boolean tryWriteLock(Access<T> access, T t, long offset) {
        long lock = read(access, t, offset); // Reads the current lock state
        int readersLocked = rwReadLocked(lock); // Gets the number of readers locked
        int writersLocked = rwWriteLocked(lock); // Gets the number of writers locked
        // Writers don't wait for waiting readers
        if (readersLocked <= 0 && writersLocked <= 0) {
            // Attempts to acquire the write lock
            return cas(access, t, offset, lock, lock + RW_WRITE_LOCKED);
        }
        return false; // Write lock acquisition failed
    }

    /**
     * Attempts to upgrade a read lock to a write lock.
     *
     * @param access The Access instance
     * @param t      The input handle
     * @param offset The offset within the input
     * @param <T>    The type of the input handle
     * @return True if the upgrade was successful, false otherwise
     */
    @Override
    public <T> boolean tryUpgradeReadToWriteLock(Access<T> access, T t, long offset) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    /**
     * Releases a read lock.
     *
     * @param access The Access instance
     * @param t      The input handle
     * @param offset The offset within the input
     * @param <T>    The type of the input handle
     */
    @Override
    public <T> void readUnlock(Access<T> access, T t, long offset) {
        for (; ; ) {
            long lock = read(access, t, offset); // Reads the current lock state
            int readersLocked = rwReadLocked(lock); // Gets the number of readers locked
            if (readersLocked <= 0)
                throw new IllegalMonitorStateException("readerLock underflow");
            // Attempts to release the read lock
            if (cas(access, t, offset, lock, lock - RW_READ_LOCKED))
                return;
        }
    }

    /**
     * Releases a write lock.
     *
     * @param access The Access instance
     * @param t      The input handle
     * @param offset The offset within the input
     * @param <T>    The type of the input handle
     */
    @Override
    public <T> void writeUnlock(Access<T> access, T t, long offset) {
        for (; ; ) {
            long lock = read(access, t, offset); // Reads the current lock state
            int writersLocked = rwWriteLocked(lock); // Gets the number of writers locked
            if (writersLocked != 1)
                throw new IllegalMonitorStateException("writersLock underflow " + writersLocked);
            // Attempts to release the write lock
            if (cas(access, t, offset, lock, lock - RW_WRITE_LOCKED))
                return;
        }
    }

    /**
     * Downgrades a write lock to a read lock.
     *
     * @param access The Access instance
     * @param t      The input handle
     * @param offset The offset within the input
     * @param <T>    The type of the input handle
     */
    @Override
    public <T> void downgradeWriteToReadLock(Access<T> access, T t, long offset) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    /**
     * Checks if the given state is write locked.
     *
     * @param state The state to check
     * @return True if the state is write locked, false otherwise
     */
    @Override
    public boolean isWriteLocked(long state) {
        // Checks if the state is write locked
        return rwWriteLocked(state) > 0;
    }

    /**
     * Retrieves the count of read locks from the given state.
     *
     * @param state The state to check
     * @return The number of read locks
     */
    @Override
    public int readLockCount(long state) {
        // Returns the number of read locks from the given state
        return rwReadLocked(state);
    }

    /**
     * Resets the lock state.
     *
     * @param access The Access instance
     * @param t      The input handle
     * @param offset The offset within the input
     * @param <T>    The type of the input handle
     */
    @Override
    public <T> void reset(Access<T> access, T t, long offset) {
        // Resets the lock state at the specified offset
        access.writeOrderedLong(t, offset, 0L);
    }

    /**
     * Resets the lock state while keeping the waits.
     *
     * @param access The Access instance
     * @param t      The input handle
     * @param offset The offset within the input
     * @param <T>    The type of the input handle
     */
    @Override
    public <T> void resetKeepingWaits(Access<T> access, T t, long offset) {
        while (true) {
            long lock = read(access, t, offset); // Reads the current lock state
            long onlyWaits = lock & ((long) RW_LOCK_MASK) << RW_LOCK_LIMIT; // Extracts the wait state
            // Attempts to reset the lock state while keeping the waits
            if (cas(access, t, offset, lock, onlyWaits))
                return;
        }
    }

    /**
     * Registers a wait operation.
     *
     * @param access The Access instance
     * @param t      The input handle
     * @param offset The offset within the input
     * @param <T>    The type of the input handle
     */
    @Override
    public <T> void registerWait(Access<T> access, T t, long offset) {
        for (; ; ) {
            long lock = read(access, t, offset); // Reads the current lock state
            int writersWaiting = rwWriteWaiting(lock); // Gets the number of writers waiting
            if (writersWaiting >= RW_LOCK_MASK)
                throw new IllegalMonitorStateException("writersWaiting has reached a limit of " +
                        writersWaiting);
            // Attempts to register the wait
            if (cas(access, t, offset, lock, lock + RW_WRITE_WAITING))
                break;
        }
    }

    /**
     * Deregisters a wait operation.
     *
     * @param access The Access instance
     * @param t      The input handle
     * @param offset The offset within the input
     * @param <T>    The type of the input handle
     */
    @Override
    public <T> void deregisterWait(Access<T> access, T t, long offset) {
        for (; ; ) {
            long lock = read(access, t, offset); // Reads the current lock state
            int writersWaiting = rwWriteWaiting(lock); // Gets the number of writers waiting
            if (writersWaiting <= 0)
                throw new IllegalMonitorStateException("writersWaiting has underflowed");
            // Attempts to deregister the wait
            if (cas(access, t, offset, lock, lock - RW_WRITE_WAITING))
                break;
        }
    }

    /**
     * Attempts to acquire the write lock and deregister wait if successful.
     *
     * @param access The Access instance
     * @param t      The input handle
     * @param offset The offset within the input
     * @param <T>    The type of the input handle
     * @return True if the write lock was acquired and wait was deregistered, false otherwise
     */
    @Override
    public <T> boolean tryWriteLockAndDeregisterWait(
            Access<T> access, T t, long offset) {
        long lock = read(access, t, offset); // Reads the current lock state
        int readersLocked = rwReadLocked(lock); // Gets the number of readers locked
        int writersWaiting = rwWriteWaiting(lock); // Gets the number of writers waiting
        int writersLocked = rwWriteLocked(lock); // Gets the number of writers locked
        if (readersLocked <= 0 && writersLocked <= 0) {
            // increment readers locked.
            if (writersWaiting <= 0)
                throw new IllegalMonitorStateException("writersWaiting has underflowed");
            // Adds to the write lock count and decreases the write waiting count
            return cas(access, t, offset, lock, lock + RW_WRITE_LOCKED - RW_WRITE_WAITING);
        }
        return false; // Write lock acquisition and wait deregistration failed
    }

    /**
     * Attempts to upgrade a read lock to a write lock and deregister wait if successful.
     *
     * @param access The Access instance
     * @param t      The input handle
     * @param offset The offset within the input
     * @param <T>    The type of the input handle
     * @return True if the upgrade and wait deregistration were successful, false otherwise
     */
    @Override
    public <T> boolean tryUpgradeReadToWriteLockAndDeregisterWait(
            Access<T> access, T t, long offset) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    /**
     * Resets the lock state to the initial state.
     *
     * @return The initial state value
     */
    @Override
    public long resetState() {
        return 0L; // Resets the state
    }

    /**
     * Retrieves the current lock state.
     *
     * @param access The ReadAccess instance
     * @param t      The input handle
     * @param offset The offset within the input
     * @param <T>    The type of the input handle
     * @return The current lock state
     */
    @Override
    public <T> long getState(ReadAccess<T> access, T t, long offset) {
        return read(access, t, offset); // Reads the current lock state
    }

    /**
     * Retrieves the count of waits from the given state.
     *
     * @param state The state to check
     * @return The number of waits
     */
    @Override
    public int waitCount(long state) {
        return rwWriteWaiting(state); // Returns the number of waits from the given state
    }

    /**
     * Checks if the given state is locked (either read or write locked).
     *
     * @param state The state to check
     * @return True if the state is locked, false otherwise
     */
    @Override
    public boolean isLocked(long state) {
        return isReadLocked(state) || isWriteLocked(state); // Checks if the state is locked
    }

    /**
     * Retrieves the total lock count from the given state, including read and write locks.
     *
     * @param state The state to check
     * @return The total lock count
     */
    @Override
    public int lockCount(long state) {
        return rwReadLocked(state) + rwWriteLocked(state); // Returns the total lock count
    }

    /**
     * Returns a string representation of the current lock state.
     *
     * @param state The state to represent
     * @return A string representation of the lock state
     */
    @Override
    public String toString(long state) {
        return "[read locks = " + readLockCount(state) +
                ", write locked = " + isWriteLocked(state) +
                ", waits = " + waitCount(state) + "]";
    }

    /**
     * Returns the size in bytes of the lock state representation.
     *
     * @return The size in bytes
     */
    @Override
    public int sizeInBytes() {
        return 8; // Returns the size in bytes of the lock state representation
    }
}
