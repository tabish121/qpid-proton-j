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

public class DoubleType extends AbstractPrimitiveType<Double> {
    private DoubleEncoding doubleEncoding;

    DoubleType(final ProtonBufferEncoderImpl encoder, final ProtonBufferDecoderImpl decoder) {
        doubleEncoding = new DoubleEncoding(encoder, decoder);
        encoder.register(Double.class, this);
        decoder.register(this);
    }

    @Override
    public Class<Double> getTypeClass() {
        return Double.class;
    }

    @Override
    public DoubleEncoding getEncoding(final Double val) {
        return doubleEncoding;
    }

    @Override
    public DoubleEncoding getCanonicalEncoding() {
        return doubleEncoding;
    }

    @Override
    public Collection<DoubleEncoding> getAllEncodings() {
        return Collections.singleton(doubleEncoding);
    }

    public void write(double d) {
        doubleEncoding.write(d);
    }

    public class DoubleEncoding extends FixedSizePrimitiveTypeEncoding<Double> {

        public DoubleEncoding(final ProtonBufferEncoderImpl encoder, final ProtonBufferDecoderImpl decoder) {
            super(encoder, decoder);
        }

        @Override
        protected int getFixedSize() {
            return 8;
        }

        @Override
        public byte getEncodingCode() {
            return EncodingCodes.DOUBLE;
        }

        @Override
        public DoubleType getType() {
            return DoubleType.this;
        }

        @Override
        public void writeValue(final Double val) {
            getEncoder().writeRaw(val.doubleValue());
        }

        public void writeValue(final double val) {
            getEncoder().writeRaw(val);
        }

        public void write(final double d) {
            writeConstructor();
            getEncoder().writeRaw(d);

        }

        @Override
        public boolean encodesSuperset(final TypeEncoding<Double> encoding) {
            return (getType() == encoding.getType());
        }

        @Override
        public Double readValue() {
            return readPrimitiveValue();
        }

        public double readPrimitiveValue() {
            return getDecoder().readRawDouble();
        }

        @Override
        public boolean encodesJavaPrimitive() {
            return true;
        }
    }
}
