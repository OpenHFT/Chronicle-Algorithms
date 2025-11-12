/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.algo.bytes;

import net.openhft.chronicle.algo.bytes.ByteBufferAccessor.Direct;
import net.openhft.chronicle.algo.bytes.ByteBufferAccessor.Generic;
import net.openhft.chronicle.algo.bytes.ByteBufferAccessor.Heap;
import net.openhft.chronicle.algo.bytes.BytesAccesses.Full;
import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.bytes.RandomDataInput;
import net.openhft.chronicle.core.Jvm;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class AccessUtilitiesTest {

    private static final int LENGTH = 32;
    private static final Random RANDOM = new Random(7);

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    void copyAndEquivalentAcrossAccessTypes() {
        ByteBuffer source = ByteBuffer.allocateDirect(LENGTH).order(ByteOrder.nativeOrder());
        ByteBuffer target = ByteBuffer.allocate(LENGTH).order(ByteOrder.nativeOrder());
        fillBuffer(source);

        Access.copy(ByteBufferAccess.INSTANCE, source, 0, ByteBufferAccess.INSTANCE, target, 0, LENGTH);
        assertTrue(Access.equivalent(ByteBufferAccess.INSTANCE, source, 0, ByteBufferAccess.INSTANCE, target, 0, LENGTH));

        BytesStore<?, ?> store = BytesStore.nativeStoreWithFixedCapacity(LENGTH);
        try {
            @SuppressWarnings("rawtypes")
            Access bytesAccess = Access.checkedBytesStoreAccess();
            // raw access types are required because the BytesStore generic is self-referential
            Access.copy(ByteBufferAccess.INSTANCE, source, 0, bytesAccess, store, 0, LENGTH);
            assertTrue(Access.equivalent(bytesAccess, store, 0, ByteBufferAccess.INSTANCE, source, 0, LENGTH));

            long index = 8;
            bytesAccess.writeLong(store, index, 0L);
            assertTrue(bytesAccess.compareAndSwapLong(store, index, 0L, 123L));
            assertEquals(123L, bytesAccess.readLong(store, index));
        } finally {
            store.releaseLast();
        }
    }

    @Test
    void copyHandlesMixedSizes() {
        ByteBuffer source = ByteBuffer.allocate(15).order(ByteOrder.nativeOrder());
        ByteBuffer target = ByteBuffer.allocate(20).order(ByteOrder.nativeOrder());
        fillBuffer(source);

        Access.copy(ByteBufferAccess.INSTANCE, source, 0, ByteBufferAccess.INSTANCE, target, 2, 15);
        assertTrue(Access.equivalent(ByteBufferAccess.INSTANCE, source, 0, ByteBufferAccess.INSTANCE, target, 2, 15));

        // early exit branch (source == target && offsets equal)
        Access.copy(ByteBufferAccess.INSTANCE, target, 0, ByteBufferAccess.INSTANCE, target, 0, 10);
    }

    @Test
    void compareAndSwapUnsupportedOnByteBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(16).order(ByteOrder.nativeOrder());
        assertThrows(UnsupportedOperationException.class,
                () -> ByteBufferAccess.INSTANCE.compareAndSwapLong(buffer, 0, 0L, 1L));
    }

    @Test
    void byteOrderReflectsBufferConfiguration() {
        ByteBuffer buffer = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN);
        assertEquals(ByteOrder.BIG_ENDIAN, ByteBufferAccess.INSTANCE.byteOrder(buffer));
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        assertEquals(ByteOrder.LITTLE_ENDIAN, ByteBufferAccess.INSTANCE.byteOrder(buffer));
    }

    private static void fillBuffer(ByteBuffer buffer) {
        for (int i = 0; i < buffer.capacity(); i++) {
            buffer.put(i, (byte) RANDOM.nextInt(256));
        }
    }

    @Test
    void byteBufferAccessorVariantsExposeOffsetsAndHandles() {
        ByteBuffer direct = ByteBuffer.allocateDirect(8);
        ByteBufferAccessor<?> directAccessor = ByteBufferAccessor.unchecked(direct);
        assertSame(Direct.INSTANCE, directAccessor);
        assertSame(NativeAccess.instance(), directAccessor.access());
        assertNull(((Direct) directAccessor).handle(direct));
        long directBase = directAccessor.offset(direct, 0);
        assertEquals(directBase + 7, directAccessor.offset(direct, 7));

        ByteBuffer heap = ByteBuffer.allocate(16);
        ByteBufferAccessor<?> heapAccessor = ByteBufferAccessor.unchecked(heap);
        assertSame(Heap.INSTANCE, heapAccessor);
        assertSame(NativeAccess.instance(), heapAccessor.access());
        assertSame(heap.array(), ((Heap) heapAccessor).handle(heap));
        assertEquals(heapAccessor.offset(heap, 0) + 5, heapAccessor.offset(heap, 5));

        ByteBufferAccessor<ByteBuffer> generic = ByteBufferAccessor.checked();
        assertSame(Generic.INSTANCE, generic);
        assertSame(ByteBufferAccess.INSTANCE, generic.access());
        assertSame(heap, generic.handle(heap));
        assertEquals(3L, generic.offset(heap, 3));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    void bytesAccessesFullDelegatesToBytesStore() {
        BytesStore<?, ?> store = BytesStore.nativeStoreWithFixedCapacity(32);
        try {
            @SuppressWarnings("rawtypes")
            Full access = (Full) Full.INSTANCE;
            assertTrue(access.compareAndSwapInt((BytesStore) store, 0, 0, 42));
            assertEquals(42, store.readInt(0));
            assertTrue(access.compareAndSwapLong((BytesStore) store, 8, 0L, 123L));
            assertEquals(123L, store.readLong(8));
            assertEquals(store.byteOrder(), access.byteOrder((BytesStore) store));
        } finally {
            store.releaseLast();
        }
    }

    @Test
    void randomDataInputAccessReadsPrimitiveValues() {
        BytesStore<?, ?> store = BytesStore.nativeStoreWithFixedCapacity(32);
        try {
            store.writeLong(0, 0x0102030405060708L);
            store.writeInt(16, 0x11223344);
            RandomDataInputAccess<RandomDataInput> access = BytesAccesses.RandomDataInputReadAccessEnum.INSTANCE;
            RandomDataInput handle = store;
            assertEquals(0x11223344, access.readInt(handle, 16));
            assertEquals(0x0102030405060708L, access.readLong(handle, 0));
            assertEquals(handle.byteOrder(), access.byteOrder(handle));
        } finally {
            store.releaseLast();
        }
    }

    @Test
    void arrayAccessorOffsetsScalePerElement() {
        boolean[] bools = new boolean[8];
        Access<boolean[]> boolAccess = ArrayAccessors.Boolean.INSTANCE.access();
        assertSame(NativeAccess.instance(), boolAccess);
        long base = ArrayAccessors.Boolean.INSTANCE.offset(bools, 0);
        assertEquals(base + 3, ArrayAccessors.Boolean.INSTANCE.offset(bools, 3));

        byte[] bytes = new byte[8];
        assertEquals(
                ArrayAccessors.Byte.INSTANCE.offset(bytes, 5) - ArrayAccessors.Byte.INSTANCE.offset(bytes, 0),
                5
        );
    }

    @Test
    void hotSpotStringAccessorExposesBackingStorage() {
        String sample = "Cafe";
        Object handle = CharSequenceAccessor.stringAccessor.handle(sample);
        if (Jvm.isJava9Plus()) {
            assertTrue(handle instanceof byte[]);
            byte[] asBytes = (byte[]) handle;
            assertTrue(asBytes.length >= sample.length());
        } else {
            assertTrue(handle instanceof char[]);
            char[] asChars = (char[]) handle;
            assertEquals(sample.length(), asChars.length);
        }
        long base = CharSequenceAccessor.stringAccessor.offset(sample, 0);
        long second = CharSequenceAccessor.stringAccessor.offset(sample, 1);
        long third = CharSequenceAccessor.stringAccessor.offset(sample, 3);
        assertTrue(second > base);
        assertTrue(third > second);
    }
}
