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

import org.apache.qpid.proton.amqp.UnsignedLong;
import org.apache.qpid.proton.codec.AMQPType;
import org.apache.qpid.proton.codec.EncodingCodes;
import org.apache.qpid.proton.codec.TypeEncoding;

abstract public class AbstractDescribedType<T, M> implements AMQPType<T> {

    private final ProtonBufferEncoderImpl encoder;
    private final Map<TypeEncoding<M>, TypeEncoding<T>> encodings = new HashMap<>();

    public AbstractDescribedType(ProtonBufferEncoderImpl encoder) {
        this.encoder = encoder;
    }

    abstract protected UnsignedLong getDescriptor();

    @Override
    public TypeEncoding<T> getEncoding(final T val) {
        M asUnderlying = wrap(val);
        TypeEncoding<M> underlyingEncoding = encoder.getType(asUnderlying).getEncoding(asUnderlying);
        TypeEncoding<T> encoding = encodings.get(underlyingEncoding);
        if (encoding == null) {
            encoding = new DynamicDescribedTypeEncoding(underlyingEncoding);
            encodings.put(underlyingEncoding, encoding);
        }

        return encoding;
    }

    abstract protected M wrap(T val);

    @Override
    public TypeEncoding<T> getCanonicalEncoding() {
        return null;
    }

    @Override
    public Collection<TypeEncoding<T>> getAllEncodings() {
        Collection values = encodings.values();
        Collection unmodifiable = Collections.unmodifiableCollection(values);
        return unmodifiable;
    }

    @Override
    public void write(final T val) {
        TypeEncoding<T> encoding = getEncoding(val);
        encoding.writeConstructor();
        encoding.writeValue(val);
    }

    private class DynamicDescribedTypeEncoding implements TypeEncoding<T> {
        private final TypeEncoding<M> _underlyingEncoding;
        private final TypeEncoding<UnsignedLong> _descriptorType;
        private final int _constructorSize;

        public DynamicDescribedTypeEncoding(final TypeEncoding<M> underlyingEncoding) {
            _underlyingEncoding = underlyingEncoding;
            _descriptorType = encoder.getType(getDescriptor()).getEncoding(getDescriptor());
            _constructorSize = 1 + _descriptorType.getConstructorSize() + _descriptorType.getValueSize(getDescriptor())
                + _underlyingEncoding.getConstructorSize();
        }

        @Override
        public AMQPType<T> getType() {
            return AbstractDescribedType.this;
        }

        @Override
        public void writeConstructor() {
            encoder.writeRaw(EncodingCodes.DESCRIBED_TYPE_INDICATOR);
            _descriptorType.writeConstructor();
            _descriptorType.writeValue(getDescriptor());
            _underlyingEncoding.writeConstructor();
        }

        @Override
        public int getConstructorSize() {
            return _constructorSize;
        }

        @Override
        public void writeValue(final T val) {
            _underlyingEncoding.writeValue(wrap(val));
        }

        @Override
        public int getValueSize(final T val) {
            return _underlyingEncoding.getValueSize(wrap(val));
        }

        @Override
        public boolean isFixedSizeVal() {
            return _underlyingEncoding.isFixedSizeVal();
        }

        @Override
        public boolean encodesSuperset(final TypeEncoding<T> encoding) {
            return (getType() == encoding.getType()) && (_underlyingEncoding.encodesSuperset(((DynamicDescribedTypeEncoding) encoding)._underlyingEncoding));
        }

        @Override
        public boolean encodesJavaPrimitive() {
            return false;
        }
    }
}
