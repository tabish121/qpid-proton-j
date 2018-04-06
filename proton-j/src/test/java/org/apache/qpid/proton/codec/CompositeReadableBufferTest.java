/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.qpid.proton.codec;

import static org.junit.Assert.*;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.InvalidMarkException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

/**
 * Test for API of the CompositeReadableBuffer class.
 */
public class CompositeReadableBufferTest {

    //----- Test newly create buffer behaviors -------------------------------//

    @Test
    public void testDefaultCtor() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        assertFalse(buffer.hasArray());
        assertEquals(0, buffer.remaining());
        assertEquals(0, buffer.position());
        assertEquals(0, buffer.limit());
    }

    //----- Test limit handling ----------------------------------------------//

    @Test
    public void testLimitAppliesUpdatesToPositionAndMark() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        buffer.append(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 });
        buffer.position(10);
        buffer.mark();

        assertEquals(10, buffer.capacity());
        assertEquals(10, buffer.limit());
        assertEquals(10, buffer.position());

        buffer.limit(5);
        assertEquals(5, buffer.limit());
        assertEquals(5, buffer.position());

        try {
            buffer.reset();
            fail("Should throw a InvalidMarkException");
        } catch (InvalidMarkException e) {}

        buffer.mark();
        buffer.limit(10);
        buffer.position(10);

        try {
            buffer.reset();
        } catch (InvalidMarkException e) {
            fail("Should not throw a InvalidMarkException");
        }

        assertEquals(5, buffer.position());
    }

    @Test
    public void testLimitAppliesUpdatesToPositionAndMarkWithTwoArrays() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        buffer.append(new byte[] { 0, 1, 2, 3, 4}).append(new byte[] { 5, 6, 7, 8, 9 });
        buffer.position(10);
        buffer.mark();

        assertEquals(10, buffer.capacity());
        assertEquals(10, buffer.limit());
        assertEquals(10, buffer.position());

        buffer.limit(5);
        assertEquals(5, buffer.limit());
        assertEquals(5, buffer.position());

        try {
            buffer.reset();
            fail("Should throw a InvalidMarkException");
        } catch (InvalidMarkException e) {}

        buffer.mark();
        buffer.limit(10);
        buffer.position(10);

        try {
            buffer.reset();
        } catch (InvalidMarkException e) {
            fail("Should not throw a InvalidMarkException");
        }

        assertEquals(5, buffer.position());
    }

    @Test
    public void testLimitWithOneArrayAppended() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        buffer.append(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 });

        assertEquals(10, buffer.capacity());
        assertEquals(10, buffer.limit());

        buffer.limit(5);
        assertEquals(5, buffer.limit());

        buffer.limit(6);
        assertEquals(6, buffer.limit());

        try {
            buffer.limit(11);
            fail("Should throw a IllegalArgumentException");
        } catch (IllegalArgumentException e) {}
    }

    @Test
    public void testLimitWithTwoArraysAppended() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        buffer.append(new byte[] { 0, 1, 2, 3, 4}).append(new byte[] { 5, 6, 7, 8, 9 });

        assertEquals(10, buffer.capacity());
        assertEquals(10, buffer.limit());

        buffer.limit(5);
        assertEquals(5, buffer.limit());

        buffer.limit(6);
        assertEquals(6, buffer.limit());

        try {
            buffer.limit(11);
            fail("Should throw a IllegalArgumentException");
        } catch (IllegalArgumentException e) {}
    }

    @Test
    public void testLimitEnforcesPreconditions() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        // test with nothing appended.
        try {
            buffer.limit(2);
            fail("Should throw a IllegalArgumentException");
        } catch (IllegalArgumentException e) {}

        try {
            buffer.limit(-1);
            fail("Should throw a IllegalArgumentException");
        } catch (IllegalArgumentException e) {}

        // Test with something appended
        buffer.append(new byte[] { 127 });

        try {
            buffer.limit(2);
            fail("Should throw a IllegalArgumentException");
        } catch (IllegalArgumentException e) {}

        try {
            buffer.limit(-1);
            fail("Should throw a IllegalArgumentException");
        } catch (IllegalArgumentException e) {}
    }

    //----- Test position handling -------------------------------------------//

    @Test
    public void testPositionWithOneArrayAppended() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        buffer.append(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 });

        assertEquals(10, buffer.capacity());
        assertEquals(10, buffer.limit());
        assertEquals(0, buffer.position());

        buffer.position(5);
        assertEquals(5, buffer.position());

        buffer.position(6);
        assertEquals(6, buffer.position());

        buffer.position(10);
        assertEquals(10, buffer.position());

        try {
            buffer.position(11);
            fail("Should throw a IllegalArgumentException");
        } catch (IllegalArgumentException e) {}

        buffer.mark();

        buffer.position(0);
        assertEquals(0, buffer.position());

        try {
            buffer.reset();
            fail("Should throw InvalidMarkException");
        } catch (InvalidMarkException ime) {}
    }

    @Test
    public void testPositionWithTwoArraysAppended() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        buffer.append(new byte[] { 0, 1, 2, 3, 4}).append(new byte[] { 5, 6, 7, 8, 9 });

        assertEquals(10, buffer.capacity());
        assertEquals(10, buffer.limit());

        buffer.position(5);
        assertEquals(5, buffer.position());

        buffer.position(6);
        assertEquals(6, buffer.position());

        buffer.position(10);
        assertEquals(10, buffer.position());

        try {
            buffer.position(11);
            fail("Should throw a IllegalArgumentException");
        } catch (IllegalArgumentException e) {}

        buffer.position(0);
        assertEquals(0, buffer.position());
    }

    @Test
    public void testPositionEnforcesPreconditions() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        // test with nothing appended.
        try {
            buffer.position(2);
            fail("Should throw a IllegalArgumentException");
        } catch (IllegalArgumentException e) {}

        try {
            buffer.position(-1);
            fail("Should throw a IllegalArgumentException");
        } catch (IllegalArgumentException e) {}

        // Test with something appended
        buffer.append(new byte[] { 127 });

        try {
            buffer.position(2);
            fail("Should throw a IllegalArgumentException");
        } catch (IllegalArgumentException e) {}

        try {
            buffer.position(-1);
            fail("Should throw a IllegalArgumentException");
        } catch (IllegalArgumentException e) {}
    }

    //----- Test buffer get methods ------------------------------------------//

    @Test
    public void testGetByteWithNothingAppended() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        try {
            buffer.get();
            fail("Should throw a BufferUnderflowException");
        } catch (BufferUnderflowException e) {}
    }

    @Test
    public void testGetByteWithOneArrayWithOneElement() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        buffer.append(new byte[] { 127 });

        assertEquals(1, buffer.remaining());
        assertTrue(buffer.hasRemaining());
        assertEquals(0, buffer.position());

        assertEquals(127, buffer.get());

        assertEquals(0, buffer.remaining());
        assertFalse(buffer.hasRemaining());
        assertEquals(1, buffer.position());

        try {
            buffer.get();
            fail("Should not be able to read past end");
        } catch (BufferUnderflowException e) {}
    }

    @Test
    public void testGetByteWithOneArrayWithManyElements() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        buffer.append(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 });

        assertEquals(10, buffer.remaining());
        assertTrue(buffer.hasRemaining());
        assertEquals(0, buffer.position());

        for (int i = 0; i < 10; ++i) {
            assertEquals(i, buffer.get());
            assertEquals(i + 1, buffer.position());
        }

        assertEquals(0, buffer.remaining());
        assertEquals(10, buffer.position());

        try {
            buffer.get();
            fail("Should not be able to read past end");
        } catch (BufferUnderflowException e) {}
    }

    @Test
    public void testGetByteWithManyArraysWithOneElements() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        buffer.append(new byte[] {0})
              .append(new byte[] {1})
              .append(new byte[] {2})
              .append(new byte[] {3})
              .append(new byte[] {4})
              .append(new byte[] {5})
              .append(new byte[] {6})
              .append(new byte[] {7})
              .append(new byte[] {8})
              .append(new byte[] {9});

        assertEquals(10, buffer.remaining());
        assertFalse(buffer.hasArray());
        assertTrue(buffer.hasRemaining());
        assertEquals(0, buffer.position());

        for (int i = 0; i < 10; ++i) {
            assertEquals(i, buffer.get());
        }

        assertEquals(0, buffer.remaining());
        assertEquals(10, buffer.position());

        try {
            buffer.get();
            fail("Should not be able to read past end");
        } catch (BufferUnderflowException e) {}
    }

    @Test
    public void testGetByteWithManyArraysWithVariedElements() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        buffer.append(new byte[] {0})
              .append(new byte[] {1, 2})
              .append(new byte[] {3, 4, 5})
              .append(new byte[] {6})
              .append(new byte[] {7, 8, 9});

        assertEquals(10, buffer.remaining());
        assertFalse(buffer.hasArray());
        assertTrue(buffer.hasRemaining());
        assertEquals(0, buffer.position());

        for (int i = 0; i < 10; ++i) {
            assertEquals(i, buffer.get());
        }

        assertEquals(0, buffer.remaining());
        assertEquals(10, buffer.position());

        try {
            buffer.get();
            fail("Should not be able to read past end");
        } catch (BufferUnderflowException e) {}
    }

    @Test
    public void testGetShortByteWithNothingAppended() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        try {
            buffer.getShort();
            fail("Should throw a BufferUnderflowException");
        } catch (BufferUnderflowException e) {}
    }

    @Test
    public void testGetShortWithOneArrayWithOneElement() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        buffer.append(new byte[] { 8, 0 });

        assertEquals(2, buffer.remaining());
        assertTrue(buffer.hasRemaining());
        assertEquals(0, buffer.position());

        assertEquals(2048, buffer.getShort());

        assertEquals(0, buffer.remaining());
        assertFalse(buffer.hasRemaining());
        assertEquals(2, buffer.position());

        try {
            buffer.getShort();
            fail("Should not be able to read past end");
        } catch (BufferUnderflowException e) {}
    }

    @Test
    public void testGetShortWithTwoArraysContainingOneElement() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        buffer.append(new byte[] {8}).append(new byte[] {0});

        assertEquals(2, buffer.remaining());
        assertTrue(buffer.hasRemaining());
        assertEquals(0, buffer.position());

        assertEquals(2048, buffer.getShort());

        assertEquals(0, buffer.remaining());
        assertFalse(buffer.hasRemaining());
        assertEquals(2, buffer.position());

        try {
            buffer.getShort();
            fail("Should not be able to read past end");
        } catch (BufferUnderflowException e) {}
    }

    @Test
    public void testGetIntByteWithNothingAppended() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        try {
            buffer.getInt();
            fail("Should throw a BufferUnderflowException");
        } catch (BufferUnderflowException e) {}
    }

    @Test
    public void testGetIntWithOneArrayWithOneElement() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        buffer.append(new byte[] { 0, 0, 8, 0 });

        assertEquals(4, buffer.remaining());
        assertTrue(buffer.hasRemaining());
        assertEquals(0, buffer.position());

        assertEquals(2048, buffer.getInt());

        assertEquals(0, buffer.remaining());
        assertFalse(buffer.hasRemaining());
        assertEquals(4, buffer.position());

        try {
            buffer.getInt();
            fail("Should not be able to read past end");
        } catch (BufferUnderflowException e) {}
    }

    @Test
    public void testGetIntWithTwoArraysContainingOneElement() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        buffer.append(new byte[] { 0 ,0 }).append(new byte[] { 8, 0 });

        assertEquals(4, buffer.remaining());
        assertTrue(buffer.hasRemaining());
        assertEquals(0, buffer.position());

        assertEquals(2048, buffer.getInt());

        assertEquals(0, buffer.remaining());
        assertFalse(buffer.hasRemaining());
        assertEquals(4, buffer.position());

        try {
            buffer.getInt();
            fail("Should not be able to read past end");
        } catch (BufferUnderflowException e) {}
    }

    @Test
    public void testGetLongByteWithNothingAppended() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        try {
            buffer.getLong();
            fail("Should throw a BufferUnderflowException");
        } catch (BufferUnderflowException e) {}
    }

    @Test
    public void testGetLongWithOneArrayWithOneElement() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        buffer.append(new byte[] { 0, 0, 0, 0, 0, 0, 8, 0 });

        assertEquals(8, buffer.remaining());
        assertTrue(buffer.hasRemaining());
        assertEquals(0, buffer.position());

        assertEquals(2048, buffer.getLong());

        assertEquals(0, buffer.remaining());
        assertFalse(buffer.hasRemaining());
        assertEquals(8, buffer.position());

        try {
            buffer.getLong();
            fail("Should not be able to read past end");
        } catch (BufferUnderflowException e) {}
    }

    @Test
    public void testGetLongWithTwoArraysContainingOneElement() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        buffer.append(new byte[] { 0 ,0, 0, 0 }).append(new byte[] { 0, 0, 8, 0 });

        assertEquals(8, buffer.remaining());
        assertTrue(buffer.hasRemaining());
        assertEquals(0, buffer.position());

        assertEquals(2048, buffer.getLong());

        assertEquals(0, buffer.remaining());
        assertFalse(buffer.hasRemaining());
        assertEquals(8, buffer.position());

        try {
            buffer.getLong();
            fail("Should not be able to read past end");
        } catch (BufferUnderflowException e) {}
    }

    @Test
    public void testGetByteArrayWithContentsInSingleArray() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        byte[] data = new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        buffer.append(data);

        assertEquals(data.length, buffer.limit());
        byte array[] = new byte[1];

        for (int i = 0; i < data.length; i++) {
            assertEquals(buffer.position(), i);
            ReadableBuffer self = buffer.get(array);
            assertEquals(array[0], buffer.get(i));
            assertSame(self, buffer);
        }

        try {
            buffer.get(array);
            fail("Should throw BufferUnderflowException");
        } catch (BufferUnderflowException e) {
        }

        try {
            buffer.get((byte[]) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testGetWritableBufferWithContentsInSingleArray() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        byte[] data = new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        buffer.append(data);

        assertEquals(data.length, buffer.limit());

        ByteBuffer destination = ByteBuffer.allocate(1);
        WritableBuffer target = WritableBuffer.ByteBufferWrapper.wrap(destination);

        for (int i = 0; i < data.length; i++) {
            assertEquals(buffer.position(), i);
            ReadableBuffer self = buffer.get(target);
            assertEquals(destination.get(0), buffer.get(i));
            assertSame(self, buffer);
            destination.rewind();
        }

        try {
            buffer.get(target);
        } catch (Throwable e) {
            fail("Should not throw: " + e.getClass().getSimpleName());
        }

        try {
            buffer.get((WritableBuffer) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testGetWritableBufferWithContentsInSeveralArrays() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        byte[] data1 = new byte[] {0, 1, 2, 3, 4};
        byte[] data2 = new byte[] {5, 6, 7, 8, 9};
        byte[] data3 = new byte[] {10, 11, 12};

        int size = data1.length + data2.length + data3.length;

        buffer.append(data1).append(data2).append(data3);

        assertEquals(size, buffer.limit());

        ByteBuffer destination = ByteBuffer.allocate(1);
        WritableBuffer target = WritableBuffer.ByteBufferWrapper.wrap(destination);

        for (int i = 0; i < size; i++) {
            assertEquals(buffer.position(), i);
            ReadableBuffer self = buffer.get(target);
            assertEquals(destination.get(0), buffer.get(i));
            assertSame(self, buffer);
            destination.rewind();
        }

        try {
            buffer.get(target);
        } catch (Throwable e) {
            fail("Should not throw: " + e.getClass().getSimpleName());
        }

        try {
            buffer.get((WritableBuffer) null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testGetintWithContentsInSingleArray() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        byte[] data = new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        buffer.append(data);

        for (int i = 0; i < buffer.capacity(); i++) {
            assertEquals(buffer.position(), i);
            assertEquals(buffer.get(i), buffer.get());
        }

        buffer.rewind();

        for (int i = 0; i < buffer.capacity(); i++) {
            assertEquals(buffer.position(), i);
            assertEquals(buffer.get(), buffer.get(i));
        }

        try {
            buffer.get(-1);
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            buffer.get(buffer.limit());
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
    }

    @Test
    public void testGetintWithContentsInMultipleArrays() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        buffer.append(new byte[] {0, 1, 2, 3, 4}).append(new byte[] {5, 6, 7, 8, 9});

        for (int i = 0; i < buffer.capacity(); i++) {
            assertEquals(buffer.position(), i);
            assertEquals(buffer.get(), buffer.get(i));
        }

        try {
            buffer.get(-1);
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            buffer.get(buffer.limit());
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
    }

    @Test
    public void testGetbyteArrayIntIntWithContentsInSingleArray() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        byte[] data = new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        buffer.append(data);

        byte array[] = new byte[data.length];

        try {
            buffer.get(new byte[data.length + 1], 0, data.length + 1);
            fail("Should throw BufferUnderflowException");
        } catch (BufferUnderflowException e) {
        }

        assertEquals(buffer.position(), 0);

        try {
            buffer.get(array, -1, array.length);
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }

        buffer.get(array, array.length, 0);

        try {
            buffer.get(array, array.length + 1, 1);
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }

        assertEquals(buffer.position(), 0);

        try {
            buffer.get(array, 2, -1);
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            buffer.get(array, 2, array.length);
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            buffer.get((byte[])null, -1, 0);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
        }

        try {
            buffer.get(array, 1, Integer.MAX_VALUE);
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            buffer.get(array, Integer.MAX_VALUE, 1);
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }

        assertEquals(buffer.position(), 0);

        CompositeReadableBuffer self = buffer.get(array, 0, array.length);
        assertEquals(buffer.position(), buffer.capacity());
        assertContentEquals(buffer, array, 0, array.length);
        assertSame(self, buffer);
    }

    @Test
    public void testGetbyteArrayIntIntWithContentsInMultipleArrays() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        byte[] data1 = new byte[] {0, 1, 2, 3, 4};
        byte[] data2 = new byte[] {5, 6, 7, 8, 9};

        final int dataLength = data1.length + data2.length;

        buffer.append(data1).append(data2);

        assertEquals(dataLength, buffer.remaining());

        byte array[] = new byte[buffer.remaining()];

        try {
            buffer.get(new byte[dataLength + 1], 0, dataLength + 1);
            fail("Should throw BufferUnderflowException");
        } catch (BufferUnderflowException e) {
        }

        assertEquals(buffer.position(), 0);

        try {
            buffer.get(array, -1, array.length);
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }

        buffer.get(array, array.length, 0);

        try {
            buffer.get(array, array.length + 1, 1);
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }

        assertEquals(buffer.position(), 0);

        try {
            buffer.get(array, 2, -1);
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            buffer.get(array, 2, array.length);
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            buffer.get((byte[])null, -1, 0);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
        }

        try {
            buffer.get(array, 1, Integer.MAX_VALUE);
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            buffer.get(array, Integer.MAX_VALUE, 1);
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }

        assertEquals(buffer.position(), 0);

        CompositeReadableBuffer self = buffer.get(array, 0, array.length);
        assertEquals(buffer.position(), buffer.capacity());
        assertContentEquals(buffer, array, 0, array.length);
        assertSame(self, buffer);
    }

    @Test
    public void testGetFloat() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        buffer.append(float2bytes(3.14f));

        assertEquals(4, buffer.remaining());
        assertTrue(buffer.hasRemaining());
        assertEquals(0, buffer.position());

        assertEquals(3.14f, buffer.getFloat(), 0.1);

        assertEquals(0, buffer.remaining());
        assertFalse(buffer.hasRemaining());
        assertEquals(4, buffer.position());

        try {
            buffer.getFloat();
            fail("Should not be able to read past end");
        } catch (BufferUnderflowException e) {}
    }

    @Test
    public void testGetDouble() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        buffer.append(double2bytes(6.11));

        assertEquals(8, buffer.remaining());
        assertTrue(buffer.hasRemaining());
        assertEquals(0, buffer.position());

        assertEquals(6.11, buffer.getDouble(), 0.1);

        assertEquals(0, buffer.remaining());
        assertFalse(buffer.hasRemaining());
        assertEquals(8, buffer.position());

        try {
            buffer.getDouble();
            fail("Should not be able to read past end");
        } catch (BufferUnderflowException e) {}
    }

    //----- Test hasArray method ---------------------------------------------//

    @Test
    public void testHasArray() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        byte[] data1 = new byte[] {0, 1, 2, 3, 4};
        byte[] data2 = new byte[] {5, 6, 7, 8, 9};
        buffer.append(data1);

        assertTrue(buffer.hasArray());
        assertNotNull(buffer.array());
        assertSame(data1, buffer.array());
        assertEquals(0, buffer.arrayOffset());

        buffer.append(data2);

        assertFalse(buffer.hasArray());
        try {
            buffer.array();
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
        }
        try {
            buffer.arrayOffset();
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
        }

        byte[] result1 = new byte[data1.length];
        byte[] result2 = new byte[data1.length];

        buffer.get(result1);
        assertArrayEquals(data1, result1);
        assertFalse(buffer.hasArray());

        buffer.compact();

        assertTrue(buffer.hasArray());
        assertNotNull(buffer.array());
        assertSame(data2, buffer.array());
        assertEquals(0, buffer.arrayOffset());

        buffer.get(result2);
        assertArrayEquals(data2, result2);
        assertTrue(buffer.hasArray());

        buffer.compact();
        assertFalse(buffer.hasArray());

        try {
            buffer.array();
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
        }
        try {
            buffer.arrayOffset();
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
        }
    }

    //----- Test appending data to the buffer --------------------------------//

    @Test
    public void testAppendOne() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        byte[] source = new byte[] { 0, 1, 2, 3 };

        buffer.append(source);

        assertEquals(4, buffer.remaining());
        assertTrue(buffer.hasRemaining());
        assertEquals(0, buffer.position());

        assertTrue(buffer.hasArray());
        assertSame(source, buffer.array());
        assertEquals(0, buffer.getArrays().size());

        assertEquals(-1, buffer.getCurrentIndex());
    }

    @Test
    public void testAppendMoreThanOne() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        byte[] source1 = new byte[] { 0, 1, 2, 3 };
        byte[] source2 = new byte[] { 4, 5, 6, 7 };

        buffer.append(source1);
        assertTrue(buffer.hasArray());
        assertSame(source1, buffer.array());
        assertEquals(-1, buffer.getCurrentIndex());

        buffer.append(source2);
        assertFalse(buffer.hasArray());
        assertEquals(2, buffer.getArrays().size());
        assertEquals(0, buffer.getCurrentIndex());

        byte[] source3 = new byte[] { 9, 10, 11, 12 };
        buffer.append(source3);
        assertFalse(buffer.hasArray());
        assertEquals(3, buffer.getArrays().size());
        assertEquals(0, buffer.getCurrentIndex());
    }

    @Test
    public void testAppendNull() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        try {
            buffer.append(null);
            fail("Should not be able to add a null array");
        } catch (IllegalArgumentException iae) {}
    }

    @Test
    public void testAppendEmpty() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        try {
            buffer.append(new byte[0]);
            fail("Should not be able to add a empty array");
        } catch (IllegalArgumentException iae) {}
    }

    //----- Test buffer compaction handling ----------------------------------//

    @Test
    public void testCompactEmptyBuffer() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        try {
            buffer.compact();
        } catch (Throwable t) {
            fail("Should not fail to compact empty buffer");
        }

        CompositeReadableBuffer slice = buffer.slice();

        try {
            slice.compact();
        } catch (Throwable t) {
            fail("Should not fail to compact empty slice");
        }
    }

    @Test
    public void testCompactSignleArrayBuffer() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        byte[] source = new byte[] { 0, 1, 2, 3 };

        buffer.append(source);

        assertEquals(4, buffer.remaining());
        assertTrue(buffer.hasRemaining());
        assertEquals(0, buffer.position());
        assertTrue(buffer.hasArray());
        assertSame(source, buffer.array());
        assertEquals(0, buffer.getArrays().size());

        // Should not have any affect on buffer that is not consumed
        buffer.compact();

        assertEquals(4, buffer.remaining());
        assertTrue(buffer.hasRemaining());
        assertEquals(0, buffer.position());
        assertTrue(buffer.hasArray());
        assertSame(source, buffer.array());

        // Should not have any affect on buffer that is not consumed
        buffer.position(1);
        buffer.compact();

        assertEquals(3, buffer.remaining());
        assertTrue(buffer.hasRemaining());
        assertEquals(1, buffer.position());
        assertTrue(buffer.hasArray());
        assertSame(source, buffer.array());

        // Should clear array from buffer as it is now consumed.
        buffer.position(source.length);
        buffer.compact();

        assertEquals(0, buffer.remaining());
        assertFalse(buffer.hasRemaining());
        assertEquals(0, buffer.position());
        assertFalse(buffer.hasArray());
    }

    @Test
    public void testCompact() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        buffer.append(new byte[] {0})
              .append(new byte[] {1})
              .append(new byte[] {2})
              .append(new byte[] {3})
              .append(new byte[] {4})
              .append(new byte[] {5})
              .append(new byte[] {6})
              .append(new byte[] {7})
              .append(new byte[] {8})
              .append(new byte[] {9});

        assertEquals(10, buffer.remaining());
        assertEquals(10, buffer.getArrays().size());
        assertFalse(buffer.hasArray());
        assertTrue(buffer.hasRemaining());
        assertEquals(0, buffer.position());

        for (int i = 0; i < 10; ++i) {
            assertEquals(i, buffer.get());
            buffer.compact();
            assertEquals(0, buffer.position());
        }

        assertTrue(buffer.getArrays().isEmpty());
        assertFalse(buffer.hasArray());
        assertEquals(0, buffer.remaining());
        assertEquals(0, buffer.position());
        assertEquals(0, buffer.getArrays().size());

        try {
            buffer.get();
            fail("Should not be able to read past end");
        } catch (BufferUnderflowException e) {}
    }

    @Test
    public void testCompactWithDifferingBufferSizes() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        buffer.append(new byte[] {0})
              .append(new byte[] {1, 2})
              .append(new byte[] {3, 4, 5})
              .append(new byte[] {6})
              .append(new byte[] {7})
              .append(new byte[] {8, 9});

        assertEquals(10, buffer.remaining());
        assertFalse(buffer.hasArray());
        assertTrue(buffer.hasRemaining());
        assertEquals(0, buffer.position());

        for (int i = 0; i < 10; ++i) {
            assertEquals(i, buffer.get());
            buffer.compact();
        }

        assertTrue(buffer.getArrays().isEmpty());
        assertFalse(buffer.hasArray());
        assertEquals(0, buffer.remaining());
        assertEquals(0, buffer.position());

        try {
            buffer.get();
            fail("Should not be able to read past end");
        } catch (BufferUnderflowException e) {}
    }

    @Test
    public void testCompactUpdatesMark() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        byte[] source1 = new byte[] { 0, 1, 2, 3 };
        byte[] source2 = new byte[] { 4, 5, 6, 7 };

        buffer.append(source1).append(source2);

        assertFalse(buffer.hasArray());
        assertEquals(2, buffer.getArrays().size());
        assertEquals(0, buffer.getCurrentIndex());

        buffer.position(5);
        buffer.mark();
        assertEquals(5, buffer.get());
        buffer.position(8);
        buffer.reset();
        assertEquals(5, buffer.position());
        buffer.mark();

        buffer.compact();
        buffer.position(buffer.limit());
        buffer.reset();
        assertEquals(5, buffer.get());

        assertFalse(buffer.getArrays().isEmpty());
    }

    @Test
    public void testCompactThreeArraysToNone() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        byte[] source1 = new byte[] { 0, 1, 2, 3 };
        byte[] source2 = new byte[] { 4, 5, 6, 7 };
        byte[] source3 = new byte[] { 8, 9, 10, 11 };

        buffer.append(source1).append(source2).append(source3);

        assertFalse(buffer.hasArray());
        assertEquals(3, buffer.getArrays().size());
        assertEquals(0, buffer.getCurrentIndex());
        assertEquals(12, buffer.limit());
        assertEquals(12, buffer.capacity());

        buffer.position(4);
        buffer.compact();

        assertFalse(buffer.hasArray());
        assertEquals(2, buffer.getArrays().size());
        assertEquals(0, buffer.getCurrentIndex());
        assertEquals(8, buffer.limit());
        assertEquals(8, buffer.capacity());

        buffer.position(4);
        buffer.compact();

        // TODO - Right now we hold off purging the array list until everything is consumed.
        assertTrue(buffer.hasArray());
        assertEquals(1, buffer.getArrays().size());
        assertEquals(0, buffer.getCurrentIndex());
        assertEquals(4, buffer.limit());
        assertEquals(4, buffer.capacity());

        buffer.position(4);
        buffer.compact();

        assertFalse(buffer.hasArray());
        assertEquals(0, buffer.getArrays().size());
        assertEquals(-1, buffer.getCurrentIndex());
        assertEquals(0, buffer.limit());
        assertEquals(0, buffer.capacity());
    }

    @Test
    public void testCompactAllBuffersInOneShot() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        byte[] source1 = new byte[] { 0, 1, 2, 3 };
        byte[] source2 = new byte[] { 4, 5, 6, 7 };
        byte[] source3 = new byte[] { 8, 9, 10, 11 };
        byte[] source4 = new byte[] { 12, 13, 14, 15 };

        int size = source1.length + source2.length + source3.length + source4.length;

        buffer.append(source1).append(source2).append(source3).append(source4);

        assertFalse(buffer.hasArray());
        assertEquals(4, buffer.getArrays().size());
        assertEquals(0, buffer.getCurrentIndex());
        assertEquals(size, buffer.limit());
        assertEquals(size, buffer.capacity());

        buffer.position(buffer.limit());
        buffer.compact();

        assertFalse(buffer.hasArray());
        assertEquals(0, buffer.getArrays().size());
        assertEquals(-1, buffer.getCurrentIndex());
        assertEquals(0, buffer.limit());
        assertEquals(0, buffer.capacity());
    }

    @Test
    public void testAppendAfterCompact() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        byte[] source1 = new byte[] { 0, 1, 2, 3 };
        byte[] source2 = new byte[] { 4, 5, 6, 7 };

        buffer.append(source1).append(source2);

        assertFalse(buffer.hasArray());
        assertEquals(2, buffer.getArrays().size());
        assertEquals(0, buffer.getCurrentIndex());

        buffer.position(4);
        buffer.compact();

        assertEquals(0, buffer.position());
        assertEquals(4, buffer.limit());
        assertEquals(4, buffer.capacity());
        assertEquals(1, buffer.getArrays().size());

        byte[] source3 = new byte[] { 8, 9, 10, 11 };

        buffer.append(source3);

        buffer.position(4);

        for (int i = 0; i < source3.length; ++i) {
            assertEquals(source3[i], buffer.get());
        }

        assertFalse(buffer.getArrays().isEmpty());
        buffer.compact();
        assertTrue(buffer.getArrays().isEmpty());
    }

    //----- Tests on Mark ----------------------------------------------------//

    @Test
    public void testMark() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        buffer.append(new byte[] {0, 1, 2, 3, 4}).append(new byte[] {5, 6, 7, 8, 9});

        // save state
        int oldPosition = buffer.position();
        int oldLimit = buffer.limit();

        assertEquals(0, oldPosition);
        assertEquals(10, oldLimit);

        CompositeReadableBuffer self = buffer.mark();
        assertSame(self, buffer);

        buffer.mark();
        buffer.position(buffer.limit());
        buffer.reset();
        assertEquals(buffer.position(), oldPosition);

        buffer.mark();
        buffer.position(buffer.limit());
        buffer.reset();
        assertEquals(buffer.position(), oldPosition);

        // restore state
        buffer.limit(oldLimit);
        buffer.position(oldPosition);
    }

    //----- Tests on Reset ---------------------------------------------------//

    @Test
    public void testReset() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        buffer.append(new byte[] {0, 1, 2, 3, 4}).append(new byte[] {5, 6, 7, 8, 9});

        try {
            buffer.reset();
            fail("Should throw InvalidMarkException when not marked.");
        } catch (InvalidMarkException e) {
        }

        // save state
        int oldPosition = buffer.position();
        int oldLimit = buffer.limit();

        assertEquals(0, oldPosition);
        assertEquals(10, oldLimit);

        buffer.mark();
        buffer.position(buffer.limit());
        buffer.reset();
        assertEquals(buffer.position(), oldPosition);

        buffer.mark();
        buffer.position(buffer.limit());
        buffer.reset();
        assertEquals(buffer.position(), oldPosition);

        // Can call as mark is not cleared on reset.
        CompositeReadableBuffer self = buffer.reset();
        assertSame(self, buffer);
    }

    //----- Tests on Rewind --------------------------------------------------//

    @Test
    public void testRewind() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        buffer.append(new byte[] {0, 1, 2, 3, 4}).append(new byte[] {5, 6, 7, 8, 9});

        // save state
        int oldPosition = buffer.position();
        int oldLimit = buffer.limit();

        assertEquals(0, oldPosition);
        assertEquals(10, oldLimit);

        CompositeReadableBuffer self = buffer.rewind();
        assertEquals(buffer.position(), 0);
        assertSame(self, buffer);

        try {
            buffer.reset();
            fail("Should throw InvalidMarkException");
        } catch (InvalidMarkException e) {
        }
    }

    //----- Tests on Flip ----------------------------------------------------//

    @Test
    public void testFlipWhenEmpty() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();
        CompositeReadableBuffer ret = buffer.flip();

        assertSame(ret, buffer);
        assertEquals(buffer.position(), 0);
        assertEquals(buffer.limit(), 0);
    }

    @Test
    public void testFlipWithOneArray() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        buffer.append(new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9});

        // save state
        int oldPosition = buffer.position();
        int oldLimit = buffer.limit();

        assertEquals(0, oldPosition);
        assertEquals(10, oldLimit);

        CompositeReadableBuffer ret = buffer.flip();
        assertSame(ret, buffer);
        assertEquals(buffer.position(), 0);
        assertEquals(buffer.limit(), oldPosition);

        try {
            buffer.reset();
            fail("Should throw InvalidMarkException");
        } catch (InvalidMarkException e) {
        }
    }

    @Test
    public void testFlipWithMultipleArrays() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        buffer.append(new byte[] {0, 1, 2, 3, 4}).append(new byte[] {5, 6, 7, 8, 9});

        // save state
        int oldPosition = buffer.position();
        int oldLimit = buffer.limit();

        assertEquals(0, oldPosition);
        assertEquals(10, oldLimit);

        CompositeReadableBuffer ret = buffer.flip();
        assertSame(ret, buffer);
        assertEquals(buffer.position(), 0);
        assertEquals(buffer.limit(), oldPosition);

        try {
            buffer.reset();
            fail("Should throw InvalidMarkException");
        } catch (InvalidMarkException e) {
        }
    }

    @Test
    public void testFlipSliceWithOneArray() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();
        buffer.append(new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9});

        buffer.mark();
        buffer.position(1);
        buffer.limit(buffer.remaining() - 1);

        ReadableBuffer slice = buffer.slice();

        assertEquals(1, slice.get(0));
        slice.position(1);
        slice.mark();
        slice.flip();

        assertEquals(1, slice.get(0));
        assertEquals(1, slice.limit());

        try {
            slice.reset();
            fail("Should throw InvalidMarkException");
        } catch (InvalidMarkException e) {
        }

        buffer.reset();
        assertEquals(0, buffer.position());
        assertEquals(buffer.remaining(), buffer.limit());
    }

    @Test
    public void testFlipSliceWithMultipleArrays() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();
        buffer.append(new byte[] {0, 1, 2, 3, 4}).append(new byte[] {5, 6, 7, 8, 9});

        buffer.mark();
        buffer.position(5);

        ReadableBuffer slice = buffer.slice();

        assertEquals(5, slice.get(0));
        slice.position(1);
        slice.mark();
        slice.flip();

        assertEquals(5, slice.get(0));
        assertEquals(1, slice.limit());

        try {
            slice.reset();
            fail("Should throw InvalidMarkException");
        } catch (InvalidMarkException e) {
        }

        buffer.reset();
        assertEquals(0, buffer.position());
        assertEquals(buffer.remaining(), buffer.limit());
    }

    //----- Tests on Clear --------------------------------------------------//

    @Test
    public void testClear() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        buffer.append(new byte[] {0, 1, 2, 3, 4}).append(new byte[] {5, 6, 7, 8, 9});

        assertEquals(0, buffer.position());
        assertEquals(10, buffer.limit());

        buffer.position(5);
        assertEquals(5, buffer.position());
        buffer.mark();

        CompositeReadableBuffer self = buffer.clear();
        assertEquals(0, buffer.position());
        assertEquals(10, buffer.limit());
        assertSame(self, buffer);

        try {
            buffer.reset();
            fail("Should throw InvalidMarkException");
        } catch (InvalidMarkException e) {
        }
    }

    //----- Test various cases of Duplicate ----------------------------------//

    @Test
    public void testDuplicateWithSingleArrayContent() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        buffer.append(new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
        buffer.mark();
        buffer.position(buffer.limit());

        // duplicate's contents should be the same as buffer
        CompositeReadableBuffer duplicate = buffer.duplicate();
        assertNotSame(buffer, duplicate);
        assertEquals(buffer.capacity(), duplicate.capacity());
        assertEquals(buffer.position(), duplicate.position());
        assertEquals(buffer.limit(), duplicate.limit());
        assertContentEquals(buffer, duplicate);

        // duplicate's position, mark, and limit should be independent to buffer
        duplicate.reset();
        assertEquals(duplicate.position(), 0);
        duplicate.clear();
        assertEquals(buffer.position(), buffer.limit());
        buffer.reset();
        assertEquals(buffer.position(), 0);

        // One array buffer should share backing array
        assertTrue(buffer.hasArray());
        assertTrue(duplicate.hasArray());
        assertSame(buffer.array(), duplicate.array());
        assertTrue(buffer.getArrays().isEmpty());
        assertTrue(duplicate.getArrays().isEmpty());
        assertSame(buffer.getArrays(), duplicate.getArrays());  // Same Empty Buffer
    }

    @Test
    public void testDuplicateWithSingleArrayContentCompactionIsNoOpWhenNotRead() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();
        buffer.append(new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9});

        CompositeReadableBuffer duplicate = buffer.duplicate();

        assertEquals(10, buffer.capacity());
        assertEquals(buffer.capacity(), duplicate.capacity());

        buffer.compact();
        assertEquals(10, buffer.capacity());
        assertEquals(buffer.capacity(), duplicate.capacity());

        duplicate.compact();
        assertEquals(10, buffer.capacity());
        assertEquals(buffer.capacity(), duplicate.capacity());
    }

    @Test
    public void testDuplicateWithSingleArrayContentCompactionLeavesOtherIntact() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();
        buffer.append(new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9});

        CompositeReadableBuffer duplicate = buffer.duplicate();

        assertEquals(10, buffer.capacity());
        assertEquals(buffer.capacity(), duplicate.capacity());

        duplicate.position(duplicate.limit());
        assertFalse(duplicate.hasRemaining());

        assertTrue(duplicate.hasArray());
        duplicate.compact();
        assertFalse(duplicate.hasArray());
        assertEquals(0, duplicate.capacity());

        // Buffer should be unaffected
        assertEquals(10, buffer.capacity());
        assertTrue(buffer.hasArray());
    }

    @Test
    public void testDuplicateWithMulitiArrayContent() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        buffer.append(new byte[] {0, 1, 2, 3, 4}).append(new byte[] {5, 6, 7, 8, 9});
        buffer.mark();
        buffer.position(buffer.limit());

        // duplicate's contents should be the same as buffer
        CompositeReadableBuffer duplicate = buffer.duplicate();
        assertNotSame(buffer, duplicate);
        assertEquals(buffer.capacity(), duplicate.capacity());
        assertEquals(buffer.position(), duplicate.position());
        assertEquals(buffer.limit(), duplicate.limit());
        assertContentEquals(buffer, duplicate);

        // duplicate's position, mark, and limit should be independent to buffer
        duplicate.reset();
        assertEquals(duplicate.position(), 0);
        duplicate.clear();
        assertEquals(buffer.position(), buffer.limit());
        buffer.reset();
        assertEquals(buffer.position(), 0);

        // One array buffer should share backing array
        assertFalse(buffer.hasArray());
        assertFalse(duplicate.hasArray());
        assertFalse(buffer.getArrays().isEmpty());
        assertFalse(duplicate.getArrays().isEmpty());
        assertNotSame(buffer.getArrays(), duplicate.getArrays());
    }

    @Test
    public void testDuplicateWithMultiArrayContentCompactionLeavesOtherIntact() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();
        buffer.append(new byte[] {0, 1, 2, 3, 4}).append(new byte[] {5, 6, 7, 8, 9});

        CompositeReadableBuffer duplicate = buffer.duplicate();

        assertEquals(10, buffer.capacity());
        assertEquals(buffer.capacity(), duplicate.capacity());

        duplicate.position(duplicate.limit());
        assertFalse(duplicate.hasRemaining());

        assertFalse(duplicate.hasArray());
        duplicate.compact();
        assertFalse(duplicate.hasArray());
        assertEquals(0, duplicate.capacity());
        assertEquals(0, duplicate.getArrays().size());

        // Buffer should be unaffected
        assertEquals(10, buffer.capacity());
        assertFalse(buffer.hasArray());
        assertEquals(2, buffer.getArrays().size());
    }

    //----- Test various cases of Slice --------------------------------------//

    @Test
    public void testSlice() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        byte[] data = new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        buffer.append(data);

        assertEquals(buffer.capacity(), data.length);
        buffer.position(1);
        buffer.limit(buffer.capacity() - 2);

        ReadableBuffer slice = buffer.slice();
        assertEquals(slice.position(), 0);
        assertEquals(slice.limit(), buffer.remaining());
        assertEquals(slice.capacity(), buffer.remaining());
        assertEquals(1, slice.get());

        try {
            slice.reset();
            fail("Should throw InvalidMarkException");
        } catch (InvalidMarkException e) {
        }
    }

    @Test
    public void testSliceOnEmptyBuffer() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();
        CompositeReadableBuffer slice = buffer.slice();

        // Sliced contents should be the same as buffer
        assertNotSame(buffer, slice);
        assertEquals(buffer.capacity(), slice.capacity());
        assertEquals(buffer.position(), slice.position());
        assertEquals(buffer.limit(), slice.limit());
        assertContentEquals(buffer, slice);

        try {
            slice.compact();
        } catch (Throwable t) {
            fail("Compacting an empty slice should not fail");
        }
    }

    @Test
    public void testSliceIgnoresAppends() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();
        buffer.append(new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9});

        // Sliced contents should be the same as buffer
        CompositeReadableBuffer slice = buffer.slice();
        assertNotSame(buffer, slice);

        try {
            slice.append(new byte[] { 10 });
            fail("Should not be allowed to append to a slice, must throw ReadOnlyBufferException");
        } catch (IllegalStateException ise) {}
    }

    @Test
    public void testSliceWithSingleArrayContent() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        buffer.append(new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
        buffer.mark();

        // Sliced contents should be the same as buffer
        CompositeReadableBuffer slice = buffer.slice();
        assertNotSame(buffer, slice);
        assertEquals(buffer.capacity(), slice.capacity());
        assertEquals(buffer.position(), slice.position());
        assertEquals(buffer.limit(), slice.limit());
        assertContentEquals(buffer, slice);

        // Sliced position, mark, and limit should be independent to buffer
        try {
            slice.reset();
            fail("Mark should be undefined in the slice and throw InvalidMarkException");
        } catch (InvalidMarkException e) {}

        assertEquals(slice.position(), 0);
        slice.clear();
        assertEquals(10, buffer.limit());
        buffer.reset();
        assertEquals(buffer.position(), 0);

        // One array buffer should share backing array
        assertTrue(buffer.hasArray());
        assertTrue(slice.hasArray());
        assertSame(buffer.array(), slice.array());
        assertTrue(buffer.getArrays().isEmpty());
        assertTrue(slice.getArrays().isEmpty());
        assertSame(buffer.getArrays(), slice.getArrays());  // Same Empty Buffer
    }

    @Test
    public void testSliceWithSingleArrayContentSourcePartiallyRead() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        buffer.append(new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
        buffer.position(5);

        // Sliced contents should be the same as buffer
        CompositeReadableBuffer slice = buffer.slice();
        assertNotSame(buffer, slice);
        assertEquals(10, buffer.capacity());
        assertEquals(5, slice.capacity());
        assertEquals(5, buffer.position());
        assertEquals(0, slice.position());
        assertEquals(10, buffer.limit());
        assertEquals(5, slice.limit());
        assertSpanEquals(buffer, slice);

        slice.position(1);
        assertEquals(6, slice.get());
        assertEquals(5, slice.get(0));

        try {
            slice.limit(6);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException iae) {}

        try {
            slice.position(6);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException iae) {}
    }

    @Test
    public void testSliceWithMulitpleArraysContentSourcePartiallyRead() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        buffer.append(new byte[] {0, 1, 2, 3, 4}).append(new byte[] {5, 6, 7, 8, 9});
        buffer.position(5);

        // Sliced contents should be the same as buffer
        CompositeReadableBuffer slice = buffer.slice();
        assertNotSame(buffer, slice);
        assertEquals(10, buffer.capacity());
        assertEquals(5, slice.capacity());
        assertEquals(5, buffer.position());
        assertEquals(0, slice.position());
        assertEquals(10, buffer.limit());
        assertEquals(5, slice.limit());
        assertSpanEquals(buffer, slice);

        slice.position(1);
        assertEquals(6, slice.get());
        assertEquals(5, slice.get(0));

        try {
            slice.limit(6);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException iae) {}

        try {
            slice.position(6);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException iae) {}
    }

    //----- Test various cases of byteBuffer ---------------------------------//

    @Test
    public void testByteBufferFromEmptyBuffer() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        ByteBuffer byteBuffer = buffer.byteBuffer();

        assertNotNull(byteBuffer);
        assertEquals(0, byteBuffer.position());
        assertEquals(0, byteBuffer.limit());
        assertEquals(0, byteBuffer.capacity());

        // Our ByteBuffer results should be read-only and indicate no array.
        assertTrue(byteBuffer.isReadOnly());
        assertFalse(byteBuffer.hasArray());
    }

    @Test
    public void testByteBufferOnSingleArrayContent() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        buffer.append(new byte[] {9, 8, 7, 6, 5, 4, 3, 2, 1, 0});

        ByteBuffer byteBuffer = buffer.byteBuffer();

        assertNotNull(byteBuffer);
        assertEquals(0, byteBuffer.position());
        assertEquals(10, byteBuffer.limit());
        assertEquals(10, byteBuffer.capacity());

        // Our ByteBuffer results should be read-only and indicate no array.
        assertTrue(byteBuffer.isReadOnly());
        assertFalse(byteBuffer.hasArray());

        for (int i = 0; i < 10; ++i) {
            assertEquals(buffer.get(i), byteBuffer.get(i));
        }
    }

    @Test
    public void testByteBufferOnMultipleArrayContent() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        buffer.append(new byte[] {9, 8, 7, 6, 5}).append(new byte[] {4, 3, 2, 1, 0});

        ByteBuffer byteBuffer = buffer.byteBuffer();

        assertNotNull(byteBuffer);
        assertEquals(0, byteBuffer.position());
        assertEquals(10, byteBuffer.limit());
        assertEquals(10, byteBuffer.capacity());

        // Our ByteBuffer results should be read-only and indicate no array.
        assertTrue(byteBuffer.isReadOnly());
        assertFalse(byteBuffer.hasArray());

        for (int i = 0; i < 10; ++i) {
            assertEquals(buffer.get(i), byteBuffer.get(i));
        }
    }

    @Test
    public void testByteBufferOnMultipleArrayContentWithLimits() {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        buffer.append(new byte[] {9, 8, 7, 6, 5}).append(new byte[] {4, 3, 2, 1, 0});

        buffer.position(3);
        buffer.limit(9);

        assertEquals(6, buffer.remaining());

        ByteBuffer byteBuffer = buffer.byteBuffer();

        assertNotNull(byteBuffer);
        assertEquals(0, byteBuffer.position());
        assertEquals(6, byteBuffer.limit());
        assertEquals(6, byteBuffer.capacity());

        // Our ByteBuffer results should be read-only and indicate no array.
        assertTrue(byteBuffer.isReadOnly());
        assertFalse(byteBuffer.hasArray());

        for (int i = 0; i < 6; ++i) {
            assertEquals(buffer.get(), byteBuffer.get());
        }
    }

    //----- Test put ReadableBuffer ------------------------------------------//

    @Test
    public void testReadStringFromEmptyBuffer() throws CharacterCodingException {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        assertNull(buffer.readString(StandardCharsets.UTF_8.newDecoder()));
    }

    @Test
    public void testReadStringFromUTF8InSingleArray() throws CharacterCodingException {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        final String testString = "Test String to Decode!";
        byte[] encoded = testString.getBytes(StandardCharsets.UTF_8);

        buffer.append(encoded);

        assertEquals(testString, buffer.readString(StandardCharsets.UTF_8.newDecoder()));
    }

    @Test
    public void testReadStringFromUTF8InSingleArrayWithLimits() throws CharacterCodingException {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        final String testString = "Test String to Decode!";
        byte[] encoded = testString.getBytes(StandardCharsets.UTF_8);

        // Only read the first character
        buffer.append(encoded);
        buffer.limit(1);

        assertEquals("T", buffer.readString(StandardCharsets.UTF_8.newDecoder()));
    }

    @Test
    public void testReadStringFromUTF8InMulitpleArrays() throws CharacterCodingException {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        final String testString = "Test String to Decode!!";
        byte[] encoded = testString.getBytes(StandardCharsets.UTF_8);

        byte[] first = new byte[encoded.length / 2];
        byte[] second = new byte[encoded.length - (encoded.length / 2)];

        System.arraycopy(encoded, 0, first, 0, first.length);
        System.arraycopy(encoded, first.length, second, 0, second.length);

        buffer.append(first).append(second);

        String result = buffer.readString(StandardCharsets.UTF_8.newDecoder());

        assertEquals(testString, result);
    }

    @Test
    public void testReadStringFromUTF8InMultipleArraysWithLimits() throws CharacterCodingException {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        final String testString = "Test String to Decode!";
        byte[] encoded = testString.getBytes(StandardCharsets.UTF_8);

        byte[] first = new byte[encoded.length / 2];
        byte[] second = new byte[encoded.length - (encoded.length / 2)];

        System.arraycopy(encoded, 0, first, 0, first.length);
        System.arraycopy(encoded, first.length, second, 0, second.length);

        buffer.append(first).append(second);

        // Only read the first character
        buffer.limit(1);

        assertEquals("T", buffer.readString(StandardCharsets.UTF_8.newDecoder()));
    }

    //----- Tests for hashCode -----------------------------------------------//

    @Test
    public void testHashCodeNotFromIdentity() throws CharacterCodingException {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        assertEquals(1, buffer.hashCode());

        byte[] data = new byte[] {9, 8, 7, 6, 5, 4, 3, 2, 1, 0};

        buffer.append(data);

        assertTrue(buffer.hashCode() != 1);
        assertNotEquals(buffer.hashCode(), System.identityHashCode(buffer));
        assertEquals(buffer.hashCode(), buffer.hashCode());
    }

    @Test
    public void testHashCodeOnSameBackingBuffer() throws CharacterCodingException {
        CompositeReadableBuffer buffer1 = new CompositeReadableBuffer();
        CompositeReadableBuffer buffer2 = new CompositeReadableBuffer();
        CompositeReadableBuffer buffer3 = new CompositeReadableBuffer();

        byte[] data = new byte[] {9, 8, 7, 6, 5, 4, 3, 2, 1, 0};

        buffer1.append(data);
        buffer2.append(data);
        buffer3.append(data);

        assertEquals(buffer1.hashCode(), buffer2.hashCode());
        assertEquals(buffer2.hashCode(), buffer3.hashCode());
        assertEquals(buffer3.hashCode(), buffer1.hashCode());
    }

    @Test
    public void testHashCodeOnDifferentBackingBuffer() throws CharacterCodingException {
        CompositeReadableBuffer buffer1 = new CompositeReadableBuffer();
        CompositeReadableBuffer buffer2 = new CompositeReadableBuffer();

        byte[] data1 = new byte[] {9, 8, 7, 6, 5, 4, 3, 2, 1, 0};
        byte[] data2 = new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

        buffer1.append(data1);
        buffer2.append(data2);

        assertNotEquals(buffer1.hashCode(), buffer2.hashCode());
    }

    @Test
    public void testHashCodeOnSplitBufferContentsNotSame() throws CharacterCodingException {
        CompositeReadableBuffer buffer1 = new CompositeReadableBuffer();
        CompositeReadableBuffer buffer2 = new CompositeReadableBuffer();

        byte[] data1 = new byte[] {9, 8, 7, 6, 5, 4, 3, 2, 1, 0};
        byte[] data2 = new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

        buffer1.append(data1).append(data2);
        buffer2.append(data2).append(data1);

        assertNotEquals(buffer1.hashCode(), buffer2.hashCode());
    }

    @Test
    public void testHashCodeOnSplitBufferContentsSame() throws CharacterCodingException {
        CompositeReadableBuffer buffer1 = new CompositeReadableBuffer();
        CompositeReadableBuffer buffer2 = new CompositeReadableBuffer();

        byte[] data1 = new byte[] {9, 8, 7, 6, 5, 4, 3, 2, 1, 0};
        byte[] data2 = new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

        buffer1.append(data1).append(data2);
        buffer2.append(data1).append(data2);

        assertEquals(buffer1.hashCode(), buffer2.hashCode());
    }

    @Test
    public void testHashCodeMatchesByteBufferSingleArrayContents() throws CharacterCodingException {
        byte[] data = new byte[] {9, 8, 7, 6, 5, 4, 3, 2, 1, 0};

        CompositeReadableBuffer buffer1 = new CompositeReadableBuffer();
        buffer1.append(data);

        ByteBuffer buffer2 = ByteBuffer.wrap(data);

        assertEquals(buffer1.hashCode(), buffer2.hashCode());
    }

    @Test
    public void testHashCodeMatchesByteBufferSingleArrayContentsWithSlice() throws CharacterCodingException {
        byte[] data = new byte[] {9, 8, 7, 6, 5, 4, 3, 2, 1, 0};

        CompositeReadableBuffer buffer1 = new CompositeReadableBuffer();
        buffer1.append(data);

        ByteBuffer buffer2 = ByteBuffer.wrap(data);

        ReadableBuffer slice1 = buffer1.position(1).slice();
        ByteBuffer slice2 = ((ByteBuffer) buffer2.position(1)).slice();

        assertEquals(slice1.hashCode(), slice2.hashCode());
    }

    @Test
    public void testHashCodeMatchesByteBufferMultipleArrayContents() throws CharacterCodingException {
        byte[] data = new byte[] {9, 8, 7, 6, 5, 4, 3, 2, 1, 0};

        byte[] data1 = new byte[] {9, 8, 7, 6, 5};
        byte[] data2 = new byte[] {4, 3, 2, 1, 0};

        CompositeReadableBuffer buffer1 = new CompositeReadableBuffer();
        buffer1.append(data1);
        buffer1.append(data2);

        ByteBuffer buffer2 = ByteBuffer.wrap(data);

        assertEquals(buffer1.hashCode(), buffer2.hashCode());
    }

    @Test
    public void testHashCodeMatchesByteBufferMultipleArrayContentsWithSlice() throws CharacterCodingException {
        byte[] data = new byte[] {9, 8, 7, 6, 5, 4, 3, 2, 1, 0};

        byte[] data1 = new byte[] {9, 8, 7, 6, 5};
        byte[] data2 = new byte[] {4, 3, 2, 1, 0};

        CompositeReadableBuffer buffer1 = new CompositeReadableBuffer();
        buffer1.append(data1);
        buffer1.append(data2);

        ByteBuffer buffer2 = ByteBuffer.wrap(data);

        ReadableBuffer slice1 = buffer1.position(1).limit(4).slice();
        ByteBuffer slice2 = ((ByteBuffer) buffer2.position(1).limit(4)).slice();

        assertEquals(slice1.hashCode(), slice2.hashCode());
    }

    //----- Tests for equals -------------------------------------------------//

    @Test
    public void testEqualsSelf() throws CharacterCodingException {
        CompositeReadableBuffer buffer = new CompositeReadableBuffer();

        assertEquals(buffer, buffer);

        byte[] data = new byte[] {9, 8, 7, 6, 5, 4, 3, 2, 1, 0};

        buffer.append(data);

        assertEquals(buffer, buffer);
    }

    @Test
    public void testEqualsOnSameBackingBuffer() throws CharacterCodingException {
        CompositeReadableBuffer buffer1 = new CompositeReadableBuffer();
        CompositeReadableBuffer buffer2 = new CompositeReadableBuffer();
        CompositeReadableBuffer buffer3 = new CompositeReadableBuffer();

        byte[] data = new byte[] {9, 8, 7, 6, 5, 4, 3, 2, 1, 0};

        buffer1.append(data);
        buffer2.append(data);
        buffer3.append(data);

        assertEquals(buffer1, buffer2);
        assertEquals(buffer2, buffer3);
        assertEquals(buffer3, buffer1);
    }

    @Test
    public void testEqualsOnDifferentBackingBuffer() throws CharacterCodingException {
        CompositeReadableBuffer buffer1 = new CompositeReadableBuffer();
        CompositeReadableBuffer buffer2 = new CompositeReadableBuffer();

        byte[] data1 = new byte[] {9, 8, 7, 6, 5, 4, 3, 2, 1, 0};
        byte[] data2 = new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

        buffer1.append(data1);
        buffer2.append(data2);

        assertNotEquals(buffer1, buffer2);
    }

    @Test
    public void testEqualsWhenContentsInMultipleArraysNotSame() throws CharacterCodingException {
        CompositeReadableBuffer buffer1 = new CompositeReadableBuffer();
        CompositeReadableBuffer buffer2 = new CompositeReadableBuffer();

        byte[] data1 = new byte[] {9, 8, 7, 6, 5, 4, 3, 2, 1, 0};
        byte[] data2 = new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

        buffer1.append(data1).append(data2);
        buffer2.append(data2).append(data1);

        assertNotEquals(buffer1, buffer2);
    }

    @Test
    public void testEqualsWhenContentsInMultipleArraysSame() throws CharacterCodingException {
        CompositeReadableBuffer buffer1 = new CompositeReadableBuffer();
        CompositeReadableBuffer buffer2 = new CompositeReadableBuffer();

        byte[] data1 = new byte[] {9, 8, 7, 6, 5, 4, 3, 2, 1, 0};
        byte[] data2 = new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

        buffer1.append(data1).append(data2);
        buffer2.append(data1).append(data2);

        assertEquals(buffer1, buffer2);
    }

    //----- Utility Methods --------------------------------------------------//

    private void assertContentEquals(CompositeReadableBuffer buffer, byte array[], int offset, int length) {
        for (int i = 0; i < length; i++) {
            assertEquals(buffer.get(i), array[offset + i]);
        }
    }

    private void assertSpanEquals(ReadableBuffer source, ReadableBuffer other) {
        assertEquals(source.remaining(), other.remaining());
        for (int i = 0; i < source.remaining(); i++) {
            assertEquals(source.get(), other.get());
        }
    }

    private void assertContentEquals(ReadableBuffer source, ReadableBuffer other) {
        assertEquals(source.capacity(), other.capacity());
        for (int i = 0; i < source.capacity(); i++) {
            assertEquals(source.get(i), other.get(i));
        }
    }

    private byte[] int2bytes(int value) {
        byte bytes[] = new byte[Integer.BYTES];
        int index = 0;

        bytes[index++] = (byte) (value >>> 24);
        bytes[index++] = (byte) (value >>> 16);
        bytes[index++] = (byte) (value >>> 8);
        bytes[index++] = (byte) (value >>> 0);

        return bytes;
    }

    private byte[] long2bytes(long value) {
        byte bytes[] = new byte[Long.BYTES];
        int index = 0;

        bytes[index++] = (byte) (value >>> 56);
        bytes[index++] = (byte) (value >>> 48);
        bytes[index++] = (byte) (value >>> 40);
        bytes[index++] = (byte) (value >>> 32);
        bytes[index++] = (byte) (value >>> 24);
        bytes[index++] = (byte) (value >>> 16);
        bytes[index++] = (byte) (value >>> 8);
        bytes[index++] = (byte) (value >>> 0);

        return bytes;
    }

    private byte[] float2bytes(float value) {
        return int2bytes(Float.floatToRawIntBits(value));
    }

    private byte[] double2bytes(double value) {
        return long2bytes(Double.doubleToRawLongBits(value));
    }
}
