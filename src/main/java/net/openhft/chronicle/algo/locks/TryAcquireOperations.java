/*
 * Copyright 2014 Higher Frequency Trading http://www.higherfrequencytrading.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.algo.locks;

import net.openhft.chronicle.algo.bytes.Access;

public final class TryAcquireOperations {

    private static final TryAcquireOperation<LockingStrategy> LOCK =
            new TryAcquireOperation<LockingStrategy>() {
                @Override
                public <T> boolean tryAcquire(LockingStrategy strategy,
                                              Access<T> access, T obj, long offset) {
                    return strategy.tryLock(access, obj, offset);
                }
            };
    private static final TryAcquireOperation<ReadWriteLockingStrategy> READ_LOCK =
            new TryAcquireOperation<ReadWriteLockingStrategy>() {
                @Override
                public <T> boolean tryAcquire(ReadWriteLockingStrategy strategy,
                                              Access<T> access, T obj, long offset) {
                    return strategy.tryReadLock(access, obj, offset);
                }
            };
    private static final TryAcquireOperation<ReadWriteLockingStrategy> UPGRADE_READ_TO_WRITE_LOCK =
            new TryAcquireOperation<ReadWriteLockingStrategy>() {
                @Override
                public <T> boolean tryAcquire(ReadWriteLockingStrategy strategy,
                                              Access<T> access, T obj, long offset) {
                    return strategy.tryUpgradeReadToWriteLock(access, obj, offset);
                }
            };
    private static final TryAcquireOperation<ReadWriteLockingStrategy> WRITE_LOCK =
            new TryAcquireOperation<ReadWriteLockingStrategy>() {
                @Override
                public <T> boolean tryAcquire(ReadWriteLockingStrategy strategy,
                                              Access<T> access, T obj, long offset) {
                    return strategy.tryWriteLock(access, obj, offset);
                }
            };
    private static final TryAcquireOperation<ReadWriteWithWaitsLockingStrategy>
            UPGRADE_READ_TO_WRITE_LOCK_AND_DEREGISTER_WAIT =
            new TryAcquireOperation<ReadWriteWithWaitsLockingStrategy>() {
                @Override
                public <T> boolean tryAcquire(ReadWriteWithWaitsLockingStrategy strategy,
                                              Access<T> access, T obj, long offset) {
                    return strategy.tryUpgradeReadToWriteLockAndDeregisterWait(access, obj, offset);
                }
            };
    private static final TryAcquireOperation<ReadWriteWithWaitsLockingStrategy>
            WRITE_LOCK_AND_DEREGISTER_WAIT =
            new TryAcquireOperation<ReadWriteWithWaitsLockingStrategy>() {
                @Override
                public <T> boolean tryAcquire(ReadWriteWithWaitsLockingStrategy strategy,
                                              Access<T> access, T obj, long offset) {
                    return strategy.tryWriteLockAndDeregisterWait(access, obj, offset);
                }
            };
    private static final TryAcquireOperation<ReadWriteUpdateLockingStrategy> UPDATE_LOCK =
            new TryAcquireOperation<ReadWriteUpdateLockingStrategy>() {
                @Override
                public <T> boolean tryAcquire(ReadWriteUpdateLockingStrategy strategy,
                                              Access<T> access, T obj, long offset) {
                    return strategy.tryUpdateLock(access, obj, offset);
                }
            };
    private static final TryAcquireOperation<ReadWriteUpdateLockingStrategy>
            UPGRADE_READ_TO_UPDATE_LOCK =
            new TryAcquireOperation<ReadWriteUpdateLockingStrategy>() {
                @Override
                public <T> boolean tryAcquire(ReadWriteUpdateLockingStrategy strategy,
                                              Access<T> access, T obj, long offset) {
                    return strategy.tryUpgradeReadToUpdateLock(access, obj, offset);
                }
            };
    private static final TryAcquireOperation<ReadWriteUpdateLockingStrategy>
            UPGRADE_UPDATE_TO_WRITE_LOCK =
            new TryAcquireOperation<ReadWriteUpdateLockingStrategy>() {
                @Override
                public <T> boolean tryAcquire(ReadWriteUpdateLockingStrategy strategy,
                                              Access<T> access, T obj, long offset) {
                    return strategy.tryUpgradeUpdateToWriteLock(access, obj, offset);
                }
            };
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

    private TryAcquireOperations() {
    }

    public static TryAcquireOperation<LockingStrategy> lock() {
        return LOCK;
    }

    public static TryAcquireOperation<ReadWriteLockingStrategy> readLock() {
        return READ_LOCK;
    }

    public static TryAcquireOperation<ReadWriteLockingStrategy> upgradeReadToWriteLock() {
        return UPGRADE_READ_TO_WRITE_LOCK;
    }

    public static TryAcquireOperation<ReadWriteLockingStrategy> writeLock() {
        return WRITE_LOCK;
    }

    public static TryAcquireOperation<ReadWriteWithWaitsLockingStrategy>
    upgradeReadToWriteLockAndDeregisterWait() {
        return UPGRADE_READ_TO_WRITE_LOCK_AND_DEREGISTER_WAIT;
    }

    public static TryAcquireOperation<ReadWriteWithWaitsLockingStrategy>
    writeLockAndDeregisterWait() {
        return WRITE_LOCK_AND_DEREGISTER_WAIT;
    }

    public static TryAcquireOperation<ReadWriteUpdateLockingStrategy> updateLock() {
        return UPDATE_LOCK;
    }

    public static TryAcquireOperation<ReadWriteUpdateLockingStrategy> upgradeReadToUpdateLock() {
        return UPGRADE_READ_TO_UPDATE_LOCK;
    }

    public static TryAcquireOperation<ReadWriteUpdateLockingStrategy> upgradeUpdateToWriteLock() {
        return UPGRADE_UPDATE_TO_WRITE_LOCK;
    }

    public static TryAcquireOperation<ReadWriteUpdateWithWaitsLockingStrategy>
    upgradeUpdateToWriteLockAndDeregisterWait() {

        return UPGRADE_UPDATE_TO_WRITE_LOCK_AND_DEREGISTER_WAIT;
    }
}
