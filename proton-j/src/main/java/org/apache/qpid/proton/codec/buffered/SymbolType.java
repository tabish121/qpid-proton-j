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

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.codec.EncodingCodes;
import org.apache.qpid.proton.codec.PrimitiveTypeEncoding;
import org.apache.qpid.proton.codec.TypeEncoding;

public class SymbolType extends AbstractPrimitiveType<Symbol> {
    private static final Charset ASCII_CHARSET = Charset.forName("US-ASCII");
    private final SymbolEncoding symbolEncoding;
    private final SymbolEncoding shortSymbolEncoding;

    private final Map<ByteBuffer, Symbol> symbolCache = new HashMap<>();
    private ProtonBufferDecoderImpl.TypeDecoder<Symbol> symbolCreator = new ProtonBufferDecoderImpl.TypeDecoder<Symbol>() {

        @Override
        public Symbol decode(final ByteBuffer buf) {

            Symbol symbol = symbolCache.get(buf);
            if (symbol == null) {
                byte[] bytes = new byte[buf.limit()];
                buf.get(bytes);

                String str = new String(bytes, ASCII_CHARSET);
                symbol = Symbol.getSymbol(str);

                symbolCache.put(ByteBuffer.wrap(bytes), symbol);
            }
            return symbol;
        }
    };

    public static interface SymbolEncoding extends PrimitiveTypeEncoding<Symbol> {

    }

    SymbolType(final ProtonBufferEncoderImpl encoder, final ProtonBufferDecoderImpl decoder) {
        symbolEncoding = new LongSymbolEncoding(encoder, decoder);
        shortSymbolEncoding = new ShortSymbolEncoding(encoder, decoder);
        encoder.register(Symbol.class, this);
        decoder.register(this);
    }

    @Override
    public Class<Symbol> getTypeClass() {
        return Symbol.class;
    }

    @Override
    public SymbolEncoding getEncoding(final Symbol val) {
        return val.length() <= 255 ? shortSymbolEncoding : symbolEncoding;
    }

    @Override
    public SymbolEncoding getCanonicalEncoding() {
        return symbolEncoding;
    }

    @Override
    public Collection<SymbolEncoding> getAllEncodings() {
        return Arrays.asList(shortSymbolEncoding, symbolEncoding);
    }

    private class LongSymbolEncoding extends LargeFloatingSizePrimitiveTypeEncoding<Symbol> implements SymbolEncoding {

        public LongSymbolEncoding(final ProtonBufferEncoderImpl encoder, final ProtonBufferDecoderImpl decoder) {
            super(encoder, decoder);
        }

        @Override
        protected void writeEncodedValue(final Symbol val) {
            final int length = val.length();
            final ProtonBufferEncoderImpl encoder = getEncoder();

            for (int i = 0; i < length; i++) {
                encoder.writeRaw((byte) val.charAt(i));
            }
        }

        @Override
        protected int getEncodedValueSize(final Symbol val) {
            return val.length();
        }

        @Override
        public byte getEncodingCode() {
            return EncodingCodes.SYM32;
        }

        @Override
        public SymbolType getType() {
            return SymbolType.this;
        }

        @Override
        public boolean encodesSuperset(final TypeEncoding<Symbol> encoding) {
            return (getType() == encoding.getType());
        }

        @Override
        public Symbol readValue() {
            ProtonBufferDecoderImpl decoder = getDecoder();
            int size = decoder.readRawInt();
            return decoder.readRaw(symbolCreator, size);
        }
    }

    private class ShortSymbolEncoding extends SmallFloatingSizePrimitiveTypeEncoding<Symbol> implements SymbolEncoding {

        public ShortSymbolEncoding(final ProtonBufferEncoderImpl encoder, final ProtonBufferDecoderImpl decoder) {
            super(encoder, decoder);
        }

        @Override
        protected void writeEncodedValue(final Symbol val) {

            final int length = val.length();
            final ProtonBufferEncoderImpl encoder = getEncoder();

            for (int i = 0; i < length; i++) {
                encoder.writeRaw((byte) val.charAt(i));
            }
        }

        @Override
        protected int getEncodedValueSize(final Symbol val) {
            return val.length();
        }

        @Override
        public byte getEncodingCode() {
            return EncodingCodes.SYM8;
        }

        @Override
        public SymbolType getType() {
            return SymbolType.this;
        }

        @Override
        public boolean encodesSuperset(final TypeEncoding<Symbol> encoder) {
            return encoder == this;
        }

        @Override
        public Symbol readValue() {
            ProtonBufferDecoderImpl decoder = getDecoder();
            int size = (decoder.readRawByte()) & 0xff;
            return decoder.readRaw(symbolCreator, size);
        }
    }
}
