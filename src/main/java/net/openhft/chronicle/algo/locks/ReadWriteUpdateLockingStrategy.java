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

import net.openhft.chronicle.bytes.Access;

/**
 * Logic of read-write-update lock state transitions.
 *
 * Read lock - could be several at the same time.
 * Update lock - doesn't block reads, but couldn't be several update locks at the same time
 * Write lock - exclusive
 */
public interface ReadWriteUpdateLockingStrategy extends ReadWriteLockingStrategy {

    <T> boolean tryUpdateLock(Access<T> access, T t, long offset);

    <T> boolean tryUpgradeReadToUpdateLock(Access<T> access, T t, long offset);

    <T> boolean tryUpgradeUpdateToWriteLock(Access<T> access, T t, long offset);

    <T> void updateUnlock(Access<T> access, T t, long offset);

    <T> void downgradeUpdateToReadLock(Access<T> access, T t, long offset);

    <T> void downgradeWriteToUpdateLock(Access<T> access, T t, long offset);

    boolean isUpdateLocked(long state);
}
