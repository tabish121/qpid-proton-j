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

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.InvalidMarkException;
import java.nio.ReadOnlyBufferException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ReadableBuffer implementation whose content is made up of one or more
 * byte arrays.
 */
public class CompositeReadableBuffer implements ReadableBuffer {

    private static final List<byte[]> EMPTY_LIST = Collections.unmodifiableList(new ArrayList<byte[]>());
    private static final ByteBuffer EMPTY_BUFFER = ByteBuffer.wrap(new byte[0]);
    private static final CompositeReadableBuffer EMPTY_SLICE = new CompositeReadableBuffer(true);
    private static int UNSET_MARK = -1;

    private ArrayList<byte[]> contents;

    // Track active array and our offset into it.
    private int currentArrayIndex = -1;
    private byte[] currentArray;
    private int currentOffset;

    // State global to the buffer.
    private int position;
    private int limit;
    private int capacity;
    private int mark = -1;
    private boolean readOnly;

    /**
     * Creates a default empty composite buffer
     */
    public CompositeReadableBuffer() {
    }

    private CompositeReadableBuffer(byte[] array, int offset) {
        this.currentArray = array;
        this.currentOffset = offset;
        this.capacity = array.length;
        this.limit = capacity;
    }

    private CompositeReadableBuffer(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public List<byte[]> getArrays() {
        return contents == null ? EMPTY_LIST : contents;
    }

    public int getCurrentIndex() {
        return currentArrayIndex;
    }

    @Override
    public boolean hasArray() {
        return currentArray != null && (contents == null || contents.size() == 1);
    }

    public int capacity() {
        return capacity;
    }

    @Override
    public byte[] array() {
        if (hasArray()) {
            return currentArray;
        }

        throw new UnsupportedOperationException("Buffer not backed by an array");
    }

    @Override
    public int arrayOffset() {
        if (hasArray()) {
            return currentOffset;
        }

        throw new UnsupportedOperationException("Buffer not backed by an array");
    }

    @Override
    public byte get() {
        if (remaining() == 0) {
            throw new BufferUnderflowException();
        }

        final byte result = currentArray[currentOffset++];
        position++;
        maybeMoveToNextArray();

        return result;
    }

    @Override
    public byte get(int index) {
        if (index < 0 || index >= limit) {
            throw new IndexOutOfBoundsException("The given index is not valid: " + index);
        }

        byte result = 0;

        if (index == position) {
            result = currentArray[currentOffset];
        } else if (index < position) {
            result = getBackwards(index);
        } else {
            result = getForward(index);
        }

        return result;
    }

    private byte getForward(int index) {
        byte result = 0;

        int currentArrayIndex = this.currentArrayIndex;
        int currentOffset = this.currentOffset;
        byte[] currentArray = this.currentArray;

        for (int amount = index - position; amount >= 0;) {
            if (amount < currentArray.length - currentOffset) {
                result = currentArray[currentOffset + amount];
                break;
            } else {
                amount -= currentArray.length - currentOffset;
                currentArray = contents.get(++currentArrayIndex);
                currentOffset = 0;
            }
        }

        return result;
    }

    private byte getBackwards(int index) {
        byte result = 0;

        int currentArrayIndex = this.currentArrayIndex;
        int currentOffset = this.currentOffset;
        byte[] currentArray = this.currentArray;

        for (int amount = position - index; amount >= 0;) {
            if ((currentOffset - amount) >= 0) {
                result = currentArray[currentOffset - amount];
                break;
            } else {
                amount -= currentOffset;
                currentArray = contents.get(--currentArrayIndex);
                currentOffset = currentArray.length;
            }
        }

        return result;
    }

    @Override
    public int getInt() {
        if (remaining() < Integer.BYTES) {
            throw new BufferUnderflowException();
        }

        int result = 0;

        for (int i = Integer.BYTES - 1; i >= 0; --i) {
            position++;
            result |= (int)(currentArray[currentOffset++] & 0xFF) << (i * Byte.SIZE);
            maybeMoveToNextArray();
        }

        return result;
    }

    @Override
    public long getLong() {
        if (remaining() < Long.BYTES) {
            throw new BufferUnderflowException();
        }

        long result = 0;

        for (int i = Long.BYTES - 1; i >= 0; --i) {
            position++;
            result |= (long)(currentArray[currentOffset++] & 0xFF) << (i * Byte.SIZE);
            maybeMoveToNextArray();
        }

        return result;
    }

    @Override
    public short getShort() {
        if (remaining() < Short.BYTES) {
            throw new BufferUnderflowException();
        }

        short result = 0;

        for (int i = Short.BYTES - 1; i >= 0; --i) {
            position++;
            result |= (currentArray[currentOffset++] & 0xFF) << (i * Byte.SIZE);
            maybeMoveToNextArray();
        }

        return result;
    }

    @Override
    public float getFloat() {
        return Float.intBitsToFloat(getInt());
    }

    @Override
    public double getDouble() {
        return Double.longBitsToDouble(getLong());
    }

    @Override
    public CompositeReadableBuffer get(byte[] data) {
        return get(data, 0, data.length);
    }

    @Override
    public CompositeReadableBuffer get(byte[] data, int offset, int length) {
        validateReadTarget(data.length, offset, length);

        if (length > remaining()) {
            throw new BufferUnderflowException();
        }

        for (int copied = 0; length > 0; ) {
            final int chunk = Math.min((currentArray.length - currentOffset), length);
            System.arraycopy(currentArray, currentOffset, data, offset + copied, chunk);

            currentOffset += chunk;
            position += chunk;
            length -= chunk;
            copied += chunk;

            maybeMoveToNextArray();
        }

        return this;
    }

    @Override
    public CompositeReadableBuffer get(WritableBuffer target) {
        int length = target.remaining();

        while (length > 0) {
            final int chunk = Math.min((currentArray.length - currentOffset), length);

            if (chunk == 0) {
                break;  // This buffer is out of data
            }

            target.put(currentArray, currentOffset, chunk);

            currentOffset += chunk;
            position += chunk;
            length -= chunk;

            maybeMoveToNextArray();
        }

        return this;
    }

    @Override
    public CompositeReadableBuffer position(int position) {
        if (position < 0 || position > limit) {
            throw new IllegalArgumentException("position must be non-negative and no greater than the limit");
        }

        // Either we are going backwards or forwards and in either case we should leave
        // the state in a valid read point or when going forwards the state should reflect
        // the fact that we are at the end.
        if (position < this.position) {
            for (int amount = this.position - position; amount > 0;) {
                if ((currentOffset - amount) >= 0) {
                    currentOffset -= amount;
                    break;
                } else {
                    amount -= currentOffset;
                    currentArray = contents.get(--currentArrayIndex);
                    currentOffset = currentArray.length;
                }
            }
        } else {
            for (int amount = position - this.position; amount > 0;) {
                if (amount < currentArray.length - currentOffset) {
                    currentOffset += amount;
                    break;
                } else {
                    amount -= currentArray.length - currentOffset;
                    if (currentArrayIndex != -1 && currentArrayIndex < contents.size() - 1) {
                        currentArray = contents.get(++currentArrayIndex);
                        currentOffset = 0;
                    } else {
                        currentOffset = currentArray.length;
                    }
                }
            }
        }

        this.position = position;

        if (mark > position) {
            mark = UNSET_MARK;
        }

        return this;
    }

    @Override
    public int position() {
        return position;
    }

    @Override
    public CompositeReadableBuffer slice() {
        int newCapacity = limit() - position();

        final CompositeReadableBuffer result;

        if (newCapacity == 0) {
            result = EMPTY_SLICE;
        } else {
            result = new CompositeReadableBuffer(currentArray, currentOffset);
            result.contents = contents;
            result.currentArrayIndex = currentArrayIndex;
            result.capacity = newCapacity;
            result.limit = newCapacity;
            result.position = 0;
            result.readOnly = true;
        }

        return result;
    }

    @Override
    public CompositeReadableBuffer flip() {
        limit = position;

        if (currentArrayIndex > 0) {
            currentArray = contents.get(0);
        }

        position = currentOffset = 0;
        mark = UNSET_MARK;

        return this;
    }

    @Override
    public CompositeReadableBuffer limit(int limit) {
        if (limit < 0 || limit > capacity) {
            throw new IllegalArgumentException("limit must be non-negative and no greater than the capacity");
        }

        if (mark > limit) {
            mark = UNSET_MARK;
        }

        if (position > limit) {
            position(limit);
        }

        this.limit = limit;

        return this;
    }

    @Override
    public int limit() {
        return limit;
    }

    @Override
    public CompositeReadableBuffer mark() {
        this.mark = position;
        return this;
    }

    @Override
    public CompositeReadableBuffer reset() {
        if (mark < 0) {
            throw new InvalidMarkException();
        }

        position(mark);

        return this;
    }

    @Override
    public CompositeReadableBuffer rewind() {
        return position(0);
    }

    @Override
    public CompositeReadableBuffer clear() {
        mark = UNSET_MARK;
        limit = capacity;

        return position(0);
    }

    @Override
    public int remaining() {
        return limit - position;
    }

    @Override
    public boolean hasRemaining() {
        return remaining() > 0;
    }

    @Override
    public CompositeReadableBuffer duplicate() {
        CompositeReadableBuffer duplicated =
            new CompositeReadableBuffer(currentArray, currentOffset);

        if (contents != null) {
            duplicated.contents = new ArrayList<>(contents);
        }

        duplicated.capacity = capacity;
        duplicated.currentArrayIndex = currentArrayIndex;
        duplicated.limit = limit;
        duplicated.position = position;
        duplicated.mark = mark;
        duplicated.readOnly = readOnly;   // A slice duplicated should not allow compaction.

        return duplicated;
    }

    @Override
    public ByteBuffer byteBuffer() {
        int viewSpan = limit() - position();

        final ByteBuffer result;

        if (viewSpan == 0) {
            result = EMPTY_BUFFER;
        } else if (viewSpan <= currentArray.length - currentOffset) {
            result = ByteBuffer.wrap(currentArray, currentOffset, viewSpan).asReadOnlyBuffer();
        } else {
            result = buildByteBuffer(viewSpan);
        }

        return result.asReadOnlyBuffer();
    }

    private ByteBuffer buildByteBuffer(int span) {
        byte[] compactedView = new byte[span];
        int arrayIndex = currentArrayIndex;

        // Take whatever is left from the current array;
        System.arraycopy(currentArray, currentOffset, compactedView, 0, currentArray.length - currentOffset);
        int copied = currentArray.length - currentOffset;

        while (copied < span) {
            byte[] next = contents.get(++arrayIndex);
            final int length = Math.min(span - copied, next.length);
            System.arraycopy(next, 0, compactedView, copied, length);
            copied += length;
        }

        return ByteBuffer.wrap(compactedView);
    }

    @Override
    public String readUTF8() throws CharacterCodingException {
        return readString(StandardCharsets.UTF_8.newDecoder());
    }

    @Override
    public String readString(CharsetDecoder decoder) throws CharacterCodingException {
        if (!hasRemaining()) {
            return null;
        }

        CharBuffer decoded = null;

        if (hasArray()) {
            decoded = decoder.decode(ByteBuffer.wrap(currentArray, currentOffset, remaining()));
        } else {
            decoded = readStringFromComponents(decoder);
        }

        return decoded.toString();
    }

    private CharBuffer readStringFromComponents(CharsetDecoder decoder) throws CharacterCodingException {
        int size = (int)(remaining() * decoder.averageCharsPerByte());
        CharBuffer decoded = CharBuffer.allocate(size);

        int arrayIndex = currentArrayIndex;
        final int viewSpan = limit() - position();
        int processed = Math.min(currentArray.length - currentOffset, viewSpan);
        ByteBuffer wrapper = ByteBuffer.wrap(currentArray, currentOffset, processed);

        CoderResult step = CoderResult.OVERFLOW;

        do {
            boolean endOfInput = processed == viewSpan;
            step = decoder.decode(wrapper, decoded, processed == viewSpan);
            if (step.isUnderflow() && endOfInput) {
                step = decoder.flush(decoded);
                break;
            }

            if (step.isOverflow()) {
                size = 2 * size + 1;
                CharBuffer upsized = CharBuffer.allocate(size);
                decoded.flip();
                upsized.put(decoded);
                decoded = upsized;
                continue;
            }

            byte[] next = contents.get(++arrayIndex);
            int wrapSize = Math.min(next.length, viewSpan - processed);
            wrapper = ByteBuffer.wrap(next, 0, wrapSize);
            processed += wrapSize;
        } while (!step.isError());

        if (step.isError()) {
            step.throwException();
        }

        return (CharBuffer) decoded.flip();
    }

    @Override
    public void put(ReadableBuffer other) {
        if (!other.hasRemaining()) {
            return;
        }

        if (readOnly) {
            throw new ReadOnlyBufferException();
        }

        byte[] copy = new byte[other.remaining()];

        if (other.hasArray()) {
            System.arraycopy(other.array(), other.arrayOffset(), copy, 0, other.remaining());
            other.position(other.limit());
        } else {
            other.get(copy, 0, other.remaining());
        }

        append(copy);
    }

    /**
     * Compact the buffer dropping arrays that have been consumed by previous
     * reads from this Composite buffer.  The limit is reset to the new capacity
     */
    public CompositeReadableBuffer compact() {
        if (readOnly || (currentArray == null && contents == null)) {
            return this;
        }

        int totalCompaction = 0;
        int totalRemovals = 0;

        for (; totalRemovals < currentArrayIndex; ++totalRemovals) {
            byte[] element = contents.remove(0);
            totalCompaction += element.length;
        }

        currentArrayIndex -= totalRemovals;

        if (currentArray.length == currentOffset) {
            totalCompaction += currentArray.length;

            // If we are sitting on the end of the data (length == offest) then
            // we are also at the last element in the ArrayList if one is currently
            // in use, so remove the data and release the list.
            if (currentArrayIndex == 0) {
                contents.clear();
                contents = null;
            }

            currentArray = null;
            currentArrayIndex = -1;
            currentOffset = 0;
        }

        position -= totalCompaction;
        limit = capacity -= totalCompaction;

        if (mark > limit) {
            mark = UNSET_MARK;
        }

        return this;
    }

    /**
     * Adds the given array into the composite buffer at the end.
     * <p>
     * The appended array is not copied so changes to the source array are visible in this
     * buffer and vice versa.  If this composite was empty than it would return true for the
     * {@link #hasArray()} method until another array is appended.
     * <p>
     * Calling this method resets the limit to the new capacity.
     *
     * @param array
     *      The array to add to this composite buffer.
     *
     * @throws IllegalArgumentException if the array is null or zero size.
     * @throws IllegalStateException if the buffer does not allow appends.
     */
    public CompositeReadableBuffer append(byte[] array) {
        if (readOnly) {
            throw new ReadOnlyBufferException();
        }

        if (array == null || array.length == 0) {
            throw new IllegalArgumentException("Array must not be empty or null");
        }

        if (currentArray == null) {
            currentArray = array;
            currentOffset = 0;
        } else if (contents == null) {
            contents = new ArrayList<>();
            contents.add(currentArray);
            contents.add(array);
            currentArrayIndex = 0;
        } else {
            contents.add(array);
        }

        capacity += array.length;
        limit = capacity;

        return this;
    }

    @Override
    public int hashCode() {
        int hash = 1;

        if (currentArrayIndex < 0) {
            int span = limit() - position();
            while (span > 0) {
                hash = 31 * hash + currentArray[currentOffset + --span];
            }
        } else {
            final int currentPos = position();
            for (int i = limit() - 1; i >= currentPos; i--) {
                hash = 31 * hash + (int)get(i);
            }
        }

        return hash;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof ReadableBuffer)) {
            return false;
        }

        ReadableBuffer buffer = (ReadableBuffer)other;
        if (this.remaining() != buffer.remaining()) {
            return false;
        }

        final int currentPos = position();

        for (int i = buffer.position(); hasRemaining(); i++) {
            if (!equals(this.get(), buffer.get(i))) {
                return false;
            }
        }

        position(currentPos);

        return true;
    }

    private static boolean equals(byte x, byte y) {
        return x == y;
    }

    private void maybeMoveToNextArray() {
        if (currentArray.length == currentOffset) {
            if (currentArrayIndex >= 0 && currentArrayIndex < (contents.size() - 1)) {
                currentArray = contents.get(++currentArrayIndex);
                currentOffset = 0;
            }
        }
    }

    private static void validateReadTarget(int destSize, int offset, int length) {
        if ((offset | length) < 0) {
            throw new IndexOutOfBoundsException("offset and legnth must be non-negative");
        }

        if (((long) offset + (long) length) > destSize) {
            throw new IndexOutOfBoundsException("target is to small for speicied read size");
        }
    }
}
