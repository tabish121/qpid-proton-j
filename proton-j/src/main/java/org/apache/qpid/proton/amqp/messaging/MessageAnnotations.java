
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


package org.apache.qpid.proton.amqp.messaging;

import java.util.Map;

import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.codec.ProtonType;


public final class MessageAnnotations implements Section, ProtonType
{
    private static final byte DESCRIPTOR_CODE = 0x72;

    private final Map<Symbol, Object> _value;

    public MessageAnnotations(Map<Symbol, Object> value)
    {
        _value = value;
    }

    public Map<Symbol, Object> getValue()
    {
        return _value;
    }

    @Override
    public String toString()
    {
        return "MessageAnnotations{" + _value + '}';
    }

    @Override
    public byte getDescriptorCode() {
        return DESCRIPTOR_CODE;
    }

    @Override
    public SectionType getType() {
        return SectionType.MessageAnnotations;
    }
}
