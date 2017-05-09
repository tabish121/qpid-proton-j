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

public class FloatType extends AbstractPrimitiveType<Float> {
    private FloatEncoding floatEncoding;

    FloatType(final ProtonBufferEncoderImpl encoder, final ProtonBufferDecoderImpl decoder) {
        floatEncoding = new FloatEncoding(encoder, decoder);
        encoder.register(Float.class, this);
        decoder.register(this);
    }

    @Override
    public Class<Float> getTypeClass() {
        return Float.class;
    }

    @Override
    public FloatEncoding getEncoding(final Float val) {
        return floatEncoding;
    }

    @Override
    public FloatEncoding getCanonicalEncoding() {
        return floatEncoding;
    }

    @Override
    public Collection<FloatEncoding> getAllEncodings() {
        return Collections.singleton(floatEncoding);
    }

    public void write(float f) {
        floatEncoding.write(f);
    }

    public class FloatEncoding extends FixedSizePrimitiveTypeEncoding<Float> {

        public FloatEncoding(final ProtonBufferEncoderImpl encoder, final ProtonBufferDecoderImpl decoder) {
            super(encoder, decoder);
        }

        @Override
        protected int getFixedSize() {
            return 4;
        }

        @Override
        public byte getEncodingCode() {
            return EncodingCodes.FLOAT;
        }

        @Override
        public FloatType getType() {
            return FloatType.this;
        }

        @Override
        public void writeValue(final Float val) {
            getEncoder().writeRaw(val.floatValue());
        }

        public void writeValue(final float val) {
            getEncoder().writeRaw(val);
        }

        public void write(final float f) {
            writeConstructor();
            getEncoder().writeRaw(f);

        }

        @Override
        public boolean encodesSuperset(final TypeEncoding<Float> encoding) {
            return (getType() == encoding.getType());
        }

        @Override
        public Float readValue() {
            return readPrimitiveValue();
        }

        public float readPrimitiveValue() {
            return getDecoder().readRawFloat();
        }

        @Override
        public boolean encodesJavaPrimitive() {
            return true;
        }
    }
}
