/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo.locks;

import net.openhft.chronicle.algo.bytes.Access;
import net.openhft.chronicle.algo.bytes.Accessor;
import net.openhft.chronicle.bytes.BytesStore;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.ByteBuffer;
import java.util.concurrent.*;
import java.util.stream.Stream;

import static net.openhft.chronicle.algo.bytes.Accessor.uncheckedByteBufferAccessor;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@SuppressWarnings({"unchecked", "rawtypes"})
public class LockingStrategyTest {

    private LockingStrategy lockingStrategy;
    private AccessMethod accessMethod;
    private final ReadWriteLockStateAdapter rwLockState = new ReadWriteLockStateAdapter();
    private final Callable<Boolean> tryReadLockTask = () -> rwls().tryReadLock();
    private final ReadWriteUpdateLockStateAdapter rwuLockState = new ReadWriteUpdateLockStateAdapter();
    private final Runnable readUnlockTask = () -> rwls().readUnlock();
    private final Callable<Boolean> tryUpdateLockTask = () -> rwuls().tryUpdateLock();
    private final Runnable updateUnlockTask = () -> rwuls().updateUnlock();
    private final Callable<Boolean> tryWriteLockTask = () -> rwls().tryWriteLock();
    private final Runnable writeUnlockTask = () -> rwls().writeUnlock();
    private ExecutorService e1, e2;
    @SuppressWarnings("FieldCanBeLocal")
    private ByteBuffer buffer;
    @SuppressWarnings("FieldCanBeLocal")
    private BytesStore<?, ?> bytesStore;
    private long offset;
    private Access access;
    private Object handle;

    static Stream<Arguments> scenarios() {
        return Stream.of(
                Arguments.of("RWU/address", VanillaReadWriteUpdateWithWaitsLockingStrategy.instance(), AccessMethod.ADDRESS),
                Arguments.of("RWU/bytes+offset", VanillaReadWriteUpdateWithWaitsLockingStrategy.instance(), AccessMethod.BYTES_WITH_OFFSET),
                Arguments.of("RW/address", VanillaReadWriteWithWaitsLockingStrategy.instance(), AccessMethod.ADDRESS),
                Arguments.of("RW/bytes+offset", VanillaReadWriteWithWaitsLockingStrategy.instance(), AccessMethod.BYTES_WITH_OFFSET)
        );
    }

    @SuppressWarnings("unchecked")
    private void setUp(LockingStrategy lockingStrategy, AccessMethod accessMethod) {
        this.lockingStrategy = lockingStrategy;
        this.accessMethod = accessMethod;
        e1 = new ThreadPoolExecutor(0, 1, Integer.MAX_VALUE, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>());
        e2 = new ThreadPoolExecutor(0, 1, Integer.MAX_VALUE, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>());

        buffer = ByteBuffer.allocateDirect(8);
        if (accessMethod == AccessMethod.ADDRESS) {
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

    private void tearDown() {
        e1.shutdown();
        e2.shutdown();
    }

    @ParameterizedTest(name = "{0} update lock exclusive")
    @MethodSource("scenarios")
    public void testUpdateLockIsExclusive(String scenario, LockingStrategy lockingStrategy, AccessMethod accessMethod)
            throws ExecutionException, InterruptedException {
        setUp(lockingStrategy, accessMethod);
        try {
        assumeReadWriteUpdateLock();

        // Acquire the update lock in thread 1...
            assertTrue(e1.submit(tryUpdateLockTask).get(), scenario + ": update lock acquired by thread1");

        // Try to acquire update lock in thread 2, should fail...
            assertFalse(e2.submit(tryUpdateLockTask).get(), scenario + ": update lock blocked in thread2");

        // Release the update lock in thread 1...
        e1.submit(updateUnlockTask).get();

        // Try to acquire update lock in thread 2 again, should succeed...
            assertTrue(e2.submit(tryUpdateLockTask).get(), scenario + ": update lock acquired by thread2 after release");

        // Release the update lock in thread 2...
        e2.submit(updateUnlockTask).get();
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest(name = "{0} update lock allows readers")
    @MethodSource("scenarios")
    public void testUpdateLockAllowsOtherReaders(String scenario, LockingStrategy lockingStrategy, AccessMethod accessMethod)
            throws ExecutionException, InterruptedException {
        setUp(lockingStrategy, accessMethod);
        try {
        assumeReadWriteUpdateLock();

        // Acquire the update lock in thread 1...
            assertTrue(e1.submit(tryUpdateLockTask).get(), scenario + ": update lock acquired by thread1");

        // Try to acquire read lock in thread 2, should succeed...
            assertTrue(e2.submit(tryReadLockTask).get(), scenario + ": read lock acquired by thread2 while update held");

        // Release the update lock in thread 1...
        e1.submit(updateUnlockTask).get();

        // Release the read lock in thread 2...
        e2.submit(readUnlockTask).get();
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest(name = "{0} update lock blocks writers")
    @MethodSource("scenarios")
    public void testUpdateLockBlocksOtherWriters(String scenario, LockingStrategy lockingStrategy, AccessMethod accessMethod)
            throws ExecutionException, InterruptedException {
        setUp(lockingStrategy, accessMethod);
        try {
        assumeReadWriteUpdateLock();

        // Acquire the update lock in thread 1...
            assertTrue(e1.submit(tryUpdateLockTask).get(), scenario + ": update lock acquired by thread1");

        // Try to acquire write lock in thread 2, should fail...
            assertFalse(e2.submit(tryWriteLockTask).get(), scenario + ": write lock blocked by update lock");

        // Release the update lock in thread 1...
        e1.submit(updateUnlockTask).get();

        // Try to acquire write lock in thread 2 again, should succeed...
            assertTrue(e2.submit(tryWriteLockTask).get(), scenario + ": write lock acquired after update release");

        // Release the write lock in thread 2...
        e2.submit(writeUnlockTask).get();
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest(name = "{0} write lock blocks readers")
    @MethodSource("scenarios")
    public void testWriteLockBlocksOtherReaders(String scenario, LockingStrategy lockingStrategy, AccessMethod accessMethod)
            throws ExecutionException, InterruptedException {
        setUp(lockingStrategy, accessMethod);
        try {
        assumeReadWriteLock();

        // Acquire the write lock in thread 1...
            assertTrue(e1.submit(tryWriteLockTask).get(), scenario + ": write lock acquired by thread1");

        // Try to acquire read lock in thread 2, should fail...
            assertFalse(e2.submit(tryReadLockTask).get(), scenario + ": read lock blocked by write lock");

        // Release the write lock in thread 1...
        e1.submit(writeUnlockTask).get();

        // Try to acquire read lock in thread 2 again, should succeed...
            assertTrue(e2.submit(tryReadLockTask).get(), scenario + ": read lock acquired after write release");

        // Release the read lock in thread 2...
        e2.submit(readUnlockTask).get();
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest(name = "{0} update-to-write upgrade")
    @MethodSource("scenarios")
    public void testUpdateLockUpgradeToWriteLock(String scenario, LockingStrategy lockingStrategy, AccessMethod accessMethod)
            throws ExecutionException, InterruptedException {
        setUp(lockingStrategy, accessMethod);
        try {
        assumeReadWriteUpdateLock();

        // Acquire the update lock in thread 1...
            assertTrue(e1.submit(tryUpdateLockTask).get(), scenario + ": update lock acquired by thread1");

        // Try to acquire write lock in thread 1, should succeed...
            assertTrue(e1.submit(() -> rwuls().tryUpgradeUpdateToWriteLock()).get(),
                    scenario + ": upgrade update->write succeeded");

        // Release the write lock in thread 1...
        e1.submit(() -> rwuls().downgradeWriteToUpdateLock()).get();

        // Release the update lock in thread 1...
        e1.submit(updateUnlockTask).get();
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest(name = "{0} read/write transitions")
    @MethodSource("scenarios")
    public void testReadWriteLockTransitions(String scenario, LockingStrategy lockingStrategy, AccessMethod accessMethod) {
        setUp(lockingStrategy, accessMethod);
        try {
        assumeReadWriteLock();

        // forbid upgrades/downgrades/unlocks when lock is not held
        readUnlockForbidden();
        writeUnlockForbidden();
        upgradeReadToWriteLockForbidden();
        downgradeWriteToReadLockForbidden();

        // Read lock is held
            assertTrue(rwls().tryReadLock(), scenario + ": tryReadLock succeeds");
        writeUnlockForbidden();
        downgradeWriteToReadLockForbidden();

        // allow unlock
        rwls().readUnlock();
            assertTrue(rwls().tryReadLock(), scenario + ": tryReadLock succeeds after readUnlock");

        // allow upgrade to write lock
        try {
            assertTrue(rwls().tryUpgradeReadToWriteLock(), scenario + ": tryUpgradeReadToWriteLock succeeds");
        } catch (UnsupportedOperationException tolerated) {
            rwls().readUnlock();
            assertTrue(rwls().tryWriteLock(), scenario + ": tryWriteLock succeeds (no upgrade support)");
        }
        // write lock is held
        readUnlockForbidden();
        upgradeReadToWriteLockForbidden();

        // allow unlock
        rwls().writeUnlock();
            assertTrue(rwls().tryWriteLock(), scenario + ": tryWriteLock succeeds after writeUnlock");

        // allow downgrade to read lock
        try {
            rwls().downgradeWriteToReadLock();
        } catch (UnsupportedOperationException tolerated) {
            // ignore
        }
        rwls().reset();
        } finally {
            tearDown();
        }
    }

    @ParameterizedTest(name = "{0} read/write/update transitions")
    @MethodSource("scenarios")
    public void testReadWriteUpgradeLockTransitions(String scenario, LockingStrategy lockingStrategy, AccessMethod accessMethod) {
        setUp(lockingStrategy, accessMethod);
        try {
        assumeReadWriteUpdateLock();

        // forbid upgrades/downgrades/unlocks when lock is not held
        updateUnlockForbidden();
        upgradeReadToUpdateLockForbidden();
        upgradeUpdateToWriteLockForbidden();
        downgradeUpdateToReadLockForbidden();
        downgradeWriteToUpdateLockForbidden();

        // Read lock is held
            assertTrue(rwuls().tryReadLock(), scenario + ": tryReadLock succeeds");
        updateUnlockForbidden();
        upgradeUpdateToWriteLockForbidden();
        downgradeUpdateToReadLockForbidden();
        downgradeWriteToUpdateLockForbidden();

        // allow upgrade to update lock
            assertTrue(rwuls().tryUpgradeReadToUpdateLock(), scenario + ": tryUpgradeReadToUpdateLock succeeds");

        // update lock is held
        readUnlockForbidden();
        writeUnlockForbidden();
        upgradeReadToUpdateLockForbidden();
        upgradeReadToWriteLockForbidden();
        downgradeWriteToUpdateLockForbidden();
        downgradeWriteToReadLockForbidden();

        // allow unlock
        rwuls().updateUnlock();
            assertTrue(rwuls().tryUpdateLock(), scenario + ": tryUpdateLock succeeds after updateUnlock");

        // allow upgrade to write lock
            assertTrue(rwuls().tryUpgradeUpdateToWriteLock(), scenario + ": tryUpgradeUpdateToWriteLock succeeds");

        // write lock is held
        updateUnlockForbidden();
        upgradeReadToUpdateLockForbidden();
        upgradeUpdateToWriteLockForbidden();
        downgradeUpdateToReadLockForbidden();

        // allow downgrade to update lock
        rwuls().downgradeWriteToUpdateLock();

        rwuls().updateUnlock();
        } finally {
            tearDown();
        }
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
        assumeTrue(lockingStrategy instanceof ReadWriteUpdateLockingStrategy,
                () -> "requires ReadWriteUpdateLockingStrategy but was " + lockingStrategy.getClass().getSimpleName());
    }

    private void assumeReadWriteLock() {
        assumeTrue(lockingStrategy instanceof ReadWriteLockingStrategy,
                () -> "requires ReadWriteLockingStrategy but was " + lockingStrategy.getClass().getSimpleName());
    }

    private ReadWriteLockState rwls() {
        return rwLockState;
    }

    private ReadWriteUpdateLockState rwuls() {
        return rwuLockState;
    }

    enum AccessMethod {ADDRESS, BYTES_WITH_OFFSET}

    @SuppressWarnings("unchecked")
    private class ReadWriteLockStateAdapter extends AbstractReadWriteLockState {

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
    private class ReadWriteUpdateLockStateAdapter extends ReadWriteLockStateAdapter
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
