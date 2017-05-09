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

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;

import org.apache.qpid.proton.codec.EncodingCodes;
import org.apache.qpid.proton.codec.PrimitiveTypeEncoding;
import org.apache.qpid.proton.codec.TypeEncoding;

public class BigIntegerType extends AbstractPrimitiveType<BigInteger> {

    public static interface BigIntegerEncoding extends PrimitiveTypeEncoding<BigInteger> {
        void write(BigInteger l);

        @Override
        void writeValue(BigInteger l);

        public BigInteger readPrimitiveValue();
    }

    private static final BigInteger BIG_BYTE_MIN = BigInteger.valueOf(Byte.MIN_VALUE);
    private static final BigInteger BIG_BYTE_MAX = BigInteger.valueOf(Byte.MAX_VALUE);
    private static final BigInteger BIG_LONG_MIN = BigInteger.valueOf(Long.MIN_VALUE);;
    private static final BigInteger BIG_LONG_MAX = BigInteger.valueOf(Long.MAX_VALUE);;

    private BigIntegerEncoding bigIntegerEncoding;
    private BigIntegerEncoding smallBigIntegerEncoding;

    BigIntegerType(final ProtonBufferEncoderImpl encoder, final ProtonBufferDecoderImpl decoder) {
        bigIntegerEncoding = new AllBigIntegerEncoding(encoder, decoder);
        smallBigIntegerEncoding = new SmallBigIntegerEncoding(encoder, decoder);
        encoder.register(BigInteger.class, this);
    }

    @Override
    public Class<BigInteger> getTypeClass() {
        return BigInteger.class;
    }

    @Override
    public BigIntegerEncoding getEncoding(final BigInteger l) {
        return (l.compareTo(BIG_BYTE_MIN) >= 0 && l.compareTo(BIG_BYTE_MAX) <= 0) ? smallBigIntegerEncoding : bigIntegerEncoding;
    }

    @Override
    public BigIntegerEncoding getCanonicalEncoding() {
        return bigIntegerEncoding;
    }

    @Override
    public Collection<BigIntegerEncoding> getAllEncodings() {
        return Arrays.asList(smallBigIntegerEncoding, bigIntegerEncoding);
    }

    private long longValueExact(final BigInteger val) {
        if (val.compareTo(BIG_LONG_MIN) < 0 || val.compareTo(BIG_LONG_MAX) > 0) {
            throw new ArithmeticException("cannot encode BigInteger not representable as long");
        }
        return val.longValue();
    }

    private class AllBigIntegerEncoding extends FixedSizePrimitiveTypeEncoding<BigInteger> implements BigIntegerEncoding {

        public AllBigIntegerEncoding(final ProtonBufferEncoderImpl encoder, final ProtonBufferDecoderImpl decoder) {
            super(encoder, decoder);
        }

        @Override
        protected int getFixedSize() {
            return 8;
        }

        @Override
        public byte getEncodingCode() {
            return EncodingCodes.LONG;
        }

        @Override
        public BigIntegerType getType() {
            return BigIntegerType.this;
        }

        @Override
        public void writeValue(final BigInteger val) {
            getEncoder().writeRaw(longValueExact(val));
        }

        @Override
        public void write(final BigInteger l) {
            writeConstructor();
            getEncoder().writeRaw(longValueExact(l));

        }

        @Override
        public boolean encodesSuperset(final TypeEncoding<BigInteger> encoding) {
            return (getType() == encoding.getType());
        }

        @Override
        public BigInteger readValue() {
            return readPrimitiveValue();
        }

        @Override
        public BigInteger readPrimitiveValue() {
            return BigInteger.valueOf(getDecoder().readLong());
        }

        @Override
        public boolean encodesJavaPrimitive() {
            return true;
        }
    }

    private class SmallBigIntegerEncoding extends FixedSizePrimitiveTypeEncoding<BigInteger> implements BigIntegerEncoding {
        public SmallBigIntegerEncoding(final ProtonBufferEncoderImpl encoder, final ProtonBufferDecoderImpl decoder) {
            super(encoder, decoder);
        }

        @Override
        public byte getEncodingCode() {
            return EncodingCodes.SMALLLONG;
        }

        @Override
        protected int getFixedSize() {
            return 1;
        }

        @Override
        public void write(final BigInteger l) {
            writeConstructor();
            getEncoder().writeRaw(l.byteValue());
        }

        @Override
        public BigInteger readPrimitiveValue() {
            return BigInteger.valueOf(getDecoder().readRawByte());
        }

        @Override
        public BigIntegerType getType() {
            return BigIntegerType.this;
        }

        @Override
        public void writeValue(final BigInteger val) {
            getEncoder().writeRaw(val.byteValue());
        }

        @Override
        public boolean encodesSuperset(final TypeEncoding<BigInteger> encoder) {
            return encoder == this;
        }

        @Override
        public BigInteger readValue() {
            return readPrimitiveValue();
        }

        @Override
        public boolean encodesJavaPrimitive() {
            return true;
        }
    }
}
