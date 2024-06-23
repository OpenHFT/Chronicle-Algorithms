/*
 * Copyright 2014-2020 chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
     *         {@code false} otherwise
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
