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

import org.apache.qpid.proton.codec.EncodingCodes;
import org.apache.qpid.proton.codec.TypeEncoding;

public class ShortType extends AbstractPrimitiveType<Short> {

    private ShortEncoding shortEncoding;

    ShortType(final ProtonBufferEncoderImpl encoder, final ProtonBufferDecoderImpl decoder) {
        shortEncoding = new ShortEncoding(encoder, decoder);
        encoder.register(Short.class, this);
        decoder.register(this);
    }

    @Override
    public Class<Short> getTypeClass() {
        return Short.class;
    }

    @Override
    public ShortEncoding getEncoding(final Short val) {
        return shortEncoding;
    }

    public void write(short s) {
        shortEncoding.write(s);
    }

    @Override
    public ShortEncoding getCanonicalEncoding() {
        return shortEncoding;
    }

    @Override
    public Collection<ShortEncoding> getAllEncodings() {
        return Collections.singleton(shortEncoding);
    }

    public class ShortEncoding extends FixedSizePrimitiveTypeEncoding<Short> {

        public ShortEncoding(final ProtonBufferEncoderImpl encoder, final ProtonBufferDecoderImpl decoder) {
            super(encoder, decoder);
        }

        @Override
        protected int getFixedSize() {
            return 2;
        }

        @Override
        public byte getEncodingCode() {
            return EncodingCodes.SHORT;
        }

        @Override
        public ShortType getType() {
            return ShortType.this;
        }

        @Override
        public void writeValue(final Short val) {
            getEncoder().writeRaw(val);
        }

        public void writeValue(final short val) {
            getEncoder().writeRaw(val);
        }

        public void write(final short s) {
            writeConstructor();
            getEncoder().writeRaw(s);
        }

        @Override
        public boolean encodesSuperset(final TypeEncoding<Short> encoding) {
            return (getType() == encoding.getType());
        }

        @Override
        public Short readValue() {
            return readPrimitiveValue();
        }

        public short readPrimitiveValue() {
            return getDecoder().readRawShort();
        }

        @Override
        public boolean encodesJavaPrimitive() {
            return true;
        }
    }
}
