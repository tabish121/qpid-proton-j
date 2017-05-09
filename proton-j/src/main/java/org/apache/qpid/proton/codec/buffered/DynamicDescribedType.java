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
import java.util.HashMap;
import java.util.Map;

import org.apache.qpid.proton.amqp.DescribedType;
import org.apache.qpid.proton.codec.AMQPType;
import org.apache.qpid.proton.codec.EncodingCodes;
import org.apache.qpid.proton.codec.TypeEncoding;

public class DynamicDescribedType implements AMQPType<DescribedType> {

    private final ProtonBufferEncoderImpl encoder;
    private final Map<TypeEncoding, TypeEncoding> encodings = new HashMap<>();
    private final Object descriptor;

    public DynamicDescribedType(ProtonBufferEncoderImpl encoder, final Object descriptor) {
        this.encoder = encoder;
        this.descriptor = descriptor;
    }

    @Override
    public Class<DescribedType> getTypeClass() {
        return DescribedType.class;
    }

    @Override
    public TypeEncoding<DescribedType> getEncoding(final DescribedType val) {
        TypeEncoding underlyingEncoding = encoder.getType(val.getDescribed()).getEncoding(val.getDescribed());
        TypeEncoding encoding = encodings.get(underlyingEncoding);
        if (encoding == null) {
            encoding = new DynamicDescribedTypeEncoding(underlyingEncoding);
            encodings.put(underlyingEncoding, encoding);
        }

        return encoding;
    }

    @Override
    public TypeEncoding<DescribedType> getCanonicalEncoding() {
        return null;
    }

    @Override
    public Collection<TypeEncoding<DescribedType>> getAllEncodings() {
        Collection values = encodings.values();
        Collection unmodifiable = Collections.unmodifiableCollection(values);
        return unmodifiable;
    }

    @Override
    public void write(final DescribedType val) {
        TypeEncoding<DescribedType> encoding = getEncoding(val);
        encoding.writeConstructor();
        encoding.writeValue(val);
    }

    private class DynamicDescribedTypeEncoding implements TypeEncoding {
        private final TypeEncoding _underlyingEncoding;
        private final TypeEncoding _descriptorType;
        private final int _constructorSize;

        public DynamicDescribedTypeEncoding(final TypeEncoding underlyingEncoding) {
            _underlyingEncoding = underlyingEncoding;
            _descriptorType = encoder.getType(descriptor).getEncoding(descriptor);
            _constructorSize = 1 + _descriptorType.getConstructorSize() + _descriptorType.getValueSize(descriptor) + _underlyingEncoding.getConstructorSize();
        }

        @Override
        public AMQPType getType() {
            return DynamicDescribedType.this;
        }

        @Override
        public void writeConstructor() {
            encoder.writeRaw(EncodingCodes.DESCRIBED_TYPE_INDICATOR);
            _descriptorType.writeConstructor();
            _descriptorType.writeValue(descriptor);
            _underlyingEncoding.writeConstructor();
        }

        @Override
        public int getConstructorSize() {
            return _constructorSize;
        }

        @Override
        public void writeValue(final Object val) {
            _underlyingEncoding.writeValue(((DescribedType) val).getDescribed());
        }

        @Override
        public int getValueSize(final Object val) {
            return _underlyingEncoding.getValueSize(((DescribedType) val).getDescribed());
        }

        @Override
        public boolean isFixedSizeVal() {
            return _underlyingEncoding.isFixedSizeVal();
        }

        @Override
        public boolean encodesSuperset(final TypeEncoding encoding) {
            return (getType() == encoding.getType()) && (_underlyingEncoding.encodesSuperset(((DynamicDescribedTypeEncoding) encoding)._underlyingEncoding));
        }

        @Override
        public boolean encodesJavaPrimitive() {
            return false;
        }

    }
}
