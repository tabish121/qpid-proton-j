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
 * Support for ProtonBuffer
 */
public class ProtonByteBufferSupport {

    /**
     * Wrapper class for ByteBuffer
     */
    public static class ByteBufferWrapper implements ProtonBuffer {

        final ByteBuffer buffer;

        public ByteBufferWrapper(ByteBuffer buffer) {
            this.buffer = buffer;
        }

        @Override
        public int capacity() {
            return buffer.capacity();
        }

        @Override
        public ProtonBuffer capacity(int newCapacity) {
            // TODO - Reallocate and copy to new ByteBuffer ?
            throw new UnsupportedOperationException("Not yet implemented");
        }

        @Override
        public int maxCapacity() {
            return buffer.capacity();
        }

        @Override
        public ProtonBuffer duplicate() {
            return new ByteBufferWrapper(buffer.duplicate());
        }

        @Override
        public ProtonBuffer copy() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ProtonBuffer clear() {
            buffer.clear();
            return this;
        }

        @Override
        public ByteBuffer toByteBuffer() {
            return buffer;
        }

        @Override
        public boolean hasArray() {
            return buffer.hasArray();
        }

        @Override
        public byte[] getArray() {
            return buffer.array();
        }

        @Override
        public int getArrayOffset() {
            return buffer.arrayOffset();
        }

        @Override
        public int getReadableBytes() {
            return buffer.remaining();
        }

        @Override
        public int getWritableBytes() {
            return buffer.remaining();
        }

        @Override
        public int getReadIndex() {
            return buffer.position();
        }

        @Override
        public ProtonBuffer setReadIndex(int value) {
            buffer.position(value);
            return this;
        }

        @Override
        public int getWriteIndex() {
            return buffer.position();
        }

        @Override
        public ProtonBuffer setWriteIndex(int value) {
            buffer.position(value);
            return this;
        }

        @Override
        public ProtonBuffer setIndex(int readIndex, int writeIndex) {
            if (readIndex != writeIndex) {
                throw new UnsupportedOperationException();
            }

            buffer.position(readIndex);
            return this;
        }

        @Override
        public ProtonBuffer markReadIndex() {
            buffer.mark();
            return this;
        }

        @Override
        public ProtonBuffer resetReadIndex() {
            buffer.reset();
            return this;
        }

        @Override
        public ProtonBuffer markWriteIndex() {
            buffer.mark();
            return this;
        }

        @Override
        public ProtonBuffer resetWriteIndex() {
            buffer.reset();
            return this;
        }

        @Override
        public byte readByte() {
            return buffer.get();
        }

        @Override
        public ProtonBuffer readBytes(byte[] target) {
            buffer.get(target);
            return this;
        }

        @Override
        public ProtonBuffer readBytes(byte[] target, int length) {
            buffer.get(target, 0, length);
            return this;
        }

        @Override
        public ProtonBuffer readBytes(byte[] target, int offset, int length) {
            buffer.get(target, offset, length);
            return this;
        }

        @Override
        public ProtonBuffer readBytes(ProtonBuffer target) {
            return readBytes(target, 0, buffer.remaining());
        }

        @Override
        public ProtonBuffer readBytes(ProtonBuffer target, int length) {
            return readBytes(target, 0, buffer.remaining());
        }

        @Override
        public ProtonBuffer readBytes(ProtonBuffer target, int offset, int length) {
            // TODO - Implement sanely.

            target.writeInt(offset);
            while (buffer.hasRemaining() && length-- > 0) {
                target.writeByte(buffer.get());
            }

            return this;
        }

        @Override
        public boolean readBoolean() {
            return buffer.get() == 0 ? false : true;
        }

        @Override
        public short readShort() {
            return buffer.getShort();
        }

        @Override
        public int readInt() {
            return buffer.getInt();
        }

        @Override
        public long readLong() {
            return buffer.getLong();
        }

        @Override
        public float readFloat() {
            return buffer.getFloat();
        }

        @Override
        public double readDouble() {
            return buffer.getDouble();
        }

        @Override
        public ProtonBuffer writeByte(byte value) {
            buffer.put(value);
            return this;
        }

        @Override
        public ProtonBuffer writeBytes(byte[] value) {
            buffer.put(value);
            return this;
        }

        @Override
        public ProtonBuffer writeBytes(byte[] value, int length) {
            buffer.put(value, 0, length);
            return this;
        }

        @Override
        public ProtonBuffer writeBytes(byte[] value, int offset, int length) {
            buffer.put(value, offset, length);
            return this;
        }

        @Override
        public ProtonBuffer writeBoolean(boolean value) {
            buffer.put((byte) (value ? 1 : 0));
            return this;
        }

        @Override
        public ProtonBuffer writeShort(short value) {
            buffer.putShort(value);
            return this;
        }

        @Override
        public ProtonBuffer writeInt(int value) {
            buffer.putInt(value);
            return this;
        }

        @Override
        public ProtonBuffer writeLong(long value) {
            buffer.putLong(value);
            return this;
        }

        @Override
        public ProtonBuffer writeFloat(float value) {
            buffer.putFloat(value);
            return this;
        }

        @Override
        public ProtonBuffer writeDouble(double value) {
            buffer.putDouble(value);
            return this;
        }

        @Override
        public ProtonBuffer slice() {
            // TODO slice view buffer wrapper
            return null;
        }

        @Override
        public boolean getBoolean(int index) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public byte getByte(int index) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public short getUnsignedByte(int index) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public char getChar(int index) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public short getShort(int index) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int getUnsignedShort(int index) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int getInt(int index) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public long getUnsignedInt(int index) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public long getLong(int index) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public float getFloat(int index) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public double getDouble(int index) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public ProtonBuffer setByte(int index, int value) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ProtonBuffer setBoolean(int index, boolean value) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ProtonBuffer setChar(int index, int value) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ProtonBuffer setShort(int index, int value) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ProtonBuffer setInt(int index, int value) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ProtonBuffer setLong(int index, long value) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ProtonBuffer setFloat(int index, float value) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ProtonBuffer setDouble(int index, double value) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean isReadable() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isReadable(int size) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isWritable() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isWritable(int size) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public ProtonBuffer getBytes(int index, ProtonBuffer dst) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ProtonBuffer getBytes(int index, ProtonBuffer dst, int length) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ProtonBuffer getBytes(int index, ProtonBuffer dst, int dstIndex, int length) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ProtonBuffer getBytes(int index, byte[] dst) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ProtonBuffer getBytes(int index, byte[] dst, int dstIndex, int length) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ProtonBuffer getBytes(int index, ByteBuffer dst) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ProtonBuffer setBytes(int index, ProtonBuffer src) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ProtonBuffer setBytes(int index, ProtonBuffer src, int length) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ProtonBuffer setBytes(int index, ProtonBuffer src, int srcIndex, int length) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ProtonBuffer setBytes(int index, byte[] src) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ProtonBuffer setBytes(int index, byte[] src, int srcIndex, int length) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ProtonBuffer setBytes(int index, ByteBuffer src) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ProtonBuffer readBytes(ByteBuffer dst) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ProtonBuffer skipBytes(int length) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ProtonBuffer writeBytes(ProtonBuffer src) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ProtonBuffer writeBytes(ProtonBuffer src, int length) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ProtonBuffer writeBytes(ProtonBuffer src, int srcIndex, int length) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ProtonBuffer writeBytes(ByteBuffer src) {
            // TODO Auto-generated method stub
            return null;
        }
    }
}
