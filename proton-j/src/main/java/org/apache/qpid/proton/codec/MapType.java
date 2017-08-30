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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class MapType extends AbstractPrimitiveType<Map>
{
    private final MapEncoding _mapEncoding;
    private final MapEncoding _shortMapEncoding;
    private EncoderImpl _encoder;

    private static interface MapEncoding extends PrimitiveTypeEncoding<Map>
    {
        void setValue(Map value, int length);
    }

    MapType(final EncoderImpl encoder, final DecoderImpl decoder)
    {
        _encoder = encoder;
        _mapEncoding = new AllMapEncoding(encoder, decoder);
        _shortMapEncoding = new ShortMapEncoding(encoder, decoder);
        encoder.register(Map.class, this);
        decoder.register(this);
    }

    @Override
    public Class<Map> getTypeClass()
    {
        return Map.class;
    }

    @Override
    public MapEncoding getEncoding(final Map val)
    {
        int calculatedSize = calculateSize(val, _encoder);
        MapEncoding encoding = (val.size() > 127 || calculatedSize >= 254)
                                    ? _mapEncoding
                                    : _shortMapEncoding;

        encoding.setValue(val, calculatedSize);
        return encoding;
    }

    private static int calculateSize(final Map map, EncoderImpl encoder)
    {
        int len = 0;
        Iterator<Map.Entry> iter = map.entrySet().iterator();

        AMQPType keyEncoder = null;
        AMQPType valueEncoder = null;

        while (iter.hasNext())
        {
            Entry entry = iter.next();

            Object key = entry.getKey();
            Object value = entry.getValue();

            if (keyEncoder == null || !keyEncoder.getTypeClass().equals(key.getClass()))
            {
                keyEncoder = encoder.getType(key);
            }

            if (value == null)
            {
                valueEncoder = encoder.getNullTypeEncoder();
            }
            else if (valueEncoder == null || !valueEncoder.getTypeClass().equals(value.getClass()))
            {
                valueEncoder = encoder.getType(value);
            }

            TypeEncoding elementEncoding = keyEncoder.getEncoding(key);
            len += elementEncoding.getConstructorSize()+elementEncoding.getValueSize(key);
            elementEncoding = valueEncoder.getEncoding(value);
            len += elementEncoding.getConstructorSize()+elementEncoding.getValueSize(value);

        }
        return len;
    }

    @Override
    public MapEncoding getCanonicalEncoding()
    {
        return _mapEncoding;
    }

    @Override
    public Collection<MapEncoding> getAllEncodings()
    {
        return Arrays.asList(_shortMapEncoding, _mapEncoding);
    }

    private class AllMapEncoding
            extends LargeFloatingSizePrimitiveTypeEncoding<Map>
            implements MapEncoding
    {
        private Map _value;
        private int _length;

        public AllMapEncoding(final EncoderImpl encoder, final DecoderImpl decoder)
        {
            super(encoder, decoder);
        }

        @Override
        protected void writeEncodedValue(final Map map)
        {
            getEncoder().writeRaw(2 * map.size());

            Iterator<Map.Entry> iter = map.entrySet().iterator();

            AMQPType keyEncoder = null;
            AMQPType valueEncoder = null;

            while (iter.hasNext())
            {
                Entry entry = iter.next();

                Object key = entry.getKey();
                Object value = entry.getValue();

                if (keyEncoder == null || !keyEncoder.getTypeClass().equals(key.getClass()))
                {
                    keyEncoder = getEncoder().getType(key);
                }

                if (value == null)
                {
                    valueEncoder = getEncoder().getNullTypeEncoder();
                }
                else if (valueEncoder == null || !valueEncoder.getTypeClass().equals(value.getClass()))
                {
                    valueEncoder = getEncoder().getType(value);
                }

                TypeEncoding elementEncoding = keyEncoder.getEncoding(key);
                elementEncoding.writeConstructor();
                elementEncoding.writeValue(key);
                elementEncoding = valueEncoder.getEncoding(value);
                elementEncoding.writeConstructor();
                elementEncoding.writeValue(value);
            }
        }

        @Override
        protected int getEncodedValueSize(final Map val)
        {
            return 4 + ((val == _value) ? _length : calculateSize(val, getEncoder()));
        }

        @Override
        public byte getEncodingCode()
        {
            return EncodingCodes.MAP32;
        }

        @Override
        public MapType getType()
        {
            return MapType.this;
        }

        @Override
        public boolean encodesSuperset(final TypeEncoding<Map> encoding)
        {
            return (getType() == encoding.getType());
        }

        @Override
        public Map readValue()
        {
            DecoderImpl decoder = getDecoder();
            int size = decoder.readRawInt();
            // todo - limit the decoder with size
            int count = decoder.readRawInt();
            if (count > decoder.getByteBufferRemaining()) {
                throw new IllegalArgumentException("Map element count "+count+" is specified to be greater than the amount of data available ("+
                                                   decoder.getByteBufferRemaining()+")");
            }
            Map map = new LinkedHashMap(count);
            for(int i = 0; i < count; i++)
            {
                Object key = decoder.readObject();
                i++;
                Object value = decoder.readObject();
                map.put(key, value);
            }
            return map;
        }

        @Override
        public void setValue(final Map value, final int length)
        {
            _value = value;
            _length = length;
        }
    }

    private class ShortMapEncoding
            extends SmallFloatingSizePrimitiveTypeEncoding<Map>
            implements MapEncoding
    {
        private Map _value;
        private int _length;

        public ShortMapEncoding(final EncoderImpl encoder, final DecoderImpl decoder)
        {
            super(encoder, decoder);
        }

        @Override
        protected void writeEncodedValue(final Map map)
        {
            getEncoder().writeRaw((byte)(2 * map.size()));

            AMQPType keyEncoder = null;
            AMQPType valueEncoder = null;

            Iterator<Map.Entry> iter = map.entrySet().iterator();
            while (iter.hasNext())
            {
                Entry entry = iter.next();

                Object key = entry.getKey();
                Object value = entry.getValue();

                if (keyEncoder == null || !keyEncoder.getTypeClass().equals(key.getClass())) {
                    keyEncoder = getEncoder().getType(key);
                }

                if (value == null)
                {
                    valueEncoder = getEncoder().getNullTypeEncoder();
                }
                else if (valueEncoder == null || !valueEncoder.getTypeClass().equals(value.getClass()))
                {
                    valueEncoder = getEncoder().getType(value);
                }

                TypeEncoding elementEncoding = keyEncoder.getEncoding(key);
                elementEncoding.writeConstructor();
                elementEncoding.writeValue(key);
                elementEncoding = valueEncoder.getEncoding(value);
                elementEncoding.writeConstructor();
                elementEncoding.writeValue(value);
            }
        }

        @Override
        protected int getEncodedValueSize(final Map val)
        {
            return 1 + ((val == _value) ? _length : calculateSize(val, getEncoder()));
        }

        @Override
        public byte getEncodingCode()
        {
            return EncodingCodes.MAP8;
        }

        @Override
        public MapType getType()
        {
            return MapType.this;
        }

        @Override
        public boolean encodesSuperset(final TypeEncoding<Map> encoder)
        {
            return encoder == this;
        }

        @Override
        public Map readValue()
        {
            DecoderImpl decoder = getDecoder();
            int size = (decoder.readRawByte()) & 0xff;
            // todo - limit the decoder with size
            int count = (decoder.readRawByte()) & 0xff;

            Map map = new LinkedHashMap(count);
            for(int i = 0; i < count; i++)
            {
                Object key = decoder.readObject();
                i++;
                Object value = decoder.readObject();
                map.put(key, value);
            }
            return map;
        }

        @Override
        public void setValue(final Map value, final int length)
        {
            _value = value;
            _length = length;
        }
    }
}
