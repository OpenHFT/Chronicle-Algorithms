/*
 * Copyright 2014-2020 chronicle.software
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
import net.openhft.chronicle.algo.bytes.Accessor;
import net.openhft.chronicle.bytes.BytesStore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.concurrent.*;

import static java.util.Arrays.asList;
import static net.openhft.chronicle.algo.bytes.Accessor.uncheckedByteBufferAccessor;
import static net.openhft.chronicle.algo.locks.LockingStrategyTest.AccessMethod.ADDRESS;
import static net.openhft.chronicle.algo.locks.LockingStrategyTest.AccessMethod.BYTES_WITH_OFFSET;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

@RunWith(value = Parameterized.class)
public class LockingStrategyTest {

    private ExecutorService e1, e2;
    @SuppressWarnings("FieldCanBeLocal")
    private ByteBuffer buffer;
    @SuppressWarnings("FieldCanBeLocal")
    private BytesStore bytesStore;
    private long offset;
    private final LockingStrategy lockingStrategy;
    private final AccessMethod accessMethod;
    private Access access;
    private Object handle;
    private final TestReadWriteLockState rwLockState = new TestReadWriteLockState();
    private final Callable<Boolean> tryReadLockTask = () -> rwls().tryReadLock();
    private final TestReadWriteUpdateLockState rwuLockState = new TestReadWriteUpdateLockState();
    private final Runnable readUnlockTask = () -> rwls().readUnlock();
    private final Callable<Boolean> tryUpdateLockTask = () -> rwuls().tryUpdateLock();
    private final Runnable updateUnlockTask = () -> rwuls().updateUnlock();
    private final Callable<Boolean> tryWriteLockTask = () -> rwls().tryWriteLock();
    private final Runnable writeUnlockTask = () -> rwls().writeUnlock();

    public LockingStrategyTest(LockingStrategy lockingStrategy, AccessMethod accessMethod) {
        this.lockingStrategy = lockingStrategy;
        this.accessMethod = accessMethod;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return asList(new Object[][]{
                {VanillaReadWriteUpdateWithWaitsLockingStrategy.instance(), ADDRESS},
                {VanillaReadWriteUpdateWithWaitsLockingStrategy.instance(), BYTES_WITH_OFFSET},
                {VanillaReadWriteWithWaitsLockingStrategy.instance(), ADDRESS},
                {VanillaReadWriteWithWaitsLockingStrategy.instance(), BYTES_WITH_OFFSET},
        });
    }

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        e1 = new ThreadPoolExecutor(0, 1, Integer.MAX_VALUE, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>());
        e2 = new ThreadPoolExecutor(0, 1, Integer.MAX_VALUE, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>());

        buffer = ByteBuffer.allocateDirect(8);
        if (accessMethod == ADDRESS) {
            Accessor.Full<ByteBuffer, ?> accessor = uncheckedByteBufferAccessor(buffer);
            access = accessor.access();
            handle = accessor.handle(buffer);
            offset = accessor.offset(buffer, 0);
        } else {
            bytesStore = BytesStore.wrap(buffer);
            Accessor.Full<BytesStore, ?> accessor = Accessor.checkedBytesStoreAccessor();
            access = accessor.access();
            handle = accessor.handle(bytesStore);
            offset = accessor.offset(bytesStore, 0);
        }
        rwls().reset();
    }

    @After
    public void tearDown() {
        e1.shutdown();
        e2.shutdown();
    }

    @Test
    public void testUpdateLockIsExclusive() throws ExecutionException, InterruptedException {
        assumeReadWriteUpdateLock();

        // Acquire the update lock in thread 1...
        assertTrue(e1.submit(tryUpdateLockTask).get());

        // Try to acquire update lock in thread 2, should fail...
        assertFalse(e2.submit(tryUpdateLockTask).get());

        // Release the update lock in thread 1...
        e1.submit(updateUnlockTask).get();

        // Try to acquire update lock in thread 2 again, should succeed...
        assertTrue(e2.submit(tryUpdateLockTask).get());

        // Release the update lock in thread 2...
        e2.submit(updateUnlockTask).get();
    }

    @Test
    public void testUpdateLockAllowsOtherReaders() throws ExecutionException, InterruptedException {
        assumeReadWriteUpdateLock();

        // Acquire the update lock in thread 1...
        assertTrue(e1.submit(tryUpdateLockTask).get());

        // Try to acquire read lock in thread 2, should succeed...
        assertTrue(e2.submit(tryReadLockTask).get());

        // Release the update lock in thread 1...
        e1.submit(updateUnlockTask).get();

        // Release the read lock in thread 2...
        e2.submit(readUnlockTask).get();
    }

    @Test
    public void testUpdateLockBlocksOtherWriters() throws ExecutionException, InterruptedException {
        assumeReadWriteUpdateLock();

        // Acquire the update lock in thread 1...
        assertTrue(e1.submit(tryUpdateLockTask).get());

        // Try to acquire write lock in thread 2, should fail...
        assertFalse(e2.submit(tryWriteLockTask).get());

        // Release the update lock in thread 1...
        e1.submit(updateUnlockTask).get();

        // Try to acquire write lock in thread 2 again, should succeed...
        assertTrue(e2.submit(tryWriteLockTask).get());

        // Release the write lock in thread 2...
        e2.submit(writeUnlockTask).get();
    }

    @Test
    public void testWriteLockBlocksOtherReaders() throws ExecutionException, InterruptedException {
        assumeReadWriteLock();

        // Acquire the write lock in thread 1...
        assertTrue(e1.submit(tryWriteLockTask).get());

        // Try to acquire read lock in thread 2, should fail...
        assertFalse(e2.submit(tryReadLockTask).get());

        // Release the write lock in thread 1...
        e1.submit(writeUnlockTask).get();

        // Try to acquire read lock in thread 2 again, should succeed...
        assertTrue(e2.submit(tryReadLockTask).get());

        // Release the read lock in thread 2...
        e2.submit(readUnlockTask).get();
    }

    @Test
    public void testUpdateLockUpgradeToWriteLock() throws ExecutionException, InterruptedException {
        assumeReadWriteUpdateLock();

        // Acquire the update lock in thread 1...
        assertTrue(e1.submit(tryUpdateLockTask).get());

        // Try to acquire write lock in thread 1, should succeed...
        assertTrue(e1.submit(() -> rwuls().tryUpgradeUpdateToWriteLock()).get());

        // Release the write lock in thread 1...
        e1.submit(() -> rwuls().downgradeWriteToUpdateLock());

        // Release the update lock in thread 1...
        e1.submit(updateUnlockTask).get();
    }

    @Test
    public void testReadWriteLockTransitions() {
        assumeReadWriteLock();

        // forbid upgrades/downgrades/unlocks when lock is not held
        readUnlockForbidden();
        writeUnlockForbidden();
        upgradeReadToWriteLockForbidden();
        downgradeWriteToReadLockForbidden();

        // Read lock is held
        assertTrue(rwls().tryReadLock());
        writeUnlockForbidden();
        downgradeWriteToReadLockForbidden();

        // allow unlock
        rwls().readUnlock();
        assertTrue(rwls().tryReadLock());

        // allow upgrade to write lock
        try {
            assertTrue(rwls().tryUpgradeReadToWriteLock());
        } catch (UnsupportedOperationException tolerated) {
            rwls().readUnlock();
            assertTrue(rwls().tryWriteLock());
        }
// write lock is held
        readUnlockForbidden();
        upgradeReadToWriteLockForbidden();

        // allow unlock
        rwls().writeUnlock();
        assertTrue(rwls().tryWriteLock());

        // allow downgrade to read lock
        try {
            rwls().downgradeWriteToReadLock();
        } catch (UnsupportedOperationException tolerated) {
            // ignore
        }
        rwls().reset();
    }

    @Test
    public void testReadWriteUpgradeLockTransitions() {
        assumeReadWriteUpdateLock();

        // forbid upgrades/downgrades/unlocks when lock is not held
        updateUnlockForbidden();
        upgradeReadToUpdateLockForbidden();
        upgradeUpdateToWriteLockForbidden();
        downgradeUpdateToReadLockForbidden();
        downgradeWriteToUpdateLockForbidden();

        // Read lock is held
        assertTrue(rwuls().tryReadLock());
        updateUnlockForbidden();
        upgradeUpdateToWriteLockForbidden();
        downgradeUpdateToReadLockForbidden();
        downgradeWriteToUpdateLockForbidden();

        // allow upgrade to update lock
        assertTrue(rwuls().tryUpgradeReadToUpdateLock());

        // update lock is held
        readUnlockForbidden();
        writeUnlockForbidden();
        upgradeReadToUpdateLockForbidden();
        upgradeReadToWriteLockForbidden();
        downgradeWriteToUpdateLockForbidden();
        downgradeWriteToReadLockForbidden();

        // allow unlock
        rwuls().updateUnlock();
        assertTrue(rwuls().tryUpdateLock());

        // allow upgrade to write lock
        assertTrue(rwuls().tryUpgradeUpdateToWriteLock());

        // write lock is held
        updateUnlockForbidden();
        upgradeReadToUpdateLockForbidden();
        upgradeUpdateToWriteLockForbidden();
        downgradeUpdateToReadLockForbidden();

        // allow downgrade to update lock
        rwuls().downgradeWriteToUpdateLock();

        rwuls().updateUnlock();
    }

    private void downgradeWriteToReadLockForbidden() {
        try {
            rwls().downgradeWriteToReadLock();
            fail("downgradeWriteToReadLock() should fail");
        } catch (IllegalMonitorStateException | UnsupportedOperationException e) {
            // expected
        }
    }

    private void upgradeReadToWriteLockForbidden() {
        try {
            rwls().tryUpgradeReadToWriteLock();
            fail("tryUpgradeReadToWriteLock() should fail");
        } catch (IllegalMonitorStateException | UnsupportedOperationException e) {
            // expected
        }
    }

    private void writeUnlockForbidden() {
        try {
            rwls().writeUnlock();
            fail("writeUnlock() should fail");
        } catch (IllegalMonitorStateException | UnsupportedOperationException e) {
            // expected
        }
    }

    private void readUnlockForbidden() {
        try {
            rwls().readUnlock();
            fail("readUnlock() should fail");
        } catch (IllegalMonitorStateException | UnsupportedOperationException e) {
            // expected
        }
    }

    private void downgradeWriteToUpdateLockForbidden() {
        try {
            rwuls().downgradeWriteToUpdateLock();
            fail("downgradeWriteToUpdateLock() should fail");
        } catch (IllegalMonitorStateException | UnsupportedOperationException e) {
            // expected
        }
    }

    private void downgradeUpdateToReadLockForbidden() {
        try {
            rwuls().downgradeUpdateToReadLock();
            fail("downgradeUpdateToReadLock() should fail");
        } catch (IllegalMonitorStateException | UnsupportedOperationException e) {
            // expected
        }
    }

    private void upgradeUpdateToWriteLockForbidden() {
        try {
            rwuls().tryUpgradeUpdateToWriteLock();
            fail("tryUpgradeUpdateToWriteLock() should fail");
        } catch (IllegalMonitorStateException | UnsupportedOperationException e) {
            // expected
        }
    }

    private void upgradeReadToUpdateLockForbidden() {
        try {
            rwuls().tryUpgradeReadToUpdateLock();
            fail("tryUpgradeReadToUpdateLock() should fail");
        } catch (IllegalMonitorStateException | UnsupportedOperationException e) {
            // expected
        }
    }

    private void updateUnlockForbidden() {
        try {
            rwuls().updateUnlock();
            fail("updateUnlock() should fail");
        } catch (IllegalMonitorStateException | UnsupportedOperationException e) {
            // expected
        }
    }

    private void assumeReadWriteUpdateLock() {
        assumeTrue(lockingStrategy instanceof ReadWriteUpdateLockingStrategy);
    }

    private void assumeReadWriteLock() {
        assumeTrue(lockingStrategy instanceof ReadWriteLockingStrategy);
    }

    private ReadWriteLockState rwls() {
        return rwLockState;
    }

    private ReadWriteUpdateLockState rwuls() {
        return rwuLockState;
    }

    enum AccessMethod {ADDRESS, BYTES_WITH_OFFSET}

    @SuppressWarnings("unchecked")
    private class TestReadWriteLockState extends AbstractReadWriteLockState {

        private ReadWriteLockingStrategy rwls() {
            return (ReadWriteLockingStrategy) lockingStrategy;
        }

        @Override
        public boolean tryReadLock() {
            return rwls().tryReadLock(access, handle, offset);
        }

        @Override
        public boolean tryWriteLock() {
            return rwls().tryWriteLock(access, handle, offset);
        }

        @Override
        public boolean tryUpgradeReadToWriteLock() {
            return rwls().tryUpgradeReadToWriteLock(access, handle, offset);
        }

        @Override
        public void readUnlock() {
            rwls().readUnlock(access, handle, offset);
        }

        @Override
        public void writeUnlock() {
            rwls().writeUnlock(access, handle, offset);
        }

        @Override
        public void downgradeWriteToReadLock() {
            rwls().downgradeWriteToReadLock(access, handle, offset);
        }

        @Override
        public void reset() {
            rwls().reset(access, handle, offset);
        }

        @Override
        public long getState() {
            return rwls().getState(access, handle, offset);
        }

        @Override
        public ReadWriteLockingStrategy lockingStrategy() {
            return rwls();
        }
    }

    @SuppressWarnings("unchecked")
    private class TestReadWriteUpdateLockState extends TestReadWriteLockState
            implements ReadWriteUpdateLockState {

        ReadWriteUpdateLockingStrategy rwuls() {
            return (ReadWriteUpdateLockingStrategy) lockingStrategy;
        }

        @Override
        public boolean tryUpdateLock() {
            return rwuls().tryUpdateLock(access, handle, offset);
        }

        @Override
        public boolean tryUpgradeReadToUpdateLock() {
            return rwuls().tryUpgradeReadToUpdateLock(access, handle, offset);
        }

        @Override
        public boolean tryUpgradeUpdateToWriteLock() {
            return rwuls().tryUpgradeUpdateToWriteLock(access, handle, offset);
        }

        @Override
        public void updateUnlock() {
            rwuls().updateUnlock(access, handle, offset);
        }

        @Override
        public void downgradeUpdateToReadLock() {
            rwuls().downgradeUpdateToReadLock(access, handle, offset);
        }

        @Override
        public void downgradeWriteToUpdateLock() {
            rwuls().downgradeWriteToUpdateLock(access, handle, offset);
        }

        @Override
        public ReadWriteUpdateLockingStrategy lockingStrategy() {
            return rwuls();
        }
    }
}