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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.qpid.proton.codec.EncodingCodes;
import org.apache.qpid.proton.codec.PrimitiveTypeEncoding;
import org.apache.qpid.proton.codec.TypeEncoding;

public class MapType extends AbstractPrimitiveType<Map> {
    private final MapEncoding mapEncoding;
    private final MapEncoding shortMapEncoding;
    private ProtonBufferEncoderImpl encoder;

    private static interface MapEncoding extends PrimitiveTypeEncoding<Map> {
        void setValue(Map value, int length);
    }

    MapType(final ProtonBufferEncoderImpl encoder, final ProtonBufferDecoderImpl decoder) {
        this.encoder = encoder;

        mapEncoding = new AllMapEncoding(encoder, decoder);
        shortMapEncoding = new ShortMapEncoding(encoder, decoder);
        encoder.register(Map.class, this);
        decoder.register(this);
    }

    @Override
    public Class<Map> getTypeClass() {
        return Map.class;
    }

    @Override
    public MapEncoding getEncoding(final Map val) {

        int calculatedSize = calculateSize(val, encoder);
        MapEncoding encoding = (val.size() > 127 || calculatedSize >= 254) ? mapEncoding : shortMapEncoding;

        encoding.setValue(val, calculatedSize);
        return encoding;
    }

    private static int calculateSize(final Map val, ProtonBufferEncoderImpl encoder) {
        int len = 0;
        Iterator<Map.Entry> iter = val.entrySet().iterator();

        while (iter.hasNext()) {
            Map.Entry element = iter.next();
            TypeEncoding elementEncoding = encoder.getType(element.getKey()).getEncoding(element.getKey());
            len += elementEncoding.getConstructorSize() + elementEncoding.getValueSize(element.getKey());
            elementEncoding = encoder.getType(element.getValue()).getEncoding(element.getValue());
            len += elementEncoding.getConstructorSize() + elementEncoding.getValueSize(element.getValue());

        }
        return len;
    }

    @Override
    public MapEncoding getCanonicalEncoding() {
        return mapEncoding;
    }

    @Override
    public Collection<MapEncoding> getAllEncodings() {
        return Arrays.asList(shortMapEncoding, mapEncoding);
    }

    private class AllMapEncoding extends LargeFloatingSizePrimitiveTypeEncoding<Map> implements MapEncoding {

        private Map _value;
        private int _length;

        public AllMapEncoding(final ProtonBufferEncoderImpl encoder, final ProtonBufferDecoderImpl decoder) {
            super(encoder, decoder);
        }

        @Override
        protected void writeEncodedValue(final Map val) {
            getEncoder().writeRaw(2 * val.size());

            Iterator<Map.Entry> iter = val.entrySet().iterator();

            while (iter.hasNext()) {
                Map.Entry element = iter.next();
                TypeEncoding elementEncoding = getEncoder().getType(element.getKey()).getEncoding(element.getKey());
                elementEncoding.writeConstructor();
                elementEncoding.writeValue(element.getKey());
                elementEncoding = getEncoder().getType(element.getValue()).getEncoding(element.getValue());
                elementEncoding.writeConstructor();
                elementEncoding.writeValue(element.getValue());
            }
        }

        @Override
        protected int getEncodedValueSize(final Map val) {
            return 4 + ((val == _value) ? _length : calculateSize(val, getEncoder()));
        }

        @Override
        public byte getEncodingCode() {
            return EncodingCodes.MAP32;
        }

        @Override
        public MapType getType() {
            return MapType.this;
        }

        @Override
        public boolean encodesSuperset(final TypeEncoding<Map> encoding) {
            return (getType() == encoding.getType());
        }

        @Override
        public Map readValue() {

            ProtonBufferDecoderImpl decoder = getDecoder();
            int size = decoder.readRawInt();
            // todo - limit the decoder with size
            int count = decoder.readRawInt();
            if (count > decoder.getByteBufferRemaining()) {
                throw new IllegalArgumentException(
                    "Map element count " + count + " is specified to be greater than the amount of data available (" + decoder.getByteBufferRemaining() + ")");
            }
            Map map = new LinkedHashMap(count);
            for (int i = 0; i < count; i++) {
                Object key = decoder.readObject();
                i++;
                Object value = decoder.readObject();
                map.put(key, value);
            }
            return map;
        }

        @Override
        public void setValue(final Map value, final int length) {
            _value = value;
            _length = length;
        }
    }

    private class ShortMapEncoding extends SmallFloatingSizePrimitiveTypeEncoding<Map> implements MapEncoding {

        private Map _value;
        private int _length;

        public ShortMapEncoding(final ProtonBufferEncoderImpl encoder, final ProtonBufferDecoderImpl decoder) {
            super(encoder, decoder);
        }

        @Override
        protected void writeEncodedValue(final Map val) {
            getEncoder().writeRaw((byte) (2 * val.size()));

            Iterator<Map.Entry> iter = val.entrySet().iterator();

            while (iter.hasNext()) {
                Map.Entry element = iter.next();
                TypeEncoding elementEncoding = getEncoder().getType(element.getKey()).getEncoding(element.getKey());
                elementEncoding.writeConstructor();
                elementEncoding.writeValue(element.getKey());
                elementEncoding = getEncoder().getType(element.getValue()).getEncoding(element.getValue());
                elementEncoding.writeConstructor();
                elementEncoding.writeValue(element.getValue());
            }
        }

        @Override
        protected int getEncodedValueSize(final Map val) {
            return 1 + ((val == _value) ? _length : calculateSize(val, getEncoder()));
        }

        @Override
        public byte getEncodingCode() {
            return EncodingCodes.MAP8;
        }

        @Override
        public MapType getType() {
            return MapType.this;
        }

        @Override
        public boolean encodesSuperset(final TypeEncoding<Map> encoder) {
            return encoder == this;
        }

        @Override
        public Map readValue() {
            ProtonBufferDecoderImpl decoder = getDecoder();
            int size = (decoder.readRawByte()) & 0xff;
            // todo - limit the decoder with size
            int count = (decoder.readRawByte()) & 0xff;

            Map map = new LinkedHashMap(count);
            for (int i = 0; i < count; i++) {
                Object key = decoder.readObject();
                i++;
                Object value = decoder.readObject();
                map.put(key, value);
            }
            return map;
        }

        @Override
        public void setValue(final Map value, final int length) {
            _value = value;
            _length = length;
        }
    }
}
