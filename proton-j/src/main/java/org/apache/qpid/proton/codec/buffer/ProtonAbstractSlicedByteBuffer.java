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
package org.apache.qpid.proton.codec.buffer;

import java.nio.ByteBuffer;

import org.apache.qpid.proton.codec.ProtonBuffer;

/**
 * Abstract based for buffer slice instances that derive from ProtonAbstractByteBuffer
 */
public abstract class ProtonAbstractSlicedByteBuffer extends ProtonAbstractByteBuffer {

    private final ProtonAbstractByteBuffer buffer;
    private final int offset;

    protected ProtonAbstractSlicedByteBuffer(ProtonAbstractByteBuffer buffer, int index, int length) {
        super(length);
        checkSliceOutOfBounds(index, length, buffer);

        if (buffer instanceof ProtonAbstractSlicedByteBuffer) {
            this.buffer = ((ProtonAbstractSlicedByteBuffer) buffer).buffer;
            this.offset = ((ProtonAbstractSlicedByteBuffer) buffer).offset + index;
//        } else if (buffer instanceof DuplicatedByteBuf) {
//            this.buffer = buffer.unwrap();
//            adjustment = index;
        } else {
            this.buffer = buffer;
            this.offset = index;
        }

        setWriteIndex(length);
    }

    @Override
    public ProtonBuffer capacity(int newCapacity) {
        throw new UnsupportedOperationException("A sliced buffer cannot have its capacity altered.");
    }

    @Override
    public ProtonBuffer duplicate() {
        return buffer.duplicate().setReadIndex(getReadIndex()).setWriteIndex(getWriteIndex());
    }

    @Override
    public ProtonBuffer copy() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ByteBuffer toByteBuffer() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean hasArray() {
        return buffer.hasArray();
    }

    @Override
    public byte[] getArray() {
        return buffer.getArray();
    }

    @Override
    public int getArrayOffset() {
        return offset(buffer.getArrayOffset());
    }

    @Override
    public byte getByte(int index) {
        checkIndex(index, 1);
        return buffer.getByte(offset(index));
    }

    @Override
    public short getShort(int index) {
        checkIndex(index, 2);
        return buffer.getShort(offset(index));
    }

    @Override
    public int getInt(int index) {
        checkIndex(index, 4);
        return buffer.getInt(offset(index));
    }

    @Override
    public long getLong(int index) {
        checkIndex(index, 8);
        return buffer.getLong(offset(index));
    }

    @Override
    public ProtonBuffer getBytes(int index, ProtonBuffer dst, int dstIndex, int length) {
        checkIndex(index, length);
        buffer.getBytes(offset(index), dst, dstIndex, length);
        return this;
    }

    @Override
    public ProtonBuffer getBytes(int index, byte[] dst, int dstIndex, int length) {
        checkIndex(index, length);
        buffer.getBytes(offset(index), dst, dstIndex, length);
        return this;
    }

    @Override
    public ProtonBuffer getBytes(int index, ByteBuffer destination) {
        checkIndex(index, destination.remaining());
        buffer.getBytes(offset(index), destination);
        return this;
    }

    @Override
    public ProtonBuffer setByte(int index, int value) {
        checkIndex(index, 1);
        buffer.setByte(offset(index), value);
        return this;
    }

    @Override
    public ProtonBuffer setShort(int index, int value) {
        checkIndex(index, 2);
        buffer.setShort(offset(index), value);
        return this;
    }

    @Override
    public ProtonBuffer setInt(int index, int value) {
        checkIndex(index, 4);
        buffer.setInt(offset(index), value);
        return this;
    }

    @Override
    public ProtonBuffer setLong(int index, long value) {
        checkIndex(index, 8);
        buffer.setLong(offset(index), value);
        return this;
    }

    @Override
    public ProtonBuffer setBytes(int index, ProtonBuffer source, int sourceIndex, int length) {
        checkIndex(index, length);
        buffer.setBytes(offset(index), source, sourceIndex, length);
        return this;
    }

    @Override
    public ProtonBuffer setBytes(int index, byte[] source, int sourceIndex, int length) {
        checkIndex(index, length);
        buffer.setBytes(offset(index), source, sourceIndex, length);
        return this;
    }

    @Override
    public ProtonBuffer setBytes(int index, ByteBuffer source) {
        checkIndex(index, source.remaining());
        buffer.setBytes(offset(index), source);
        return this;
    }

    @Override
    public ProtonBuffer slice() {
        // TODO Auto-generated method stub
        return null;
    }

    //----- Internal helper methods for buffer slices ------------------------//

    final int offset(int index) {
        return index + offset;
    }

    static void checkSliceOutOfBounds(int index, int length, ProtonAbstractByteBuffer buffer) {
        if (isOutOfBounds(index, length, buffer.capacity())) {
            throw new IndexOutOfBoundsException(buffer + ".slice(" + index + ", " + length + ')');
        }
    }
}
