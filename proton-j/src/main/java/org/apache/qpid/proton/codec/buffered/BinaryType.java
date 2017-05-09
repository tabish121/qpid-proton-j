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

import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.codec.EncodingCodes;
import org.apache.qpid.proton.codec.PrimitiveTypeEncoding;
import org.apache.qpid.proton.codec.TypeEncoding;

public class BinaryType extends AbstractPrimitiveType<Binary> {

    private final BinaryEncoding binaryEncoding;
    private final BinaryEncoding shortBinaryEncoding;

    private static interface BinaryEncoding extends PrimitiveTypeEncoding<Binary> {

    }

    BinaryType(final ProtonBufferEncoderImpl encoder, final ProtonBufferDecoderImpl decoder) {
        binaryEncoding = new LongBinaryEncoding(encoder, decoder);
        shortBinaryEncoding = new ShortBinaryEncoding(encoder, decoder);
        encoder.register(Binary.class, this);
        decoder.register(this);
    }

    @Override
    public Class<Binary> getTypeClass() {
        return Binary.class;
    }

    @Override
    public BinaryEncoding getEncoding(final Binary val) {
        return val.getLength() <= 255 ? shortBinaryEncoding : binaryEncoding;
    }

    @Override
    public BinaryEncoding getCanonicalEncoding() {
        return binaryEncoding;
    }

    @Override
    public Collection<BinaryEncoding> getAllEncodings() {
        return Arrays.asList(shortBinaryEncoding, binaryEncoding);
    }

    private class LongBinaryEncoding extends LargeFloatingSizePrimitiveTypeEncoding<Binary> implements BinaryEncoding {

        public LongBinaryEncoding(final ProtonBufferEncoderImpl encoder, final ProtonBufferDecoderImpl decoder) {
            super(encoder, decoder);
        }

        @Override
        protected void writeEncodedValue(final Binary val) {
            getEncoder().writeRaw(val.getArray(), val.getArrayOffset(), val.getLength());
        }

        @Override
        protected int getEncodedValueSize(final Binary val) {
            return val.getLength();
        }

        @Override
        public byte getEncodingCode() {
            return EncodingCodes.VBIN32;
        }

        @Override
        public BinaryType getType() {
            return BinaryType.this;
        }

        @Override
        public boolean encodesSuperset(final TypeEncoding<Binary> encoding) {
            return (getType() == encoding.getType());
        }

        @Override
        public Binary readValue() {
            final ProtonBufferDecoderImpl decoder = getDecoder();
            int size = decoder.readRawInt();
            if (size > decoder.getByteBufferRemaining()) {
                throw new IllegalArgumentException(
                    "Binary data size " + size + " is specified to be greater than the amount of data available (" + decoder.getByteBufferRemaining() + ")");
            }
            byte[] data = new byte[size];
            decoder.readRaw(data, 0, size);
            return new Binary(data);
        }
    }

    private class ShortBinaryEncoding extends SmallFloatingSizePrimitiveTypeEncoding<Binary> implements BinaryEncoding {

        public ShortBinaryEncoding(final ProtonBufferEncoderImpl encoder, final ProtonBufferDecoderImpl decoder) {
            super(encoder, decoder);
        }

        @Override
        protected void writeEncodedValue(final Binary val) {
            getEncoder().writeRaw(val.getArray(), val.getArrayOffset(), val.getLength());
        }

        @Override
        protected int getEncodedValueSize(final Binary val) {
            return val.getLength();
        }

        @Override
        public byte getEncodingCode() {
            return EncodingCodes.VBIN8;
        }

        @Override
        public BinaryType getType() {
            return BinaryType.this;
        }

        @Override
        public boolean encodesSuperset(final TypeEncoding<Binary> encoder) {
            return encoder == this;
        }

        @Override
        public Binary readValue() {
            int size = (getDecoder().readRawByte()) & 0xff;
            byte[] data = new byte[size];
            getDecoder().readRaw(data, 0, size);
            return new Binary(data);
        }
    }
}
