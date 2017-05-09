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

import org.apache.qpid.proton.amqp.Decimal32;
import org.apache.qpid.proton.codec.EncodingCodes;
import org.apache.qpid.proton.codec.TypeEncoding;

public class Decimal32Type extends AbstractPrimitiveType<Decimal32> {
    private Decimal32Encoding decimal32Encoder;

    Decimal32Type(final ProtonBufferEncoderImpl encoder, final ProtonBufferDecoderImpl decoder) {
        decimal32Encoder = new Decimal32Encoding(encoder, decoder);
        encoder.register(Decimal32.class, this);
        decoder.register(this);
    }

    @Override
    public Class<Decimal32> getTypeClass() {
        return Decimal32.class;
    }

    @Override
    public Decimal32Encoding getEncoding(final Decimal32 val) {
        return decimal32Encoder;
    }

    @Override
    public Decimal32Encoding getCanonicalEncoding() {
        return decimal32Encoder;
    }

    @Override
    public Collection<Decimal32Encoding> getAllEncodings() {
        return Collections.singleton(decimal32Encoder);
    }

    private class Decimal32Encoding extends FixedSizePrimitiveTypeEncoding<Decimal32> {

        public Decimal32Encoding(final ProtonBufferEncoderImpl encoder, final ProtonBufferDecoderImpl decoder) {
            super(encoder, decoder);
        }

        @Override
        protected int getFixedSize() {
            return 4;
        }

        @Override
        public byte getEncodingCode() {
            return EncodingCodes.DECIMAL32;
        }

        @Override
        public Decimal32Type getType() {
            return Decimal32Type.this;
        }

        @Override
        public void writeValue(final Decimal32 val) {
            getEncoder().writeRaw(val.getBits());
        }

        @Override
        public boolean encodesSuperset(final TypeEncoding<Decimal32> encoding) {
            return (getType() == encoding.getType());
        }

        @Override
        public Decimal32 readValue() {
            return new Decimal32(getDecoder().readRawInt());
        }
    }
}
