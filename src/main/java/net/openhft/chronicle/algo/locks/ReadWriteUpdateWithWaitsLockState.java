//
// Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
//

package net.openhft.chronicle.algo.locks;

/**
 * Interface representing a read-write-update lock state with wait registration capabilities.
 * <p>
 * This interface extends both {@link ReadWriteUpdateLockState} and {@link ReadWriteWithWaitsLockState},
 * adding the capability to upgrade an update lock to a write lock while deregistering the wait state.
 */
public interface ReadWriteUpdateWithWaitsLockState
        extends ReadWriteUpdateLockState, ReadWriteWithWaitsLockState {

    /**
     * Attempts to upgrade an update lock to a write lock and deregister the wait state.
     *
     * @return {@code true} if the lock was successfully upgraded and the wait state deregistered,
     * {@code false} otherwise
     */
    boolean tryUpgradeUpdateToWriteLockAndDeregisterWait();

    /**
     * Returns the locking strategy associated with this lock state.
     *
     * @return the {@link ReadWriteUpdateWithWaitsLockingStrategy} instance
     */
    @Override
    ReadWriteUpdateWithWaitsLockingStrategy lockingStrategy();
}
