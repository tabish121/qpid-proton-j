/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.qpid.proton.codec.transport;

import java.util.Collection;

import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.UnsignedLong;
import org.apache.qpid.proton.amqp.transport.Flow;
import org.apache.qpid.proton.codec.AMQPType;
import org.apache.qpid.proton.codec.BuiltinDescribedTypeConstructor;
import org.apache.qpid.proton.codec.DecodeException;
import org.apache.qpid.proton.codec.Decoder;
import org.apache.qpid.proton.codec.DecoderImpl;
import org.apache.qpid.proton.codec.EncoderImpl;
import org.apache.qpid.proton.codec.EncodingCodes;
import org.apache.qpid.proton.codec.TypeEncoding;
import org.apache.qpid.proton.codec.WritableBuffer;

public class BuiltinFlowType implements AMQPType<Flow>, BuiltinDescribedTypeConstructor<Flow> {

    private static final Object[] DESCRIPTORS =
    {
        UnsignedLong.valueOf(0x0000000000000013L), Symbol.valueOf("amqp:flow:list"),
    };

    private final FlowType flowType;

    public BuiltinFlowType(EncoderImpl encoder) {
        this.flowType = new FlowType(encoder);
    }

    public EncoderImpl getEncoder() {
        return flowType.getEncoder();
    }

    public DecoderImpl getDecoder() {
        return flowType.getDecoder();
    }

    @Override
    public boolean encodesJavaPrimitive() {
        return false;
    }

    @Override
    public Class<Flow> getTypeClass() {
        return Flow.class;
    }

    @Override
    public TypeEncoding<Flow> getEncoding(Flow flow) {
        return flowType.getEncoding(flow);
    }

    @Override
    public TypeEncoding<Flow> getCanonicalEncoding() {
        return flowType.getCanonicalEncoding();
    }

    @Override
    public Collection<? extends TypeEncoding<Flow>> getAllEncodings() {
        return flowType.getAllEncodings();
    }

    @Override
    public Flow readValue() {
        DecoderImpl decoder = getDecoder();
        byte typeCode = decoder.getByteBuffer().get();

        @SuppressWarnings("unused")
        int size = 0;
        int count = 0;

        switch (typeCode) {
            case EncodingCodes.LIST0:
                break;
            case EncodingCodes.LIST8:
                size = ((int)decoder.getByteBuffer().get()) & 0xff;
                count = ((int)decoder.getByteBuffer().get()) & 0xff;
                break;
            case EncodingCodes.LIST32:
                size = decoder.getByteBuffer().getInt();
                count = decoder.getByteBuffer().getInt();
                break;
            default:
                throw new DecodeException("Incorrect type found in Flow encoding: " + typeCode);
        }

        Flow flow = new Flow();

        for (int index = 0; index < count; ++index) {
            switch (index) {
                case 0:
                    flow.setNextIncomingId(decoder.readUnsignedInteger());
                    break;
                case 1:
                    flow.setIncomingWindow(decoder.readUnsignedInteger());
                    break;
                case 2:
                    flow.setNextOutgoingId(decoder.readUnsignedInteger());
                    break;
                case 3:
                    flow.setOutgoingWindow(decoder.readUnsignedInteger());
                    break;
                case 4:
                    flow.setHandle(decoder.readUnsignedInteger());
                    break;
                case 5:
                    flow.setDeliveryCount(decoder.readUnsignedInteger());
                    break;
                case 6:
                    flow.setLinkCredit(decoder.readUnsignedInteger());
                    break;
                case 7:
                    flow.setAvailable(decoder.readUnsignedInteger());
                    break;
                case 8:
                    flow.setDrain(decoder.readBoolean(false));
                    break;
                case 9:
                    flow.setDrain(decoder.readBoolean(false));
                    break;
                case 10:
                    flow.setProperties(decoder.readMap());
                    break;
                default:
                    throw new IllegalStateException("To many entries in Flow encoding");
            }
        }

        return flow;
    }

    @Override
    public void write(Flow flow) {
        WritableBuffer buffer = getEncoder().getBuffer();
        int count = getElementCount(flow);
        byte encodingCode = deduceEncodingCode(flow, count);

        buffer.put(EncodingCodes.DESCRIBED_TYPE_INDICATOR);
        getEncoder().writeUnsignedLong(flowType.getDescriptor());

        // Optimized step, no other data to be written.
        if (count == 0 || encodingCode == EncodingCodes.LIST0) {
            buffer.put(EncodingCodes.LIST0);
            return;
        }

        final int fieldWidth;

        if (encodingCode == EncodingCodes.LIST8) {
            fieldWidth = 1;
            buffer.put(EncodingCodes.LIST8);
        } else {
            fieldWidth = 4;
            buffer.put(EncodingCodes.LIST32);
        }

        int startIndex = buffer.position();

        // Reserve space for the size and write the count of list elements.
        if (fieldWidth == 1) {
            buffer.put((byte) 0);
            buffer.put((byte) count);
        } else {
            buffer.putInt(0);
            buffer.putInt(count);
        }

        // Write the list elements and then compute total size written.
        for (int i = 0; i < count; ++i) {
            writeElement(flow, i);
        }

        // Move back and write the size
        int endIndex = buffer.position();
        int writeSize = endIndex - startIndex - fieldWidth;

        buffer.position(startIndex);
        if (fieldWidth == 1) {
            buffer.put((byte) writeSize);
        } else {
            buffer.putInt(writeSize);
        }
        buffer.position(endIndex);
    }

    private void writeElement(Flow flow, int index) {
        switch (index) {
            case 0:
                getEncoder().writeUnsignedInteger(flow.getNextIncomingId());
                break;
            case 1:
                getEncoder().writeUnsignedInteger(flow.getIncomingWindow());
                break;
            case 2:
                getEncoder().writeUnsignedInteger(flow.getNextOutgoingId());
                break;
            case 3:
                getEncoder().writeUnsignedInteger(flow.getOutgoingWindow());
                break;
            case 4:
                getEncoder().writeUnsignedInteger(flow.getHandle());
                break;
            case 5:
                getEncoder().writeUnsignedInteger(flow.getDeliveryCount());
                break;
            case 6:
                getEncoder().writeUnsignedInteger(flow.getLinkCredit());
                break;
            case 7:
                getEncoder().writeUnsignedInteger(flow.getAvailable());
                break;
            case 8:
                getEncoder().writeBoolean(flow.getDrain());
                break;
            case 9:
                getEncoder().writeBoolean(flow.getEcho());
                break;
            case 10:
                getEncoder().writeMap(flow.getProperties());
                break;
            default:
                throw new IllegalArgumentException("Unknown Flow value index: " + index);
        }
    }

    private int getElementCount(Flow flow) {
        if (flow.getProperties() != null) {
            return 11;
        } else if (flow.getEcho()) {
            return 10;
        } else if (flow.getDrain()) {
            return 9;
        } else if (flow.getAvailable() != null) {
            return 8;
        } else if (flow.getLinkCredit() != null) {
            return 7;
        } else if (flow.getDeliveryCount() != null) {
            return 6;
        } else if (flow.getHandle() != null) {
            return 5;
        } else {
            return 4;
        }
    }

    private byte deduceEncodingCode(Flow value, int elementCount) {
        if (elementCount == 0) {
            return EncodingCodes.LIST0;
        } else if (value.getProperties() == null) {
            return EncodingCodes.LIST8;
        } else {
            return EncodingCodes.LIST32;
        }
    }

    public static void register(Decoder decoder, EncoderImpl encoder) {
        BuiltinFlowType type = new BuiltinFlowType(encoder);
        for(Object descriptor : DESCRIPTORS)
        {
            decoder.register(descriptor, (BuiltinDescribedTypeConstructor<?>) type);
        }
        encoder.register(type);
    }
}
