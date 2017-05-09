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

import org.apache.qpid.proton.amqp.UnsignedShort;
import org.apache.qpid.proton.codec.EncodingCodes;
import org.apache.qpid.proton.codec.TypeEncoding;

public class UnsignedShortType extends AbstractPrimitiveType<UnsignedShort> {

    private UnsignedShortEncoding unsignedShortEncoder;

    UnsignedShortType(final ProtonBufferEncoderImpl encoder, final ProtonBufferDecoderImpl decoder) {
        unsignedShortEncoder = new UnsignedShortEncoding(encoder, decoder);
        encoder.register(UnsignedShort.class, this);
        decoder.register(this);
    }

    @Override
    public Class<UnsignedShort> getTypeClass() {
        return UnsignedShort.class;
    }

    @Override
    public UnsignedShortEncoding getEncoding(final UnsignedShort val) {
        return unsignedShortEncoder;
    }

    @Override
    public UnsignedShortEncoding getCanonicalEncoding() {
        return unsignedShortEncoder;
    }

    @Override
    public Collection<UnsignedShortEncoding> getAllEncodings() {
        return Collections.singleton(unsignedShortEncoder);
    }

    private class UnsignedShortEncoding extends FixedSizePrimitiveTypeEncoding<UnsignedShort> {

        public UnsignedShortEncoding(final ProtonBufferEncoderImpl encoder, final ProtonBufferDecoderImpl decoder) {
            super(encoder, decoder);
        }

        @Override
        protected int getFixedSize() {
            return 2;
        }

        @Override
        public byte getEncodingCode() {
            return EncodingCodes.USHORT;
        }

        @Override
        public UnsignedShortType getType() {
            return UnsignedShortType.this;
        }

        @Override
        public void writeValue(final UnsignedShort val) {
            getEncoder().writeRaw(val.shortValue());
        }

        @Override
        public boolean encodesSuperset(final TypeEncoding<UnsignedShort> encoding) {
            return (getType() == encoding.getType());
        }

        @Override
        public UnsignedShort readValue() {
            return UnsignedShort.valueOf(getDecoder().readRawShort());
        }
    }
}
