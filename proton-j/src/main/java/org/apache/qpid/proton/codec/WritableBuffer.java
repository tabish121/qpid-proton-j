/*
 *
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
 *
 */

package org.apache.qpid.proton.codec;

import java.nio.ByteBuffer;

public interface WritableBuffer
{
    void put(byte b);

    void putFloat(float f);

    void putDouble(double d);

    void put(byte[] src, int offset, int length);

    void putShort(short s);

    void putInt(int i);

    void putLong(long l);

    boolean hasRemaining();

    int remaining();

    int position();

    void position(int position);

    void put(ByteBuffer payload);

    void put(ReadableBuffer payload);

    int limit();

    class ByteBufferWrapper implements WritableBuffer
    {
        private final ByteBuffer _buf;

        public ByteBufferWrapper(ByteBuffer buf)
        {
            _buf = buf;
        }

        @Override
        public void put(byte b)
        {
            _buf.put(b);
        }

        @Override
        public void putFloat(float f)
        {
            _buf.putFloat(f);
        }

        @Override
        public void putDouble(double d)
        {
            _buf.putDouble(d);
        }

        @Override
        public void put(byte[] src, int offset, int length)
        {
            _buf.put(src, offset, length);
        }

        @Override
        public void putShort(short s)
        {
            _buf.putShort(s);
        }

        @Override
        public void putInt(int i)
        {
            _buf.putInt(i);
        }

        @Override
        public void putLong(long l)
        {
            _buf.putLong(l);
        }

        @Override
        public boolean hasRemaining()
        {
            return _buf.hasRemaining();
        }

        @Override
        public int remaining()
        {
            return _buf.remaining();
        }

        @Override
        public int position()
        {
            return _buf.position();
        }

        @Override
        public void position(int position)
        {
            _buf.position(position);
        }

        @Override
        public void put(ByteBuffer src)
        {
            _buf.put(src);
        }

        @Override
        public void put(ReadableBuffer src)
        {
            src.get(this);
        }

        @Override
        public int limit()
        {
            return _buf.limit();
        }

        public ByteBuffer byteBuffer()
        {
            return _buf;
        }

        public ReadableBuffer toReadableBuffer()
        {
            return ReadableBuffer.ByteBufferReader.wrap((ByteBuffer) _buf.duplicate().flip());
        }

        @Override
        public String toString()
        {
            return String.format("[pos: %d, limit: %d, remaining:%d]", _buf.position(), _buf.limit(), _buf.remaining());
        }

        public static ByteBufferWrapper allocate(int size)
        {
            ByteBuffer allocated = ByteBuffer.allocate(size);
            return new ByteBufferWrapper(allocated);
        }

        public static ByteBufferWrapper wrap(ByteBuffer buffer)
        {
            return new ByteBufferWrapper(buffer);
        }

        public static ByteBufferWrapper wrap(byte[] bytes)
        {
            return new ByteBufferWrapper(ByteBuffer.wrap(bytes));
        }
    }
}
