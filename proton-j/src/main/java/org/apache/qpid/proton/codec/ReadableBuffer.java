/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.qpid.proton.codec;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Interface to abstract a buffer, similar to {@link WritableBuffer}
 */
public interface ReadableBuffer {

    /**
     * Returns the capacity of the backing buffer of this ReadableBuffer
     * @return the capacity of the backing buffer of this ReadableBuffer
     */
    int capacity();

    /**
     * Returns true if this ReadableBuffer is backed by an array which can be
     * accessed by the {@link #array()} and {@link #arrayOffset()} methods.
     *
     * @return true if the buffer is backed by a primitive array.
     */
    boolean hasArray();

    /**
     * Returns the primitive array that backs this buffer if one exists and the
     * buffer is not read-only.  The caller should have checked the {@link #hasArray()}
     * method before calling this method.
     *
     * @return the array that backs this buffer is available.
     *
     * @throws UnsupportedOperationException if this {@link ReadableBuffer} doesn't support array access.
     * @throws ReadOnlyBufferException if the ReadableBuffer is read-only.
     */
    byte[] array();

    /**
     * Returns the offset into the backing array where data should be read from.  The caller
     * should have checked the {@link #hasArray()} method before calling this method.
     *
     * @return the offset into the backing array to start reading from.
     *
     * @throws UnsupportedOperationException if this {@link ReadableBuffer} doesn't support array access.
     * @throws ReadOnlyBufferException if the ReadableBuffer is read-only.
     */
    int arrayOffset();

    /**
     * Compact the backing buffer of this ReadableBuffer possibly freeing portions
     * of pooled data or reducing the size of the backing array if present.
     * <p>
     * This is an optional operation and care should be taken in its implementation.
     *
     * @return a reference to this buffer
     *
     * @throws ReadOnlyBufferException if the ReadableBuffer is read-only.
     */
    ReadableBuffer compact();

    /**
     * Used to copy new data into this buffer, the contents of the given {@link ReadableBuffer}
     * will be copied into the backing store.  If this buffer does not have sufficient capacity
     * to hold the remaining bytes from the given buffer no data will be copied and a
     * {@link BufferOverflowException} will be thrown.
     *
     * @param other
     *      The ReadableBuffer whose contents will be copied into this buffer.
     *
     * @throws BufferOverflowException if this buffer cannot hold the remaining bytes from the source.
     * @throws ReadOnlyBufferException if the ReadableBuffer is read-only.
     */
    void put(ReadableBuffer other);

    byte get();

    byte get(int index);

    int getInt();

    long getLong();

    short getShort();

    float getFloat();

    double getDouble();

    ReadableBuffer get(final byte[] target, final int offset, final int length);

    ReadableBuffer get(final byte[] target);

    /**
     * Copy data from this buffer to the target buffer starting from the current
     * position and continuing until either this buffer's remaining bytes are
     * consumed or the target is full.
     *
     * @param target
     *      The WritableBuffer to transfer this buffer's data to.
     *
     * @return a reference to this ReadableBuffer instance.
     */
    ReadableBuffer get(WritableBuffer target);

    ReadableBuffer slice();

    ReadableBuffer flip();

    ReadableBuffer limit(int limit);

    int limit();

    ReadableBuffer position(int position);

    int position();

    ReadableBuffer mark();

    ReadableBuffer reset();

    ReadableBuffer rewind();

    ReadableBuffer clear();

    int remaining();

    boolean hasRemaining();

    ReadableBuffer duplicate();

    ByteBuffer byteBuffer();

    String readUTF8() throws CharacterCodingException;

    String readString(CharsetDecoder decoder) throws CharacterCodingException;

    final class ByteBufferReader implements ReadableBuffer {

        private ByteBuffer buffer;

        public static ByteBufferReader allocate(int size) {
            ByteBuffer allocated = ByteBuffer.allocate(size);
            return new ByteBufferReader(allocated);
        }

        public static ByteBufferReader wrap(ByteBuffer buffer) {
            return new ByteBufferReader(buffer);
        }

        public static ByteBufferReader wrap(byte[] array) {
            return new ByteBufferReader(ByteBuffer.wrap(array));
        }

        public ByteBufferReader(ByteBuffer buffer) {
            this.buffer = buffer;
        }

        @Override
        public int capacity() {
            return buffer.capacity();
        }

        @Override
        public byte get() {
            return buffer.get();
        }

        @Override
        public byte get(int index) {
            return buffer.get(index);
        }

        @Override
        public int getInt() {
            return buffer.getInt();
        }

        @Override
        public long getLong() {
            return buffer.getLong();
        }

        @Override
        public short getShort() {
            return buffer.getShort();
        }

        @Override
        public float getFloat() {
            return buffer.getFloat();
        }

        @Override
        public double getDouble() {
            return buffer.getDouble();
        }

        @Override
        public int limit() {
            return buffer.limit();
        }

        @Override
        public ReadableBuffer get(byte[] data, int offset, int length) {
            buffer.get(data, offset, length);
            return this;
        }

        @Override
        public ReadableBuffer get(byte[] data) {
            buffer.get(data);
            return this;
        }

        @Override
        public ReadableBuffer flip() {
            buffer.flip();
            return this;
        }

        @Override
        public ReadableBuffer position(int position) {
            buffer.position(position);
            return this;
        }

        @Override
        public ReadableBuffer slice() {
            return new ByteBufferReader(buffer.slice());
        }

        @Override
        public ReadableBuffer limit(int limit) {
            buffer.limit(limit);
            return this;
        }

        @Override
        public int remaining() {
            return buffer.remaining();
        }

        @Override
        public int position() {
            return buffer.position();
        }

        @Override
        public boolean hasRemaining() {
            return buffer.hasRemaining();
        }

        @Override
        public ReadableBuffer duplicate() {
            return new ByteBufferReader(buffer.duplicate());
        }

        @Override
        public ByteBuffer byteBuffer() {
            return buffer;
        }

        @Override
        public String readUTF8() {
            return StandardCharsets.UTF_8.decode(buffer).toString();
        }

        @Override
        public String readString(CharsetDecoder decoder) throws CharacterCodingException {
            return decoder.decode(buffer).toString();
        }

        @Override
        public void put(ReadableBuffer other) {
            this.buffer.put(other.byteBuffer());
        }

        @Override
        public boolean hasArray() {
            return buffer.hasArray();
        }

        @Override
        public byte[] array() {
            return buffer.array();
        }

        @Override
        public int arrayOffset() {
            return buffer.arrayOffset();
        }

        @Override
        public ReadableBuffer compact() {
            // Don't compact ByteBuffer due to the expense of the copy
            return this;
        }

        @Override
        public ReadableBuffer mark() {
            buffer.mark();
            return this;
        }

        @Override
        public ReadableBuffer reset() {
            buffer.reset();
            return this;
        }

        @Override
        public ReadableBuffer rewind() {
            buffer.rewind();
            return this;
        }

        @Override
        public ReadableBuffer clear() {
            buffer.clear();
            return this;
        }

        @Override
        public ReadableBuffer get(WritableBuffer target) {
            target.put(buffer);
            return this;
        }

        @Override
        public String toString() {
            return buffer.toString();
        }

        @Override
        public int hashCode() {
            return buffer.hashCode();
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }

            if (!(other instanceof ReadableBuffer)) {
                return false;
            }

            ReadableBuffer readable = (ReadableBuffer) other;
            if (this.remaining() != readable.remaining()) {
                return false;
            }

            return buffer.equals(readable.byteBuffer());
        }
    }
}