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

abstract class FloatingSizePrimitiveTypeEncoding<T> extends AbstractPrimitiveTypeEncoding<T> {

    FloatingSizePrimitiveTypeEncoding(final ProtonBufferEncoderImpl encoder, final ProtonBufferDecoderImpl decoder) {
        super(encoder, decoder);
    }

    @Override
    public final boolean isFixedSizeVal() {
        return false;
    }

    @Override
    public void writeValue(final T val) {
        writeSize(val);
        writeEncodedValue(val);
    }

    @Override
    public int getValueSize(final T val) {
        return getSizeBytes() + getEncodedValueSize(val);
    }

    abstract int getSizeBytes();

    protected abstract void writeEncodedValue(final T val);

    protected abstract void writeSize(final T val);

    protected abstract int getEncodedValueSize(final T val);

}
