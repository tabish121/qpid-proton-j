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
package org.apache.qpid.proton.codec.buffered;

import java.util.Arrays;
import java.util.Collection;

import org.apache.qpid.proton.codec.EncodingCodes;
import org.apache.qpid.proton.codec.PrimitiveTypeEncoding;
import org.apache.qpid.proton.codec.TypeEncoding;

public class LongType extends AbstractPrimitiveType<Long> {

    public static interface LongEncoding extends PrimitiveTypeEncoding<Long> {
        void write(long l);

        void writeValue(long l);

        public long readPrimitiveValue();
    }

    private LongEncoding longEncoding;
    private LongEncoding smallLongEncoding;

    LongType(final ProtonBufferEncoderImpl encoder, final ProtonBufferDecoderImpl decoder) {
        longEncoding = new AllLongEncoding(encoder, decoder);
        smallLongEncoding = new SmallLongEncoding(encoder, decoder);
        encoder.register(Long.class, this);
        decoder.register(this);
    }

    @Override
    public Class<Long> getTypeClass() {
        return Long.class;
    }

    @Override
    public LongEncoding getEncoding(final Long val) {
        return getEncoding(val.longValue());
    }

    public LongEncoding getEncoding(final long l) {
        return (l >= -128l && l <= 127l) ? smallLongEncoding : longEncoding;
    }

    @Override
    public LongEncoding getCanonicalEncoding() {
        return longEncoding;
    }

    @Override
    public Collection<LongEncoding> getAllEncodings() {
        return Arrays.asList(smallLongEncoding, longEncoding);
    }

    public void write(long l) {
        if (l >= -128l && l <= 127l) {
            smallLongEncoding.write(l);
        } else {
            longEncoding.write(l);
        }
    }

    private class AllLongEncoding extends FixedSizePrimitiveTypeEncoding<Long> implements LongEncoding {

        public AllLongEncoding(final ProtonBufferEncoderImpl encoder, final ProtonBufferDecoderImpl decoder) {
            super(encoder, decoder);
        }

        @Override
        protected int getFixedSize() {
            return 8;
        }

        @Override
        public byte getEncodingCode() {
            return EncodingCodes.LONG;
        }

        @Override
        public LongType getType() {
            return LongType.this;
        }

        @Override
        public void writeValue(final Long val) {
            getEncoder().writeRaw(val.longValue());
        }

        @Override
        public void write(final long l) {
            writeConstructor();
            getEncoder().writeRaw(l);

        }

        @Override
        public void writeValue(final long l) {
            getEncoder().writeRaw(l);
        }

        @Override
        public boolean encodesSuperset(final TypeEncoding<Long> encoding) {
            return (getType() == encoding.getType());
        }

        @Override
        public Long readValue() {
            return readPrimitiveValue();
        }

        @Override
        public long readPrimitiveValue() {
            return getDecoder().readRawLong();
        }

        @Override
        public boolean encodesJavaPrimitive() {
            return true;
        }
    }

    private class SmallLongEncoding extends FixedSizePrimitiveTypeEncoding<Long> implements LongEncoding {
        public SmallLongEncoding(final ProtonBufferEncoderImpl encoder, final ProtonBufferDecoderImpl decoder) {
            super(encoder, decoder);
        }

        @Override
        public byte getEncodingCode() {
            return EncodingCodes.SMALLLONG;
        }

        @Override
        protected int getFixedSize() {
            return 1;
        }

        @Override
        public void write(final long l) {
            writeConstructor();
            getEncoder().writeRaw((byte) l);
        }

        @Override
        public void writeValue(final long l) {
            getEncoder().writeRaw((byte) l);
        }

        @Override
        public long readPrimitiveValue() {
            return getDecoder().readRawByte();
        }

        @Override
        public LongType getType() {
            return LongType.this;
        }

        @Override
        public void writeValue(final Long val) {
            getEncoder().writeRaw((byte) val.longValue());
        }

        @Override
        public boolean encodesSuperset(final TypeEncoding<Long> encoder) {
            return encoder == this;
        }

        @Override
        public Long readValue() {
            return readPrimitiveValue();
        }

        @Override
        public boolean encodesJavaPrimitive() {
            return true;
        }
    }
}
