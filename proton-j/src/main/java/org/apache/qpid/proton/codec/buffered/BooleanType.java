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

import org.apache.qpid.proton.codec.EncodingCodes;
import org.apache.qpid.proton.codec.PrimitiveTypeEncoding;
import org.apache.qpid.proton.codec.TypeEncoding;

public final class BooleanType extends AbstractPrimitiveType<Boolean> {

    private static final byte BYTE_0 = (byte) 0;
    private static final byte BYTE_1 = (byte) 1;

    private BooleanEncoding trueEncoder;
    private BooleanEncoding falseEncoder;
    private BooleanEncoding booleanEncoder;

    public static interface BooleanEncoding extends PrimitiveTypeEncoding<Boolean> {
        void write(boolean b);

        void writeValue(boolean b);

        boolean readPrimitiveValue();
    }

    BooleanType(final ProtonBufferEncoderImpl encoder, final ProtonBufferDecoderImpl decoder) {
        trueEncoder = new TrueEncoding(encoder, decoder);
        falseEncoder = new FalseEncoding(encoder, decoder);
        booleanEncoder = new AllBooleanEncoding(encoder, decoder);

        encoder.register(Boolean.class, this);
        decoder.register(this);
    }

    @Override
    public Class<Boolean> getTypeClass() {
        return Boolean.class;
    }

    @Override
    public BooleanEncoding getEncoding(final Boolean val) {
        return val ? trueEncoder : falseEncoder;
    }

    public BooleanEncoding getEncoding(final boolean val) {
        return val ? trueEncoder : falseEncoder;
    }

    public void writeValue(final boolean val) {
        getEncoding(val).write(val);
    }

    @Override
    public BooleanEncoding getCanonicalEncoding() {
        return booleanEncoder;
    }

    @Override
    public Collection<BooleanEncoding> getAllEncodings() {
        return Arrays.asList(trueEncoder, falseEncoder, booleanEncoder);
    }

    private class TrueEncoding extends FixedSizePrimitiveTypeEncoding<Boolean> implements BooleanEncoding {

        public TrueEncoding(final ProtonBufferEncoderImpl encoder, final ProtonBufferDecoderImpl decoder) {
            super(encoder, decoder);
        }

        @Override
        protected int getFixedSize() {
            return 0;
        }

        @Override
        public byte getEncodingCode() {
            return EncodingCodes.BOOLEAN_TRUE;
        }

        @Override
        public BooleanType getType() {
            return BooleanType.this;
        }

        @Override
        public void writeValue(final Boolean val) {
        }

        @Override
        public void write(final boolean b) {
            writeConstructor();
        }

        @Override
        public void writeValue(final boolean b) {
        }

        @Override
        public boolean encodesSuperset(final TypeEncoding<Boolean> encoding) {
            return encoding == this;
        }

        @Override
        public Boolean readValue() {
            return Boolean.TRUE;
        }

        @Override
        public boolean readPrimitiveValue() {
            return true;
        }

        @Override
        public boolean encodesJavaPrimitive() {
            return true;
        }
    }

    private class FalseEncoding extends FixedSizePrimitiveTypeEncoding<Boolean> implements BooleanEncoding {

        public FalseEncoding(final ProtonBufferEncoderImpl encoder, final ProtonBufferDecoderImpl decoder) {
            super(encoder, decoder);
        }

        @Override
        protected int getFixedSize() {
            return 0;
        }

        @Override
        public byte getEncodingCode() {
            return EncodingCodes.BOOLEAN_FALSE;
        }

        @Override
        public BooleanType getType() {
            return BooleanType.this;
        }

        @Override
        public void writeValue(final Boolean val) {
        }

        @Override
        public void write(final boolean b) {
            writeConstructor();
        }

        @Override
        public void writeValue(final boolean b) {
        }

        @Override
        public boolean readPrimitiveValue() {
            return false;
        }

        @Override
        public boolean encodesSuperset(final TypeEncoding<Boolean> encoding) {
            return encoding == this;
        }

        @Override
        public Boolean readValue() {
            return Boolean.FALSE;
        }

        @Override
        public boolean encodesJavaPrimitive() {
            return true;
        }
    }

    private class AllBooleanEncoding extends FixedSizePrimitiveTypeEncoding<Boolean> implements BooleanEncoding {

        public AllBooleanEncoding(final ProtonBufferEncoderImpl encoder, final ProtonBufferDecoderImpl decoder) {
            super(encoder, decoder);
        }

        @Override
        public BooleanType getType() {
            return BooleanType.this;
        }

        @Override
        protected int getFixedSize() {
            return 1;
        }

        @Override
        public byte getEncodingCode() {
            return EncodingCodes.BOOLEAN;
        }

        @Override
        public void writeValue(final Boolean val) {
            getEncoder().writeRaw(val ? BYTE_1 : BYTE_0);
        }

        @Override
        public void write(final boolean val) {
            writeConstructor();
            getEncoder().writeRaw(val ? BYTE_1 : BYTE_0);
        }

        @Override
        public void writeValue(final boolean b) {
            getEncoder().writeRaw(b ? BYTE_1 : BYTE_0);
        }

        @Override
        public boolean readPrimitiveValue() {

            return getDecoder().readRawByte() != BYTE_0;
        }

        @Override
        public boolean encodesSuperset(final TypeEncoding<Boolean> encoding) {
            return (getType() == encoding.getType());
        }

        @Override
        public Boolean readValue() {
            return readPrimitiveValue() ? Boolean.TRUE : Boolean.FALSE;
        }

        @Override
        public boolean encodesJavaPrimitive() {
            return true;
        }
    }
}
