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

public class IntegerType extends AbstractPrimitiveType<Integer> {

    public static interface IntegerEncoding extends PrimitiveTypeEncoding<Integer> {

        void write(int i);

        void writeValue(int i);

        int readPrimitiveValue();
    }

    private IntegerEncoding integerEncoding;
    private IntegerEncoding smallIntegerEncoding;

    IntegerType(final ProtonBufferEncoderImpl encoder, final ProtonBufferDecoderImpl decoder) {
        integerEncoding = new AllIntegerEncoding(encoder, decoder);
        smallIntegerEncoding = new SmallIntegerEncoding(encoder, decoder);
        encoder.register(Integer.class, this);
        decoder.register(this);
    }

    @Override
    public Class<Integer> getTypeClass() {
        return Integer.class;
    }

    @Override
    public IntegerEncoding getEncoding(final Integer val) {
        return getEncoding(val.intValue());
    }

    public IntegerEncoding getEncoding(final int i) {

        return (i >= -128 && i <= 127) ? smallIntegerEncoding : integerEncoding;
    }

    @Override
    public IntegerEncoding getCanonicalEncoding() {
        return integerEncoding;
    }

    @Override
    public Collection<IntegerEncoding> getAllEncodings() {
        return Arrays.asList(integerEncoding, smallIntegerEncoding);
    }

    public void write(int i) {
        if (i >= -128 && i <= 127) {
            smallIntegerEncoding.write(i);
        } else {
            integerEncoding.write(i);
        }
    }

    private class AllIntegerEncoding extends FixedSizePrimitiveTypeEncoding<Integer> implements IntegerEncoding {

        public AllIntegerEncoding(final ProtonBufferEncoderImpl encoder, final ProtonBufferDecoderImpl decoder) {
            super(encoder, decoder);
        }

        @Override
        protected int getFixedSize() {
            return 4;
        }

        @Override
        public byte getEncodingCode() {
            return EncodingCodes.INT;
        }

        @Override
        public IntegerType getType() {
            return IntegerType.this;
        }

        @Override
        public void writeValue(final Integer val) {
            getEncoder().writeRaw(val.intValue());
        }

        @Override
        public void write(final int i) {
            writeConstructor();
            getEncoder().writeRaw(i);

        }

        @Override
        public void writeValue(final int i) {
            getEncoder().writeRaw(i);
        }

        @Override
        public int readPrimitiveValue() {
            return getDecoder().readRawInt();
        }

        @Override
        public boolean encodesSuperset(final TypeEncoding<Integer> encoding) {
            return (getType() == encoding.getType());
        }

        @Override
        public Integer readValue() {
            return readPrimitiveValue();
        }

        @Override
        public boolean encodesJavaPrimitive() {
            return true;
        }
    }

    private class SmallIntegerEncoding extends FixedSizePrimitiveTypeEncoding<Integer> implements IntegerEncoding {
        public SmallIntegerEncoding(final ProtonBufferEncoderImpl encoder, final ProtonBufferDecoderImpl decoder) {
            super(encoder, decoder);
        }

        @Override
        public byte getEncodingCode() {
            return EncodingCodes.SMALLINT;
        }

        @Override
        protected int getFixedSize() {
            return 1;
        }

        @Override
        public void write(final int i) {
            writeConstructor();
            getEncoder().writeRaw((byte) i);
        }

        @Override
        public void writeValue(final int i) {
            getEncoder().writeRaw((byte) i);
        }

        @Override
        public int readPrimitiveValue() {
            return getDecoder().readRawByte();
        }

        @Override
        public IntegerType getType() {
            return IntegerType.this;
        }

        @Override
        public void writeValue(final Integer val) {
            getEncoder().writeRaw((byte) val.intValue());
        }

        @Override
        public boolean encodesSuperset(final TypeEncoding<Integer> encoder) {
            return encoder == this;
        }

        @Override
        public Integer readValue() {
            return readPrimitiveValue();
        }

        @Override
        public boolean encodesJavaPrimitive() {
            return true;
        }
    }
}
