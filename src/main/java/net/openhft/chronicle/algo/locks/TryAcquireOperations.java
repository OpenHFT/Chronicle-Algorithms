/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo.locks;

import net.openhft.chronicle.algo.bytes.Access;

/**
 * Utility class providing various {@link TryAcquireOperation} implementations
 * for different locking strategies.
 */
public final class TryAcquireOperations {

    // TryAcquireOperation for LockingStrategy
    private static final TryAcquireOperation<LockingStrategy> LOCK =
            new TryAcquireOperation<LockingStrategy>() {
                @Override
                public <T> boolean tryAcquire(LockingStrategy strategy,
                                              Access<T> access, T obj, long offset) {
                    return strategy.tryLock(access, obj, offset);
                }
            };

    // TryAcquireOperation for ReadWriteLockingStrategy - Read Lock
    private static final TryAcquireOperation<ReadWriteLockingStrategy> READ_LOCK =
            new TryAcquireOperation<ReadWriteLockingStrategy>() {
                @Override
                public <T> boolean tryAcquire(ReadWriteLockingStrategy strategy,
                                              Access<T> access, T obj, long offset) {
                    return strategy.tryReadLock(access, obj, offset);
                }
            };

    // TryAcquireOperation for ReadWriteLockingStrategy - Upgrade Read to Write Lock
    private static final TryAcquireOperation<ReadWriteLockingStrategy> UPGRADE_READ_TO_WRITE_LOCK =
            new TryAcquireOperation<ReadWriteLockingStrategy>() {
                @Override
                public <T> boolean tryAcquire(ReadWriteLockingStrategy strategy,
                                              Access<T> access, T obj, long offset) {
                    return strategy.tryUpgradeReadToWriteLock(access, obj, offset);
                }
            };

    // TryAcquireOperation for ReadWriteLockingStrategy - Write Lock
    private static final TryAcquireOperation<ReadWriteLockingStrategy> WRITE_LOCK =
            new TryAcquireOperation<ReadWriteLockingStrategy>() {
                @Override
                public <T> boolean tryAcquire(ReadWriteLockingStrategy strategy,
                                              Access<T> access, T obj, long offset) {
                    return strategy.tryWriteLock(access, obj, offset);
                }
            };

    // TryAcquireOperation for ReadWriteWithWaitsLockingStrategy - Upgrade Read to Write Lock and Deregister Wait
    private static final TryAcquireOperation<ReadWriteWithWaitsLockingStrategy>
            UPGRADE_READ_TO_WRITE_LOCK_AND_DEREGISTER_WAIT =
            new TryAcquireOperation<ReadWriteWithWaitsLockingStrategy>() {
                @Override
                public <T> boolean tryAcquire(ReadWriteWithWaitsLockingStrategy strategy,
                                              Access<T> access, T obj, long offset) {
                    return strategy.tryUpgradeReadToWriteLockAndDeregisterWait(access, obj, offset);
                }
            };

    // TryAcquireOperation for ReadWriteWithWaitsLockingStrategy - Write Lock and Deregister Wait
    private static final TryAcquireOperation<ReadWriteWithWaitsLockingStrategy>
            WRITE_LOCK_AND_DEREGISTER_WAIT =
            new TryAcquireOperation<ReadWriteWithWaitsLockingStrategy>() {
                @Override
                public <T> boolean tryAcquire(ReadWriteWithWaitsLockingStrategy strategy,
                                              Access<T> access, T obj, long offset) {
                    return strategy.tryWriteLockAndDeregisterWait(access, obj, offset);
                }
            };

    // TryAcquireOperation for ReadWriteUpdateLockingStrategy - Update Lock
    private static final TryAcquireOperation<ReadWriteUpdateLockingStrategy> UPDATE_LOCK =
            new TryAcquireOperation<ReadWriteUpdateLockingStrategy>() {
                @Override
                public <T> boolean tryAcquire(ReadWriteUpdateLockingStrategy strategy,
                                              Access<T> access, T obj, long offset) {
                    return strategy.tryUpdateLock(access, obj, offset);
                }
            };

    // TryAcquireOperation for ReadWriteUpdateLockingStrategy - Upgrade Read to Update Lock
    private static final TryAcquireOperation<ReadWriteUpdateLockingStrategy>
            UPGRADE_READ_TO_UPDATE_LOCK =
            new TryAcquireOperation<ReadWriteUpdateLockingStrategy>() {
                @Override
                public <T> boolean tryAcquire(ReadWriteUpdateLockingStrategy strategy,
                                              Access<T> access, T obj, long offset) {
                    return strategy.tryUpgradeReadToUpdateLock(access, obj, offset);
                }
            };

    // TryAcquireOperation for ReadWriteUpdateLockingStrategy - Upgrade Update to Write Lock
    private static final TryAcquireOperation<ReadWriteUpdateLockingStrategy>
            UPGRADE_UPDATE_TO_WRITE_LOCK =
            new TryAcquireOperation<ReadWriteUpdateLockingStrategy>() {
                @Override
                public <T> boolean tryAcquire(ReadWriteUpdateLockingStrategy strategy,
                                              Access<T> access, T obj, long offset) {
                    return strategy.tryUpgradeUpdateToWriteLock(access, obj, offset);
                }
            };

    // TryAcquireOperation for ReadWriteUpdateWithWaitsLockingStrategy - Upgrade Update to Write Lock and Deregister Wait
    private static final TryAcquireOperation<ReadWriteUpdateWithWaitsLockingStrategy>
            UPGRADE_UPDATE_TO_WRITE_LOCK_AND_DEREGISTER_WAIT =
            new TryAcquireOperation<ReadWriteUpdateWithWaitsLockingStrategy>() {
                @Override
                public <T> boolean tryAcquire(ReadWriteUpdateWithWaitsLockingStrategy strategy,
                                              Access<T> access, T obj, long offset) {
                    return strategy.tryUpgradeUpdateToWriteLockAndDeregisterWait(
                            access, obj, offset);
                }
            };

    // Private constructor to prevent instantiation
    private TryAcquireOperations() {
    }

    /**
     * Returns a {@link TryAcquireOperation} for acquiring a general lock.
     *
     * @return the {@link TryAcquireOperation} for a general lock
     */
    public static TryAcquireOperation<LockingStrategy> lock() {
        return LOCK;
    }

    /**
     * Returns a {@link TryAcquireOperation} for acquiring a read lock.
     *
     * @return the {@link TryAcquireOperation} for a read lock
     */
    public static TryAcquireOperation<ReadWriteLockingStrategy> readLock() {
        return READ_LOCK;
    }

    /**
     * Returns a {@link TryAcquireOperation} for upgrading a read lock to a write lock.
     *
     * @return the {@link TryAcquireOperation} for upgrading a read lock to a write lock
     */
    public static TryAcquireOperation<ReadWriteLockingStrategy> upgradeReadToWriteLock() {
        return UPGRADE_READ_TO_WRITE_LOCK;
    }

    /**
     * Returns a {@link TryAcquireOperation} for acquiring a write lock.
     *
     * @return the {@link TryAcquireOperation} for a write lock
     */
    public static TryAcquireOperation<ReadWriteLockingStrategy> writeLock() {
        return WRITE_LOCK;
    }

    /**
     * Returns a {@link TryAcquireOperation} for upgrading a read lock to a write lock and deregistering a wait.
     *
     * @return the {@link TryAcquireOperation} for upgrading a read lock to a write lock and deregistering a wait
     */
    public static TryAcquireOperation<ReadWriteWithWaitsLockingStrategy>
    upgradeReadToWriteLockAndDeregisterWait() {
        return UPGRADE_READ_TO_WRITE_LOCK_AND_DEREGISTER_WAIT;
    }

    /**
     * Returns a {@link TryAcquireOperation} for acquiring a write lock and deregistering a wait.
     *
     * @return the {@link TryAcquireOperation} for a write lock and deregistering a wait
     */
    public static TryAcquireOperation<ReadWriteWithWaitsLockingStrategy>
    writeLockAndDeregisterWait() {
        return WRITE_LOCK_AND_DEREGISTER_WAIT;
    }

    /**
     * Returns a {@link TryAcquireOperation} for acquiring an update lock.
     *
     * @return the {@link TryAcquireOperation} for an update lock
     */
    public static TryAcquireOperation<ReadWriteUpdateLockingStrategy> updateLock() {
        return UPDATE_LOCK;
    }

    /**
     * Returns a {@link TryAcquireOperation} for upgrading a read lock to an update lock.
     *
     * @return the {@link TryAcquireOperation} for upgrading a read lock to an update lock
     */
    public static TryAcquireOperation<ReadWriteUpdateLockingStrategy> upgradeReadToUpdateLock() {
        return UPGRADE_READ_TO_UPDATE_LOCK;
    }

    /**
     * Returns a {@link TryAcquireOperation} for upgrading an update lock to a write lock.
     *
     * @return the {@link TryAcquireOperation} for upgrading an update lock to a write lock
     */
    public static TryAcquireOperation<ReadWriteUpdateLockingStrategy> upgradeUpdateToWriteLock() {
        return UPGRADE_UPDATE_TO_WRITE_LOCK;
    }

    /**
     * Returns a {@link TryAcquireOperation} for upgrading an update lock to a write lock and deregistering a wait.
     *
     * @return the {@link TryAcquireOperation} for upgrading an update lock to a write lock and deregistering a wait
     */
    public static TryAcquireOperation<ReadWriteUpdateWithWaitsLockingStrategy>
    upgradeUpdateToWriteLockAndDeregisterWait() {

        return UPGRADE_UPDATE_TO_WRITE_LOCK_AND_DEREGISTER_WAIT;
    }
}
