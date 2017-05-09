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

import java.util.Collection;
import java.util.Collections;

import org.apache.qpid.proton.amqp.Decimal128;
import org.apache.qpid.proton.codec.EncodingCodes;
import org.apache.qpid.proton.codec.TypeEncoding;

public class Decimal128Type extends AbstractPrimitiveType<Decimal128> {

    private Decimal128Encoding decimal128Encoder;

    Decimal128Type(final ProtonBufferEncoderImpl encoder, final ProtonBufferDecoderImpl decoder) {
        decimal128Encoder = new Decimal128Encoding(encoder, decoder);
        encoder.register(Decimal128.class, this);
        decoder.register(this);
    }

    @Override
    public Class<Decimal128> getTypeClass() {
        return Decimal128.class;
    }

    @Override
    public Decimal128Encoding getEncoding(final Decimal128 val) {
        return decimal128Encoder;
    }

    @Override
    public Decimal128Encoding getCanonicalEncoding() {
        return decimal128Encoder;
    }

    @Override
    public Collection<Decimal128Encoding> getAllEncodings() {
        return Collections.singleton(decimal128Encoder);
    }

    private class Decimal128Encoding extends FixedSizePrimitiveTypeEncoding<Decimal128> {

        public Decimal128Encoding(final ProtonBufferEncoderImpl encoder, final ProtonBufferDecoderImpl decoder) {
            super(encoder, decoder);
        }

        @Override
        protected int getFixedSize() {
            return 16;
        }

        @Override
        public byte getEncodingCode() {
            return EncodingCodes.DECIMAL128;
        }

        @Override
        public Decimal128Type getType() {
            return Decimal128Type.this;
        }

        @Override
        public void writeValue(final Decimal128 val) {
            getEncoder().writeRaw(val.getMostSignificantBits());
            getEncoder().writeRaw(val.getLeastSignificantBits());
        }

        @Override
        public boolean encodesSuperset(final TypeEncoding<Decimal128> encoding) {
            return (getType() == encoding.getType());
        }

        @Override
        public Decimal128 readValue() {
            long msb = getDecoder().readRawLong();
            long lsb = getDecoder().readRawLong();
            return new Decimal128(msb, lsb);
        }
    }
}
