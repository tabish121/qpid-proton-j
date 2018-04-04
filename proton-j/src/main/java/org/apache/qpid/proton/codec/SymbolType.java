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
package org.apache.qpid.proton.codec;

import org.apache.qpid.proton.amqp.Symbol;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SymbolType extends AbstractPrimitiveType<Symbol>
{
    private static final Charset ASCII_CHARSET = Charset.forName("US-ASCII");
    private final SymbolEncoding _symbolEncoding;
    private final SymbolEncoding _shortSymbolEncoding;

    private final Map<ReadableBuffer, Symbol> _symbolCache = new HashMap<ReadableBuffer, Symbol>();
    private DecoderImpl.TypeDecoder<Symbol> _symbolCreator =
        new DecoderImpl.TypeDecoder<Symbol>()
        {
            @Override
            public Symbol decode(DecoderImpl decoder, ReadableBuffer buffer)
            {
                Symbol symbol = _symbolCache.get(buffer);
                if (symbol == null)
                {
                    byte[] bytes = new byte[buffer.limit()];
                    buffer.get(bytes);

                    String str = new String(bytes, ASCII_CHARSET);
                    symbol = Symbol.getSymbol(str);

                    _symbolCache.put(ReadableBuffer.ByteBufferReader.wrap(bytes), symbol);
                }
                return symbol;
            }
        };

    public static interface SymbolEncoding extends PrimitiveTypeEncoding<Symbol>
    {

    }

    SymbolType(final EncoderImpl encoder, final DecoderImpl decoder)
    {
        _symbolEncoding =  new LongSymbolEncoding(encoder, decoder);
        _shortSymbolEncoding = new ShortSymbolEncoding(encoder, decoder);
        encoder.register(Symbol.class, this);
        decoder.register(this);
    }

    public Class<Symbol> getTypeClass()
    {
        return Symbol.class;
    }

    public void fastWrite(EncoderImpl encoder, Symbol symbol)
    {
        if (symbol.length() <= 255)
        {
            encoder.writeRaw(EncodingCodes.SYM8);
            encoder.writeRaw((byte) symbol.length());
            symbol.writeTo(encoder.getBuffer());
        }
        else
        {
            encoder.writeRaw(EncodingCodes.SYM32);
            encoder.writeRaw(symbol.length());
            symbol.writeTo(encoder.getBuffer());
        }
    }

    public SymbolEncoding getEncoding(final Symbol val)
    {
        return val.length() <= 255 ? _shortSymbolEncoding : _symbolEncoding;
    }

    public SymbolEncoding getCanonicalEncoding()
    {
        return _symbolEncoding;
    }

    public Collection<SymbolEncoding> getAllEncodings()
    {
        return Arrays.asList(_shortSymbolEncoding, _symbolEncoding);
    }

    private class LongSymbolEncoding
            extends LargeFloatingSizePrimitiveTypeEncoding<Symbol>
            implements SymbolEncoding
    {

        public LongSymbolEncoding(final EncoderImpl encoder, final DecoderImpl decoder)
        {
            super(encoder, decoder);
        }

        @Override
        protected void writeEncodedValue(final Symbol val)
        {
            val.writeTo(getEncoder().getBuffer());
        }

        @Override
        protected int getEncodedValueSize(final Symbol val)
        {
            return val.length();
        }


        @Override
        public byte getEncodingCode()
        {
            return EncodingCodes.SYM32;
        }

        public SymbolType getType()
        {
            return SymbolType.this;
        }

        public boolean encodesSuperset(final TypeEncoding<Symbol> encoding)
        {
            return (getType() == encoding.getType());
        }

        public Symbol readValue()
        {
            DecoderImpl decoder = getDecoder();
            int size = decoder.readRawInt();
            return decoder.readRaw(_symbolCreator, size);
        }

        public void skipValue()
        {
            DecoderImpl decoder = getDecoder();
            ReadableBuffer buffer = decoder.getBuffer();
            int size = decoder.readRawInt();
            buffer.position(buffer.position() + size);
        }
    }

    private class ShortSymbolEncoding
            extends SmallFloatingSizePrimitiveTypeEncoding<Symbol>
            implements SymbolEncoding
    {

        public ShortSymbolEncoding(final EncoderImpl encoder, final DecoderImpl decoder)
        {
            super(encoder, decoder);
        }

        @Override
        protected void writeEncodedValue(final Symbol val)
        {
            val.writeTo(getEncoder().getBuffer());
        }

        @Override
        protected int getEncodedValueSize(final Symbol val)
        {
            return val.length();
        }


        @Override
        public byte getEncodingCode()
        {
            return EncodingCodes.SYM8;
        }

        public SymbolType getType()
        {
            return SymbolType.this;
        }

        public boolean encodesSuperset(final TypeEncoding<Symbol> encoder)
        {
            return encoder == this;
        }

        public Symbol readValue()
        {
            DecoderImpl decoder = getDecoder();
            int size = ((int)decoder.readRawByte()) & 0xff;
            return decoder.readRaw(_symbolCreator, size);
        }

        public void skipValue()
        {
            DecoderImpl decoder = getDecoder();
            ReadableBuffer buffer = decoder.getBuffer();
            int size = ((int)decoder.readRawByte()) & 0xff;
            buffer.position(buffer.position() + size);
        }
    }
}
