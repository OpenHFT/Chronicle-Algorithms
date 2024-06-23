package net.openhft.chronicle.algo.locks;

import net.openhft.chronicle.algo.bytes.Access;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class VanillaReadWriteUpdateWithWaitsLockingStrategyTest {

    private Access<Object> access;
    private Object handle;
    private VanillaReadWriteUpdateWithWaitsLockingStrategy strategy;

    @BeforeEach
    void setUp() {
        access = mock(Access.class);
        handle = new Object();
        strategy = (VanillaReadWriteUpdateWithWaitsLockingStrategy) VanillaReadWriteUpdateWithWaitsLockingStrategy.instance();
    }

    @Test
    void testSingletonInstance() {
        ReadWriteUpdateWithWaitsLockingStrategy instance1 = VanillaReadWriteUpdateWithWaitsLockingStrategy.instance();
        ReadWriteUpdateWithWaitsLockingStrategy instance2 = VanillaReadWriteUpdateWithWaitsLockingStrategy.instance();
        assertEquals(instance1, instance2, "Expected the same instance to be returned each time.");
    }

    @Test
    void testCasLockWord() {
        long offset = 0L;
        long expected = 1L;
        long x = 2L;
        when(access.compareAndSwapLong(handle, offset, expected, x)).thenReturn(true);

        boolean result = VanillaReadWriteUpdateWithWaitsLockingStrategy.casLockWord(access, handle, offset, expected, x);
        assertTrue(result, "Expected compareAndSwapLong to return true.");
        verify(access).compareAndSwapLong(handle, offset, expected, x);
    }

    @Test
    void testLockWord() {
        int countWord = 123;
        int waitWord = 456;

        long expectedLockWord = ((((long) countWord) & 0xFFFFFFFFL) << VanillaReadWriteUpdateWithWaitsLockingStrategy.COUNT_WORD_SHIFT) |
                ((((long) waitWord) & 0xFFFFFFFFL) << VanillaReadWriteUpdateWithWaitsLockingStrategy.WAIT_WORD_SHIFT);

        long lockWord = VanillaReadWriteUpdateWithWaitsLockingStrategy.lockWord(countWord, waitWord);
        assertEquals(expectedLockWord, lockWord, "Expected lockWord to combine countWord and waitWord correctly.");
    }

    @Test
    void testPutCountWord() {
        long offset = 0L;
        int countWord = 123;

        doNothing().when(access).writeOrderedInt(handle, offset + VanillaReadWriteUpdateWithWaitsLockingStrategy.COUNT_WORD_OFFSET, countWord);

        VanillaReadWriteUpdateWithWaitsLockingStrategy.putCountWord(access, handle, offset, countWord);
        verify(access).writeOrderedInt(handle, offset + VanillaReadWriteUpdateWithWaitsLockingStrategy.COUNT_WORD_OFFSET, countWord);
    }

    @Test
    void testGetWaitWord() {
        long offset = 0L;
        int expectedWaitWord = 123;
        when(access.readVolatileInt(handle, offset + VanillaReadWriteUpdateWithWaitsLockingStrategy.WAIT_WORD_OFFSET)).thenReturn(expectedWaitWord);

        int waitWord = strategy.getWaitWord(access, handle, offset);
        assertEquals(expectedWaitWord, waitWord);
        verify(access).readVolatileInt(handle, offset + VanillaReadWriteUpdateWithWaitsLockingStrategy.WAIT_WORD_OFFSET);
    }

    @Test
    void testCasWaitWord() {
        long offset = 0L;
        int expected = 1;
        int x = 2;
        when(access.compareAndSwapInt(handle, offset + VanillaReadWriteUpdateWithWaitsLockingStrategy.WAIT_WORD_OFFSET, expected, x)).thenReturn(true);

        boolean result = strategy.casWaitWord(access, handle, offset, expected, x);
        assertTrue(result);
        verify(access).compareAndSwapInt(handle, offset + VanillaReadWriteUpdateWithWaitsLockingStrategy.WAIT_WORD_OFFSET, expected, x);
    }

    @Test
    void testCheckWaitWordForIncrement() {
        int waitWord = VanillaReadWriteUpdateWithWaitsLockingStrategy.MAX_WAIT - 1;
        assertDoesNotThrow(() -> strategy.checkWaitWordForIncrement(waitWord));

        int maxWaitWord = VanillaReadWriteUpdateWithWaitsLockingStrategy.MAX_WAIT;
        assertThrows(IllegalMonitorStateException.class, () -> strategy.checkWaitWordForIncrement(maxWaitWord));
    }

    @Test
    void testCheckWaitWordForDecrement() {
        int waitWord = 1;
        assertDoesNotThrow(() -> strategy.checkWaitWordForDecrement(waitWord));

        int zeroWaitWord = 0;
        assertThrows(IllegalMonitorStateException.class, () -> strategy.checkWaitWordForDecrement(zeroWaitWord));
    }

    @Test
    void testTryWriteLockAndDeregisterWait0() {
        long offset = 0L;
        long lockWord = VanillaReadWriteUpdateWithWaitsLockingStrategy.lockWord(0, 1);
        int waitWord = 1;
        when(access.compareAndSwapLong(handle, offset, lockWord, VanillaReadWriteUpdateWithWaitsLockingStrategy.lockWord(VanillaReadWriteUpdateWithWaitsLockingStrategy.WRITE_LOCKED_COUNT_WORD, waitWord - VanillaReadWriteUpdateWithWaitsLockingStrategy.WAIT_PARTY))).thenReturn(true);

        boolean result = strategy.tryWriteLockAndDeregisterWait0(access, handle, offset, lockWord);
        assertTrue(result);
        verify(access).compareAndSwapLong(handle, offset, lockWord, VanillaReadWriteUpdateWithWaitsLockingStrategy.lockWord(VanillaReadWriteUpdateWithWaitsLockingStrategy.WRITE_LOCKED_COUNT_WORD, waitWord - VanillaReadWriteUpdateWithWaitsLockingStrategy.WAIT_PARTY));
    }

    @Test
    void testCheckExclusiveUpdateLocked() {
        int countWord = VanillaReadWriteUpdateWithWaitsLockingStrategy.UPDATE_PARTY;
        assertTrue(strategy.checkExclusiveUpdateLocked(countWord));

        int nonExclusiveUpdateCountWord = VanillaReadWriteUpdateWithWaitsLockingStrategy.UPDATE_PARTY + 1;
        assertFalse(strategy.checkExclusiveUpdateLocked(nonExclusiveUpdateCountWord));
    }

    @Test
    void testResetState() {
        long state = strategy.resetState();
        assertEquals(0L, state);
    }

    @Test
    void testResetKeepingWaits() {
        long offset = 0L;
        doNothing().when(access).writeOrderedInt(handle, offset + VanillaReadWriteUpdateWithWaitsLockingStrategy.COUNT_WORD_OFFSET, 0);

        strategy.resetKeepingWaits(access, handle, offset);
        verify(access).writeOrderedInt(handle, offset + VanillaReadWriteUpdateWithWaitsLockingStrategy.COUNT_WORD_OFFSET, 0);
    }

    @Test
    void testRegisterWait() {
        long offset = 0L;
        int waitWord = 1;
        when(access.readVolatileInt(handle, offset + VanillaReadWriteUpdateWithWaitsLockingStrategy.WAIT_WORD_OFFSET)).thenReturn(waitWord);
        when(access.compareAndSwapInt(handle, offset + VanillaReadWriteUpdateWithWaitsLockingStrategy.WAIT_WORD_OFFSET, waitWord, waitWord + VanillaReadWriteUpdateWithWaitsLockingStrategy.WAIT_PARTY)).thenReturn(true);

        strategy.registerWait(access, handle, offset);
        verify(access).compareAndSwapInt(handle, offset + VanillaReadWriteUpdateWithWaitsLockingStrategy.WAIT_WORD_OFFSET, waitWord, waitWord + VanillaReadWriteUpdateWithWaitsLockingStrategy.WAIT_PARTY);
    }

    @Test
    void testDeregisterWait() {
        long offset = 0L;
        int waitWord = 1;
        when(access.readVolatileInt(handle, offset + VanillaReadWriteUpdateWithWaitsLockingStrategy.WAIT_WORD_OFFSET)).thenReturn(waitWord);
        when(access.compareAndSwapInt(handle, offset + VanillaReadWriteUpdateWithWaitsLockingStrategy.WAIT_WORD_OFFSET, waitWord, waitWord - VanillaReadWriteUpdateWithWaitsLockingStrategy.WAIT_PARTY)).thenReturn(true);

        strategy.deregisterWait(access, handle, offset);
        verify(access).compareAndSwapInt(handle, offset + VanillaReadWriteUpdateWithWaitsLockingStrategy.WAIT_WORD_OFFSET, waitWord, waitWord - VanillaReadWriteUpdateWithWaitsLockingStrategy.WAIT_PARTY);
    }

    @Test
    void testIsUpdateLocked() {
        long state = VanillaReadWriteUpdateWithWaitsLockingStrategy.lockWord(VanillaReadWriteUpdateWithWaitsLockingStrategy.UPDATE_PARTY, 0);
        assertTrue(strategy.isUpdateLocked(state));

        long nonUpdateLockedState = 0;
        assertFalse(strategy.isUpdateLocked(nonUpdateLockedState));
    }

    @Test
    void testReadLockCount() {
        long state = VanillaReadWriteUpdateWithWaitsLockingStrategy.lockWord(VanillaReadWriteUpdateWithWaitsLockingStrategy.READ_PARTY, 0);
        int expectedReadLockCount = 1;

        int readLockCount = strategy.readLockCount(state);
        assertEquals(expectedReadLockCount, readLockCount);
    }

    @Test
    void testIsWriteLocked() {
        long state = VanillaReadWriteUpdateWithWaitsLockingStrategy.lockWord(VanillaReadWriteUpdateWithWaitsLockingStrategy.WRITE_LOCKED_COUNT_WORD, 0);
        assertTrue(strategy.isWriteLocked(state));

        long nonWriteLockedState = 0;
        assertFalse(strategy.isWriteLocked(nonWriteLockedState));
    }

    @Test
    void testWaitCount() {
        long state = VanillaReadWriteUpdateWithWaitsLockingStrategy.lockWord(0, 1);
        int expectedWaitCount = 1;

        int waitCount = strategy.waitCount(state);
        assertEquals(expectedWaitCount, waitCount);
    }

    @Test
    void testIsLocked() {
        long lockedState = VanillaReadWriteUpdateWithWaitsLockingStrategy.lockWord(1, 0);
        assertTrue(strategy.isLocked(lockedState));

        long nonLockedState = 0;
        assertFalse(strategy.isLocked(nonLockedState));
    }

    @Test
    void testToString() {
        long state = VanillaReadWriteUpdateWithWaitsLockingStrategy.lockWord(1, 1);
        String expectedString = "[read locks = 1, update locked = false, write locked = false, waits = 1]";

        String lockStateString = strategy.toString(state);
        assertEquals(expectedString, lockStateString);
    }

    @Test
    void testSizeInBytes() {
        int expectedSize = 8;
        assertEquals(expectedSize, strategy.sizeInBytes());
    }

}
