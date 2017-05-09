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

import org.apache.qpid.proton.amqp.Decimal64;
import org.apache.qpid.proton.codec.EncodingCodes;
import org.apache.qpid.proton.codec.TypeEncoding;

public class Decimal64Type extends AbstractPrimitiveType<Decimal64> {
    private Decimal64Encoding decimal64Encoder;

    Decimal64Type(final ProtonBufferEncoderImpl encoder, final ProtonBufferDecoderImpl decoder) {
        decimal64Encoder = new Decimal64Encoding(encoder, decoder);
        encoder.register(Decimal64.class, this);
        decoder.register(this);
    }

    @Override
    public Class<Decimal64> getTypeClass() {
        return Decimal64.class;
    }

    @Override
    public Decimal64Encoding getEncoding(final Decimal64 val) {
        return decimal64Encoder;
    }

    @Override
    public Decimal64Encoding getCanonicalEncoding() {
        return decimal64Encoder;
    }

    @Override
    public Collection<Decimal64Encoding> getAllEncodings() {
        return Collections.singleton(decimal64Encoder);
    }

    private class Decimal64Encoding extends FixedSizePrimitiveTypeEncoding<Decimal64> {

        public Decimal64Encoding(final ProtonBufferEncoderImpl encoder, final ProtonBufferDecoderImpl decoder) {
            super(encoder, decoder);
        }

        @Override
        protected int getFixedSize() {
            return 8;
        }

        @Override
        public byte getEncodingCode() {
            return EncodingCodes.DECIMAL64;
        }

        @Override
        public Decimal64Type getType() {
            return Decimal64Type.this;
        }

        @Override
        public void writeValue(final Decimal64 val) {
            getEncoder().writeRaw(val.getBits());
        }

        @Override
        public boolean encodesSuperset(final TypeEncoding<Decimal64> encoding) {
            return (getType() == encoding.getType());
        }

        @Override
        public Decimal64 readValue() {
            return new Decimal64(getDecoder().readRawLong());
        }
    }
}
