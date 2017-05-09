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

import org.apache.qpid.proton.codec.PrimitiveTypeEncoding;

abstract class AbstractPrimitiveTypeEncoding<T> implements PrimitiveTypeEncoding<T> {

    private final ProtonBufferEncoderImpl encoder;
    private final ProtonBufferDecoderImpl decoder;

    AbstractPrimitiveTypeEncoding(final ProtonBufferEncoderImpl encoder, final ProtonBufferDecoderImpl decoder) {
        this.encoder = encoder;
        this.decoder = decoder;
    }

    @Override
    public final void writeConstructor() {
        encoder.writeRaw(getEncodingCode());
    }

    @Override
    public int getConstructorSize() {
        return 1;
    }

    @Override
    public abstract byte getEncodingCode();

    protected ProtonBufferEncoderImpl getEncoder() {
        return encoder;
    }

    @Override
    public Class<T> getTypeClass() {
        return getType().getTypeClass();
    }

    protected ProtonBufferDecoderImpl getDecoder() {
        return decoder;
    }

    @Override
    public boolean encodesJavaPrimitive() {
        return false;
    }
}
