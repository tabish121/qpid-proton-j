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

import org.apache.qpid.proton.amqp.UnsignedLong;
import org.apache.qpid.proton.codec.EncodingCodes;
import org.apache.qpid.proton.codec.PrimitiveTypeEncoding;
import org.apache.qpid.proton.codec.TypeEncoding;

public class UnsignedLongType extends AbstractPrimitiveType<UnsignedLong> {
    public static interface UnsignedLongEncoding extends PrimitiveTypeEncoding<UnsignedLong> {

    }

    private UnsignedLongEncoding unsignedLongEncoding;
    private UnsignedLongEncoding smallUnsignedLongEncoding;
    private UnsignedLongEncoding zeroUnsignedLongEncoding;

    UnsignedLongType(final ProtonBufferEncoderImpl encoder, final ProtonBufferDecoderImpl decoder) {
        unsignedLongEncoding = new AllUnsignedLongEncoding(encoder, decoder);
        smallUnsignedLongEncoding = new SmallUnsignedLongEncoding(encoder, decoder);
        zeroUnsignedLongEncoding = new ZeroUnsignedLongEncoding(encoder, decoder);
        encoder.register(UnsignedLong.class, this);
        decoder.register(this);
    }

    @Override
    public Class<UnsignedLong> getTypeClass() {
        return UnsignedLong.class;
    }

    @Override
    public UnsignedLongEncoding getEncoding(final UnsignedLong val) {
        long l = val.longValue();
        return l == 0L ? zeroUnsignedLongEncoding : (l >= 0 && l <= 255L) ? smallUnsignedLongEncoding : unsignedLongEncoding;
    }

    @Override
    public UnsignedLongEncoding getCanonicalEncoding() {
        return unsignedLongEncoding;
    }

    @Override
    public Collection<UnsignedLongEncoding> getAllEncodings() {
        return Arrays.asList(zeroUnsignedLongEncoding, smallUnsignedLongEncoding, unsignedLongEncoding);
    }

    private class AllUnsignedLongEncoding extends FixedSizePrimitiveTypeEncoding<UnsignedLong> implements UnsignedLongEncoding {

        public AllUnsignedLongEncoding(final ProtonBufferEncoderImpl encoder, final ProtonBufferDecoderImpl decoder) {
            super(encoder, decoder);
        }

        @Override
        protected int getFixedSize() {
            return 8;
        }

        @Override
        public byte getEncodingCode() {
            return EncodingCodes.ULONG;
        }

        @Override
        public UnsignedLongType getType() {
            return UnsignedLongType.this;
        }

        @Override
        public void writeValue(final UnsignedLong val) {
            getEncoder().writeRaw(val.longValue());
        }

        @Override
        public boolean encodesSuperset(final TypeEncoding<UnsignedLong> encoding) {
            return (getType() == encoding.getType());
        }

        @Override
        public UnsignedLong readValue() {
            return UnsignedLong.valueOf(getDecoder().readRawLong());
        }
    }

    private class SmallUnsignedLongEncoding extends FixedSizePrimitiveTypeEncoding<UnsignedLong> implements UnsignedLongEncoding {
        public SmallUnsignedLongEncoding(final ProtonBufferEncoderImpl encoder, final ProtonBufferDecoderImpl decoder) {
            super(encoder, decoder);
        }

        @Override
        public byte getEncodingCode() {
            return EncodingCodes.SMALLULONG;
        }

        @Override
        protected int getFixedSize() {
            return 1;
        }

        @Override
        public UnsignedLongType getType() {
            return UnsignedLongType.this;
        }

        @Override
        public void writeValue(final UnsignedLong val) {
            getEncoder().writeRaw((byte) val.longValue());
        }

        @Override
        public boolean encodesSuperset(final TypeEncoding<UnsignedLong> encoder) {
            return encoder == this || encoder instanceof ZeroUnsignedLongEncoding;
        }

        @Override
        public UnsignedLong readValue() {
            return UnsignedLong.valueOf((getDecoder().readRawByte()) & 0xffl);
        }
    }

    private class ZeroUnsignedLongEncoding extends FixedSizePrimitiveTypeEncoding<UnsignedLong> implements UnsignedLongEncoding {
        public ZeroUnsignedLongEncoding(final ProtonBufferEncoderImpl encoder, final ProtonBufferDecoderImpl decoder) {
            super(encoder, decoder);
        }

        @Override
        public byte getEncodingCode() {
            return EncodingCodes.ULONG0;
        }

        @Override
        protected int getFixedSize() {
            return 0;
        }

        @Override
        public UnsignedLongType getType() {
            return UnsignedLongType.this;
        }

        @Override
        public void writeValue(final UnsignedLong val) {
        }

        @Override
        public boolean encodesSuperset(final TypeEncoding<UnsignedLong> encoder) {
            return encoder == this;
        }

        @Override
        public UnsignedLong readValue() {
            return UnsignedLong.ZERO;
        }
    }
}
