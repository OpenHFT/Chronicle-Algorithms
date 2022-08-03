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

import net.openhft.chronicle.algo.bytes.Access;
import net.openhft.chronicle.algo.bytes.ReadAccess;

public final class VanillaReadWriteWithWaitsLockingStrategy extends AbstractReadWriteLockingStrategy
        implements ReadWriteWithWaitsLockingStrategy {

    static final int RW_LOCK_LIMIT = 30;
    static final long RW_READ_LOCKED = 1L;
    static final long RW_WRITE_WAITING = 1L << RW_LOCK_LIMIT;
    static final long RW_WRITE_LOCKED = 1L << 2 * RW_LOCK_LIMIT;
    static final int RW_LOCK_MASK = (1 << RW_LOCK_LIMIT) - 1;
    private static final ReadWriteWithWaitsLockingStrategy INSTANCE =
            new VanillaReadWriteWithWaitsLockingStrategy();

    private VanillaReadWriteWithWaitsLockingStrategy() {
    }

    public static ReadWriteWithWaitsLockingStrategy instance() {
        return INSTANCE;
    }

    static int rwReadLocked(long lock) {
        return (int) (lock & RW_LOCK_MASK);
    }

    static int rwWriteWaiting(long lock) {
        return (int) ((lock >>> RW_LOCK_LIMIT) & RW_LOCK_MASK);
    }

    static int rwWriteLocked(long lock) {
        return (int) (lock >>> (2 * RW_LOCK_LIMIT));
    }

    static <T> long read(ReadAccess<T> access, T t, long offset) {
        return access.readVolatileLong(t, offset);
    }

    static <T> boolean cas(Access<T> access, T t, long offset, long expected, long x) {
        return access.compareAndSwapLong(t, offset, expected, x);
    }

    @Override
    public <T> boolean tryReadLock(Access<T> access, T t, long offset) {
        long lock = read(access, t, offset);
        int writersWaiting = rwWriteWaiting(lock);
        int writersLocked = rwWriteLocked(lock);
        // readers wait for waiting writers
        if (writersLocked <= 0 && writersWaiting <= 0) {
            // increment readers locked.
            int readersLocked = rwReadLocked(lock);
            if (readersLocked >= RW_LOCK_MASK)
                throw new IllegalMonitorStateException("readersLocked has reached a limit of " +
                        readersLocked);
            return cas(access, t, offset, lock, lock + RW_READ_LOCKED);
        }
        return false;
    }

    @Override
    public <T> boolean tryWriteLock(Access<T> access, T t, long offset) {
        long lock = read(access, t, offset);
        int readersLocked = rwReadLocked(lock);
        int writersLocked = rwWriteLocked(lock);
        // writers don't wait for waiting readers.
        if (readersLocked <= 0 && writersLocked <= 0) {
            return cas(access, t, offset, lock, lock + RW_WRITE_LOCKED);
        }
        return false;
    }

    @Override
    public <T> boolean tryUpgradeReadToWriteLock(Access<T> access, T t, long offset) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public <T> void readUnlock(Access<T> access, T t, long offset) {
        for (; ; ) {
            long lock = read(access, t, offset);
            int readersLocked = rwReadLocked(lock);
            if (readersLocked <= 0)
                throw new IllegalMonitorStateException("readerLock underflow");
            if (cas(access, t, offset, lock, lock - RW_READ_LOCKED))
                return;
        }
    }

    @Override
    public <T> void writeUnlock(Access<T> access, T t, long offset) {
        for (; ; ) {
            long lock = read(access, t, offset);
            int writersLocked = rwWriteLocked(lock);
            if (writersLocked != 1)
                throw new IllegalMonitorStateException("writersLock underflow " + writersLocked);
            if (cas(access, t, offset, lock, lock - RW_WRITE_LOCKED))
                return;
        }
    }

    @Override
    public <T> void downgradeWriteToReadLock(Access<T> access, T t, long offset) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public boolean isWriteLocked(long state) {
        return rwWriteLocked(state) > 0;
    }

    @Override
    public int readLockCount(long state) {
        return rwReadLocked(state);
    }

    @Override
    public <T> void reset(Access<T> access, T t, long offset) {
        access.writeOrderedLong(t, offset, 0L);
    }

    @Override
    public <T> void resetKeepingWaits(Access<T> access, T t, long offset) {
        while (true) {
            long lock = read(access, t, offset);
            long onlyWaits = lock & ((long) RW_LOCK_MASK) << RW_LOCK_LIMIT;
            if (cas(access, t, offset, lock, onlyWaits))
                return;
        }
    }

    @Override
    public <T> void registerWait(Access<T> access, T t, long offset) {
        for (; ; ) {
            long lock = read(access, t, offset);
            int writersWaiting = rwWriteWaiting(lock);
            if (writersWaiting >= RW_LOCK_MASK)
                throw new IllegalMonitorStateException("writersWaiting has reached a limit of " +
                        writersWaiting);
            if (cas(access, t, offset, lock, lock + RW_WRITE_WAITING))
                break;
        }
    }

    @Override
    public <T> void deregisterWait(Access<T> access, T t, long offset) {
        for (; ; ) {
            long lock = read(access, t, offset);
            int writersWaiting = rwWriteWaiting(lock);
            if (writersWaiting <= 0)
                throw new IllegalMonitorStateException("writersWaiting has underflowed");
            if (cas(access, t, offset, lock, lock - RW_WRITE_WAITING))
                break;
        }
    }

    @Override
    public <T> boolean tryWriteLockAndDeregisterWait(
            Access<T> access, T t, long offset) {
        long lock = read(access, t, offset);
        int readersLocked = rwReadLocked(lock);
        int writersWaiting = rwWriteWaiting(lock);
        int writersLocked = rwWriteLocked(lock);
        if (readersLocked <= 0 && writersLocked <= 0) {
            // increment readers locked.
            if (writersWaiting <= 0)
                throw new IllegalMonitorStateException("writersWaiting has underflowed");
            // add to the readLock count and decrease the readWaiting count.
            return cas(access, t, offset, lock, lock + RW_WRITE_LOCKED - RW_WRITE_WAITING);
        }
        return false;
    }

    @Override
    public <T> boolean tryUpgradeReadToWriteLockAndDeregisterWait(
            Access<T> access, T t, long offset) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public long resetState() {
        return 0L;
    }

    @Override
    public <T> long getState(ReadAccess<T> access, T t, long offset) {
        return read(access, t, offset);
    }

    @Override
    public int waitCount(long state) {
        return rwWriteWaiting(state);
    }

    @Override
    public boolean isLocked(long state) {
        return isReadLocked(state) || isWriteLocked(state);
    }

    @Override
    public int lockCount(long state) {
        return rwReadLocked(state) + rwWriteLocked(state);
    }

    @Override
    public String toString(long state) {
        return "[read locks = " + readLockCount(state) +
                ", write locked = " + isWriteLocked(state) +
                ", waits = " + waitCount(state) + "]";
    }

    @Override
    public int sizeInBytes() {
        return 8;
    }
}
