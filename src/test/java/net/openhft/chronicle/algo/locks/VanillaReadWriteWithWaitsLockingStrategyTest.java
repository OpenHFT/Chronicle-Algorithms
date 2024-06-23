package net.openhft.chronicle.algo.locks;

import net.openhft.chronicle.algo.bytes.Access;
import net.openhft.chronicle.algo.bytes.ReadAccess;
import net.openhft.chronicle.algo.locks.VanillaReadWriteWithWaitsLockingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class VanillaReadWriteWithWaitsLockingStrategyTest {

    private Access<Object> access;
    private ReadAccess<Object> readAccess;
    private Object handle;
    private ReadWriteWithWaitsLockingStrategy strategy;

    @BeforeEach
    void setUp() {
        access = mock(Access.class);
        readAccess = mock(ReadAccess.class);
        handle = new Object();
        strategy = VanillaReadWriteWithWaitsLockingStrategy.instance();
    }

    @Test
    void testRwReadLocked() {
        long lock = 5L;
        int expectedReadLocks = 5;
        int readLocks = VanillaReadWriteWithWaitsLockingStrategy.rwReadLocked(lock);
        assertEquals(expectedReadLocks, readLocks);
    }

    @Test
    void testIsWriteLocked() {
        long state = VanillaReadWriteWithWaitsLockingStrategy.RW_WRITE_LOCKED;
        assertTrue(strategy.isWriteLocked(state));

        long nonWriteLockedState = 0;
        assertFalse(strategy.isWriteLocked(nonWriteLockedState));
    }

    @Test
    void testReadLockCount() {
        long state = 7L; // 7 read locks
        int expectedReadLockCount = 7;
        int readLockCount = strategy.readLockCount(state);
        assertEquals(expectedReadLockCount, readLockCount);
    }

    @Test
    void testRegisterWait() {
        long offset = 0L;
        long lock = 0L;
        when(access.readLong(handle, offset)).thenReturn(lock);
        when(access.compareAndSwapLong(handle, offset, lock, lock + VanillaReadWriteWithWaitsLockingStrategy.RW_WRITE_WAITING)).thenReturn(true);

        strategy.registerWait(access, handle, offset);
        verify(access).compareAndSwapLong(handle, offset, lock, lock + VanillaReadWriteWithWaitsLockingStrategy.RW_WRITE_WAITING);
    }

    @Test
    void testWaitCount() {
        long state = VanillaReadWriteWithWaitsLockingStrategy.RW_WRITE_WAITING * 3; // 3 waits
        int expectedWaitCount = 3;
        int waitCount = strategy.waitCount(state);
        assertEquals(expectedWaitCount, waitCount);
    }

    @Test
    void testIsLocked() {
        long lockedState = VanillaReadWriteWithWaitsLockingStrategy.RW_WRITE_LOCKED;
        assertTrue(strategy.isLocked(lockedState));

        long nonLockedState = 0;
        assertFalse(strategy.isLocked(nonLockedState));
    }

    @Test
    void testLockCount() {
        long state = VanillaReadWriteWithWaitsLockingStrategy.RW_WRITE_LOCKED + 3; // 3 read locks + 1 write lock
        int expectedLockCount = 4;

        int lockCount = strategy.lockCount(state);
        assertEquals(expectedLockCount, lockCount);
    }

    @Test
    void testToString() {
        long state = VanillaReadWriteWithWaitsLockingStrategy.RW_WRITE_WAITING + 3; // 3 read locks, 1 wait
        String expectedString = "[read locks = 3, write locked = false, waits = 1]";

        String lockStateString = strategy.toString(state);
        assertEquals(expectedString, lockStateString);
    }

    @Test
    void testSizeInBytes() {
        int expectedSize = 8;
        assertEquals(expectedSize, strategy.sizeInBytes());
    }
}
