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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.qpid.proton.codec.AMQPType;
import org.apache.qpid.proton.codec.EncodingCodes;
import org.apache.qpid.proton.codec.PrimitiveTypeEncoding;
import org.apache.qpid.proton.codec.TypeEncoding;

public class ListType extends AbstractPrimitiveType<List>
{
    private final ListEncoding listEncoding;
    private final ListEncoding shortListEncoding;
    private final ListEncoding zeroListEncoding;
    private ProtonBufferEncoderImpl encoder;

    private static interface ListEncoding extends PrimitiveTypeEncoding<List>
    {
        void setValue(List value, int length);
    }

    ListType(final ProtonBufferEncoderImpl encoder, final ProtonBufferDecoderImpl decoder)
    {
        this.encoder = encoder;
        listEncoding = new AllListEncoding(encoder, decoder);
        shortListEncoding = new ShortListEncoding(encoder, decoder);
        zeroListEncoding = new ZeroListEncoding(encoder, decoder);
        encoder.register(List.class, this);
        decoder.register(this);
    }

    @Override
    public Class<List> getTypeClass()
    {
        return List.class;
    }

    @Override
    public ListEncoding getEncoding(final List val)
    {

        int calculatedSize = calculateSize(val, encoder);
        ListEncoding encoding = val.isEmpty()
                                    ? zeroListEncoding
                                    : (val.size() > 255 || calculatedSize >= 254)
                                        ? listEncoding
                                        : shortListEncoding;

        encoding.setValue(val, calculatedSize);
        return encoding;
    }

    private static int calculateSize(final List val, ProtonBufferEncoderImpl encoder)
    {
        int len = 0;
        final int count = val.size();

        for(int i = 0; i < count; i++)
        {
            Object element = val.get(i);
            AMQPType type = encoder.getType(element);
            if(type == null)
            {
                throw new IllegalArgumentException("No encoding defined for type: " + element.getClass());
            }
            TypeEncoding elementEncoding = type.getEncoding(element);
            len += elementEncoding.getConstructorSize()+elementEncoding.getValueSize(element);
        }
        return len;
    }


    @Override
    public ListEncoding getCanonicalEncoding()
    {
        return listEncoding;
    }

    @Override
    public Collection<ListEncoding> getAllEncodings()
    {
        return Arrays.asList(zeroListEncoding, shortListEncoding, listEncoding);
    }

    private class AllListEncoding
            extends LargeFloatingSizePrimitiveTypeEncoding<List>
            implements ListEncoding
    {

        private List _value;
        private int _length;

        public AllListEncoding(final ProtonBufferEncoderImpl encoder, final ProtonBufferDecoderImpl decoder)
        {
            super(encoder, decoder);
        }

        @Override
        protected void writeEncodedValue(final List val)
        {
            getEncoder().writeRaw(val.size());

            final int count = val.size();

            for(int i = 0; i < count; i++)
            {
                Object element = val.get(i);
                TypeEncoding elementEncoding = getEncoder().getType(element).getEncoding(element);
                elementEncoding.writeConstructor();
                elementEncoding.writeValue(element);
            }
        }

        @Override
        protected int getEncodedValueSize(final List val)
        {
            return 4 + ((val == _value) ? _length : calculateSize(val, getEncoder()));
        }


        @Override
        public byte getEncodingCode()
        {
            return EncodingCodes.LIST32;
        }

        @Override
        public ListType getType()
        {
            return ListType.this;
        }

        @Override
        public boolean encodesSuperset(final TypeEncoding<List> encoding)
        {
            return (getType() == encoding.getType());
        }

        @Override
        public List readValue()
        {
            ProtonBufferDecoderImpl decoder = getDecoder();
            int size = decoder.readRawInt();
            // todo - limit the decoder with size
            int count = decoder.readRawInt();
            // Ensure we do not allocate an array of size greater then the available data, otherwise there is a risk for an OOM error
            if (count > decoder.getByteBufferRemaining()) {
                throw new IllegalArgumentException("List element count "+count+" is specified to be greater than the amount of data available ("+
                                                   decoder.getByteBufferRemaining()+")");
            }
            List list = new ArrayList(count);
            for(int i = 0; i < count; i++)
            {
                list.add(decoder.readObject());
            }
            return list;
        }

        @Override
        public void setValue(final List value, final int length)
        {
            _value = value;
            _length = length;
        }
    }

    private class ShortListEncoding
            extends SmallFloatingSizePrimitiveTypeEncoding<List>
            implements ListEncoding
    {

        private List _value;
        private int _length;

        public ShortListEncoding(final ProtonBufferEncoderImpl encoder, final ProtonBufferDecoderImpl decoder)
        {
            super(encoder, decoder);
        }

        @Override
        protected void writeEncodedValue(final List val)
        {
            getEncoder().writeRaw((byte)val.size());

            final int count = val.size();

            for(int i = 0; i < count; i++)
            {
                Object element = val.get(i);
                TypeEncoding elementEncoding = getEncoder().getType(element).getEncoding(element);
                elementEncoding.writeConstructor();
                elementEncoding.writeValue(element);
            }
        }

        @Override
        protected int getEncodedValueSize(final List val)
        {
            return 1 + ((val == _value) ? _length : calculateSize(val, getEncoder()));
        }


        @Override
        public byte getEncodingCode()
        {
            return EncodingCodes.LIST8;
        }

        @Override
        public ListType getType()
        {
            return ListType.this;
        }

        @Override
        public boolean encodesSuperset(final TypeEncoding<List> encoder)
        {
            return encoder == this;
        }

        @Override
        public List readValue()
        {

            ProtonBufferDecoderImpl decoder = getDecoder();
            int size = (decoder.readRawByte()) & 0xff;
            // todo - limit the decoder with size
            int count = (decoder.readRawByte()) & 0xff;
            List list = new ArrayList(count);
            for(int i = 0; i < count; i++)
            {
                list.add(decoder.readObject());
            }
            return list;
        }

        @Override
        public void setValue(final List value, final int length)
        {
            _value = value;
            _length = length;
        }
    }


    private class ZeroListEncoding
            extends FixedSizePrimitiveTypeEncoding<List>
            implements ListEncoding
    {
        public ZeroListEncoding(final ProtonBufferEncoderImpl encoder, final ProtonBufferDecoderImpl decoder)
        {
            super(encoder, decoder);
        }

        @Override
        public byte getEncodingCode()
        {
            return EncodingCodes.LIST0;
        }

        @Override
        protected int getFixedSize()
        {
            return 0;
        }


        @Override
        public ListType getType()
        {
           return ListType.this;
        }

        @Override
        public void setValue(List value, int length)
        {
        }

        @Override
        public void writeValue(final List val)
        {
        }

        @Override
        public boolean encodesSuperset(final TypeEncoding<List> encoder)
        {
            return encoder == this;
        }

        @Override
        public List readValue()
        {
            return Collections.EMPTY_LIST;
        }


    }
}
