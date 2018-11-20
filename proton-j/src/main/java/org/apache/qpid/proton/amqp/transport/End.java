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
package org.apache.qpid.proton.amqp.transport;

import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.UnsignedLong;

public final class End implements FrameBody
{
    private static final UnsignedLong DESCRIPTOR = UnsignedLong.valueOf(0x0000000000000017L);

    private ErrorCondition _error;

    public End() {}

    public End(End other)
    {
        if (other._error != null)
        {
            this._error = new ErrorCondition();
            this._error.copyFrom(other._error);
        }
    }

    public ErrorCondition getError()
    {
        return _error;
    }

    public void setError(ErrorCondition error)
    {
        _error = error;
    }

    @Override
    public <E> void invoke(FrameBodyHandler<E> handler, Binary payload, E context)
    {
        handler.handleEnd(this, payload, context);
    }

    @Override
    public String toString()
    {
        return "End{" +
               "error=" + _error +
               '}';
    }

    @Override
    public End copy()
    {
        return new End(this);
    }

    @Override
    public byte getDescriptorCode()
    {
        return DESCRIPTOR.byteValue();
    }
}
