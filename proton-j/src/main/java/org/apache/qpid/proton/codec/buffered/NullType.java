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

public final class NullType extends AbstractPrimitiveType<Void>
{
    private NullEncoding nullEncoding;

    NullType(final ProtonBufferEncoderImpl encoder, final ProtonBufferDecoderImpl decoder)
    {
        nullEncoding = new NullEncoding(encoder, decoder);
        encoder.register(Void.class, this);
        decoder.register(this);
    }

    @Override
    public Class<Void> getTypeClass()
    {
        return Void.class;
    }

    @Override
    public NullEncoding getEncoding(final Void val)
    {
        return nullEncoding;
    }


    @Override
    public NullEncoding getCanonicalEncoding()
    {
        return nullEncoding;
    }

    @Override
    public Collection<NullEncoding> getAllEncodings()
    {
        return Collections.singleton(nullEncoding);
    }

    public void write()
    {
        nullEncoding.write();
    }

    private class NullEncoding extends FixedSizePrimitiveTypeEncoding<Void>
    {

        public NullEncoding(final ProtonBufferEncoderImpl encoder, final ProtonBufferDecoderImpl decoder)
        {
            super(encoder, decoder);
        }

        @Override
        protected int getFixedSize()
        {
            return 0;
        }

        @Override
        public byte getEncodingCode()
        {
            return EncodingCodes.NULL;
        }

        @Override
        public NullType getType()
        {
            return NullType.this;
        }

        @Override
        public void writeValue(final Void val)
        {
        }

        public void writeValue()
        {
        }

        @Override
        public boolean encodesSuperset(final TypeEncoding<Void> encoding)
        {
            return encoding == this;
        }

        @Override
        public Void readValue()
        {
            return null;
        }

        public void write()
        {
            writeConstructor();
        }
    }
}
