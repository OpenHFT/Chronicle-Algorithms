package net.openhft.chronicle.algo.bytes;

import junit.framework.TestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.OngoingStubbing;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AccessTest extends TestCase {

    private Access<Object> access;
    private Object handle;
    private Access<Object> sourceAccess;
    private Access<Object> targetAccess;
    private Object source;
    private Object target;

    @BeforeEach
    public void setUp() {
        access = mock(Access.class);
        handle = new Object();
        sourceAccess = mock(Access.class);
        targetAccess = mock(Access.class);
        source = new Object();
        target = new Object();
    }

    @Test
    public void testCompareAndSwapInt() {
        long offset = 0L;
        int expected = 10;
        int value = 20;

        when(access.compareAndSwapInt(handle, offset, expected, value)).thenReturn(true);

        assertTrue(access.compareAndSwapInt(handle, offset, expected, value));
        verify(access).compareAndSwapInt(handle, offset, expected, value);
    }

    @Test
    public void testCompareAndSwapLong() {
        long offset = 0L;
        long expected = 10L;
        long value = 20L;

        when(access.compareAndSwapLong(handle, offset, expected, value)).thenReturn(true);

        assertTrue(access.compareAndSwapLong(handle, offset, expected, value));
        verify(access).compareAndSwapLong(handle, offset, expected, value);
    }
}
