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
import java.util.Date;

import org.apache.qpid.proton.codec.EncodingCodes;
import org.apache.qpid.proton.codec.TypeEncoding;

public class TimestampType extends AbstractPrimitiveType<Date> {
    private TimestampEncoding timestampEncoding;

    TimestampType(final ProtonBufferEncoderImpl encoder, final ProtonBufferDecoderImpl decoder) {
        timestampEncoding = new TimestampEncoding(encoder, decoder);
        encoder.register(Date.class, this);
        decoder.register(this);
    }

    @Override
    public Class<Date> getTypeClass() {
        return Date.class;
    }

    @Override
    public TimestampEncoding getEncoding(final Date val) {
        return timestampEncoding;
    }

    @Override
    public TimestampEncoding getCanonicalEncoding() {
        return timestampEncoding;
    }

    @Override
    public Collection<TimestampEncoding> getAllEncodings() {
        return Collections.singleton(timestampEncoding);
    }

    public void write(long l) {
        timestampEncoding.write(l);
    }

    private class TimestampEncoding extends FixedSizePrimitiveTypeEncoding<Date> {

        public TimestampEncoding(final ProtonBufferEncoderImpl encoder, final ProtonBufferDecoderImpl decoder) {
            super(encoder, decoder);
        }

        @Override
        protected int getFixedSize() {
            return 8;
        }

        @Override
        public byte getEncodingCode() {
            return EncodingCodes.TIMESTAMP;
        }

        @Override
        public TimestampType getType() {
            return TimestampType.this;
        }

        @Override
        public void writeValue(final Date val) {
            getEncoder().writeRaw(val.getTime());
        }

        public void write(final long l) {
            writeConstructor();
            getEncoder().writeRaw(l);
        }

        @Override
        public boolean encodesSuperset(final TypeEncoding<Date> encoding) {
            return (getType() == encoding.getType());
        }

        @Override
        public Date readValue() {
            return new Date(getDecoder().readRawLong());
        }
    }
}
