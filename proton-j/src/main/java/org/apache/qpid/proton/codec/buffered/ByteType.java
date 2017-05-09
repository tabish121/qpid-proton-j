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

public class ByteType extends AbstractPrimitiveType<Byte> {
    private ByteEncoding byteEncoding;

    ByteType(final ProtonBufferEncoderImpl encoder, final ProtonBufferDecoderImpl decoder) {
        byteEncoding = new ByteEncoding(encoder, decoder);
        encoder.register(Byte.class, this);
        decoder.register(this);
    }

    @Override
    public Class<Byte> getTypeClass() {
        return Byte.class;
    }

    @Override
    public ByteEncoding getEncoding(final Byte val) {
        return byteEncoding;
    }

    @Override
    public ByteEncoding getCanonicalEncoding() {
        return byteEncoding;
    }

    @Override
    public Collection<ByteEncoding> getAllEncodings() {
        return Collections.singleton(byteEncoding);
    }

    public void writeType(byte b) {
        byteEncoding.write(b);
    }

    public class ByteEncoding extends FixedSizePrimitiveTypeEncoding<Byte> {

        public ByteEncoding(final ProtonBufferEncoderImpl encoder, final ProtonBufferDecoderImpl decoder) {
            super(encoder, decoder);
        }

        @Override
        protected int getFixedSize() {
            return 1;
        }

        @Override
        public byte getEncodingCode() {
            return EncodingCodes.BYTE;
        }

        @Override
        public ByteType getType() {
            return ByteType.this;
        }

        @Override
        public void writeValue(final Byte val) {
            getEncoder().writeRaw(val);
        }

        public void write(final byte val) {
            writeConstructor();
            getEncoder().writeRaw(val);
        }

        public void writeValue(final byte val) {
            getEncoder().writeRaw(val);
        }

        @Override
        public boolean encodesSuperset(final TypeEncoding<Byte> encoding) {
            return (getType() == encoding.getType());
        }

        @Override
        public Byte readValue() {
            return readPrimitiveValue();
        }

        public byte readPrimitiveValue() {
            return getDecoder().readRawByte();
        }

        @Override
        public boolean encodesJavaPrimitive() {
            return true;
        }
    }
}
