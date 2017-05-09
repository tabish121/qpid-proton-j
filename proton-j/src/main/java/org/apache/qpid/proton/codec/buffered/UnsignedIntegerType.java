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

import org.apache.qpid.proton.amqp.UnsignedInteger;
import org.apache.qpid.proton.codec.EncodingCodes;
import org.apache.qpid.proton.codec.PrimitiveTypeEncoding;
import org.apache.qpid.proton.codec.TypeEncoding;

public class UnsignedIntegerType extends AbstractPrimitiveType<UnsignedInteger> {
    public static interface UnsignedIntegerEncoding extends PrimitiveTypeEncoding<UnsignedInteger> {

    }

    private UnsignedIntegerEncoding unsignedIntegerEncoding;
    private UnsignedIntegerEncoding smallUnsignedIntegerEncoding;
    private UnsignedIntegerEncoding zeroUnsignedIntegerEncoding;

    UnsignedIntegerType(final ProtonBufferEncoderImpl encoder, final ProtonBufferDecoderImpl decoder) {
        unsignedIntegerEncoding = new AllUnsignedIntegerEncoding(encoder, decoder);
        smallUnsignedIntegerEncoding = new SmallUnsignedIntegerEncoding(encoder, decoder);
        zeroUnsignedIntegerEncoding = new ZeroUnsignedIntegerEncoding(encoder, decoder);
        encoder.register(UnsignedInteger.class, this);
        decoder.register(this);
    }

    @Override
    public Class<UnsignedInteger> getTypeClass() {
        return UnsignedInteger.class;
    }

    @Override
    public UnsignedIntegerEncoding getEncoding(final UnsignedInteger val) {
        int i = val.intValue();
        return i == 0 ? zeroUnsignedIntegerEncoding : (i >= 0 && i <= 255) ? smallUnsignedIntegerEncoding : unsignedIntegerEncoding;
    }

    @Override
    public UnsignedIntegerEncoding getCanonicalEncoding() {
        return unsignedIntegerEncoding;
    }

    @Override
    public Collection<UnsignedIntegerEncoding> getAllEncodings() {
        return Arrays.asList(unsignedIntegerEncoding, smallUnsignedIntegerEncoding, zeroUnsignedIntegerEncoding);
    }

    private class AllUnsignedIntegerEncoding extends FixedSizePrimitiveTypeEncoding<UnsignedInteger> implements UnsignedIntegerEncoding {

        public AllUnsignedIntegerEncoding(final ProtonBufferEncoderImpl encoder, final ProtonBufferDecoderImpl decoder) {
            super(encoder, decoder);
        }

        @Override
        protected int getFixedSize() {
            return 4;
        }

        @Override
        public byte getEncodingCode() {
            return EncodingCodes.UINT;
        }

        @Override
        public UnsignedIntegerType getType() {
            return UnsignedIntegerType.this;
        }

        @Override
        public void writeValue(final UnsignedInteger val) {
            getEncoder().writeRaw(val.intValue());
        }

        public void write(final int i) {
            writeConstructor();
            getEncoder().writeRaw(i);

        }

        @Override
        public boolean encodesSuperset(final TypeEncoding<UnsignedInteger> encoding) {
            return (getType() == encoding.getType());
        }

        @Override
        public UnsignedInteger readValue() {
            return UnsignedInteger.valueOf(getDecoder().readRawInt());
        }
    }

    private class SmallUnsignedIntegerEncoding extends FixedSizePrimitiveTypeEncoding<UnsignedInteger> implements UnsignedIntegerEncoding {
        public SmallUnsignedIntegerEncoding(final ProtonBufferEncoderImpl encoder, final ProtonBufferDecoderImpl decoder) {
            super(encoder, decoder);
        }

        @Override
        public byte getEncodingCode() {
            return EncodingCodes.SMALLUINT;
        }

        @Override
        protected int getFixedSize() {
            return 1;
        }

        @Override
        public UnsignedIntegerType getType() {
            return UnsignedIntegerType.this;
        }

        @Override
        public void writeValue(final UnsignedInteger val) {
            getEncoder().writeRaw((byte) val.intValue());
        }

        @Override
        public boolean encodesSuperset(final TypeEncoding<UnsignedInteger> encoder) {
            return encoder == this || encoder instanceof ZeroUnsignedIntegerEncoding;
        }

        @Override
        public UnsignedInteger readValue() {
            return UnsignedInteger.valueOf((getDecoder().readRawByte()) & 0xff);
        }
    }

    private class ZeroUnsignedIntegerEncoding extends FixedSizePrimitiveTypeEncoding<UnsignedInteger> implements UnsignedIntegerEncoding {
        public ZeroUnsignedIntegerEncoding(final ProtonBufferEncoderImpl encoder, final ProtonBufferDecoderImpl decoder) {
            super(encoder, decoder);
        }

        @Override
        public byte getEncodingCode() {
            return EncodingCodes.UINT0;
        }

        @Override
        protected int getFixedSize() {
            return 0;
        }

        @Override
        public UnsignedIntegerType getType() {
            return UnsignedIntegerType.this;
        }

        @Override
        public void writeValue(final UnsignedInteger val) {
        }

        @Override
        public boolean encodesSuperset(final TypeEncoding<UnsignedInteger> encoder) {
            return encoder == this;
        }

        @Override
        public UnsignedInteger readValue() {
            return UnsignedInteger.ZERO;
        }
    }
}
