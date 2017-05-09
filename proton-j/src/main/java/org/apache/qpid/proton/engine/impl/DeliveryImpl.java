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
package org.apache.qpid.proton.engine.impl;

import java.util.Arrays;

import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.codec.ProtonBuffer;
import org.apache.qpid.proton.codec.ReadableBuffer;
import org.apache.qpid.proton.codec.WritableBuffer;
import org.apache.qpid.proton.codec.buffer.ProtonByteBufferAllocator;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.Record;
import org.apache.qpid.proton.engine.Transport;

public class DeliveryImpl implements Delivery
{
    public static final int DEFAULT_MESSAGE_FORMAT = 0;

    private DeliveryImpl _linkPrevious;
    private DeliveryImpl _linkNext;

    private DeliveryImpl _workNext;
    private DeliveryImpl _workPrev;
    boolean _work;

    private DeliveryImpl _transportWorkNext;
    private DeliveryImpl _transportWorkPrev;
    boolean _transportWork;

    private Record _attachments;
    private Object _context;

    private final byte[] _tag;
    private final LinkImpl _link;
    private DeliveryState _deliveryState;
    private boolean _settled;
    private boolean _remoteSettled;
    private DeliveryState _remoteDeliveryState;
    private DeliveryState _defaultDeliveryState = null;
    private int _messageFormat = DEFAULT_MESSAGE_FORMAT;

    /**
     * A bit-mask representing the outstanding work on this delivery received from the transport layer
     * that has not yet been processed by the application.
     */
    private int _flags = (byte) 0;

    private TransportDelivery _transportDelivery;
    private ProtonBuffer data;
    private boolean _complete;
    private boolean _updated;
    private boolean _done;

    DeliveryImpl(final byte[] tag, final LinkImpl link, DeliveryImpl previous)
    {
        _tag = tag;
        _link = link;
        _link.incrementUnsettled();
        _linkPrevious = previous;
        if(previous != null)
        {
            previous._linkNext = this;
        }
    }

    @Override
    public byte[] getTag()
    {
        return _tag;
    }

    @Override
    public LinkImpl getLink()
    {
        return _link;
    }

    @Override
    public DeliveryState getLocalState()
    {
        return _deliveryState;
    }

    @Override
    public DeliveryState getRemoteState()
    {
        return _remoteDeliveryState;
    }

    @Override
    public boolean remotelySettled()
    {
        return _remoteSettled;
    }

    @Override
    public void setMessageFormat(int messageFormat)
    {
        _messageFormat = messageFormat;
    }

    @Override
    public int getMessageFormat()
    {
        return _messageFormat;
    }

    @Override
    public void disposition(final DeliveryState state)
    {
        _deliveryState = state;
        if(!_remoteSettled)
        {
            addToTransportWorkList();
        }
    }

    @Override
    public void settle()
    {
        if (_settled) {
            return;
        }

        _settled = true;
        _link.decrementUnsettled();
        if(!_remoteSettled)
        {
            addToTransportWorkList();
        }
        else
        {
            _transportDelivery.settled();
        }

        if(_link.current() == this)
        {
            _link.advance();
        }

        _link.remove(this);
        if(_linkPrevious != null)
        {
            _linkPrevious._linkNext = _linkNext;
        }

        if(_linkNext != null)
        {
            _linkNext._linkPrevious = _linkPrevious;
        }

        updateWork();

        _linkNext= null;
        _linkPrevious = null;
    }

    DeliveryImpl getLinkNext()
    {
        return _linkNext;
    }

    @Override
    public DeliveryImpl next()
    {
        return getLinkNext();
    }

    @Override
    public void free()
    {
        settle();
    }

    DeliveryImpl getLinkPrevious()
    {
        return _linkPrevious;
    }

    @Override
    public DeliveryImpl getWorkNext()
    {
        if (_workNext != null)
            return _workNext;
        // the following hack is brought to you by the C implementation!
        if (!_work)  // not on the work list
            return _link.getConnectionImpl().getWorkHead();
        return null;
    }

    DeliveryImpl getWorkPrev()
    {
        return _workPrev;
    }


    void setWorkNext(DeliveryImpl workNext)
    {
        _workNext = workNext;
    }

    void setWorkPrev(DeliveryImpl workPrev)
    {
        _workPrev = workPrev;
    }

    int recv(final byte[] bytes, int offset, int size)
    {
        final int consumed;
        if (data != null)
        {
            consumed = Math.min(size, data.getReadableBytes());
            data.readBytes(bytes, offset, consumed);
        }
        else
        {
            consumed = 0;
        }

        return (_complete && consumed == 0) ? Transport.END_OF_STREAM : consumed;  //TODO - Implement
    }

    // TODO - Hacked for now
    int recv(final WritableBuffer buffer) {
        final int consumed;
        if (data != null)
        {
            consumed = Math.min(buffer.remaining(), data.getReadableBytes());

            while (data.getReadableBytes() > 0)
            {
                buffer.put(data.readByte());
            }
        }
        else
        {
            consumed = 0;
        }

        return (_complete && consumed == 0) ? Transport.END_OF_STREAM : consumed;
    }

    void updateWork()
    {
        getLink().getConnectionImpl().workUpdate(this);
    }

    DeliveryImpl clearTransportWork()
    {
        DeliveryImpl next = _transportWorkNext;
        getLink().getConnectionImpl().removeTransportWork(this);
        return next;
    }

    void addToTransportWorkList()
    {
        getLink().getConnectionImpl().addTransportWork(this);
    }


    DeliveryImpl getTransportWorkNext()
    {
        return _transportWorkNext;
    }


    DeliveryImpl getTransportWorkPrev()
    {
        return _transportWorkPrev;
    }

    void setTransportWorkNext(DeliveryImpl transportWorkNext)
    {
        _transportWorkNext = transportWorkNext;
    }

    void setTransportWorkPrev(DeliveryImpl transportWorkPrev)
    {
        _transportWorkPrev = transportWorkPrev;
    }

    TransportDelivery getTransportDelivery()
    {
        return _transportDelivery;
    }

    void setTransportDelivery(TransportDelivery transportDelivery)
    {
        _transportDelivery = transportDelivery;
    }

    @Override
    public boolean isSettled()
    {
        return _settled;
    }

    int send(byte[] bytes, int offset, int length)
    {
        if (data == null)
        {
            data = ProtonByteBufferAllocator.DEFAULT.allocate(length);
        }

        data.writeBytes(bytes, offset, length);

        addToTransportWorkList();
        return length;  //TODO - Implement.
    }

    int send(final ReadableBuffer buffer)
    {
        int length = buffer.remaining();

        if (data == null)
        {
            data = ProtonByteBufferAllocator.DEFAULT.allocate(length);
        }

        // TODO - Terrible hack for now
        while (buffer.hasRemaining())
        {
            data.writeByte(buffer.get());
        }

        return length;
    }

    ProtonBuffer getData()
    {
        return data;
    }

    void setData(ProtonBuffer data)
    {
        this.data = data;
    }

    int getDataLength()
    {
        return data.getReadableBytes();
    }

    void reset()
    {
        this.data = null;
    }

    @Override
    public int available()
    {
        return data != null ? data.getReadableBytes() : 0;
    }

    @Override
    public boolean isWritable()
    {
        return getLink() instanceof SenderImpl
                && getLink().current() == this
                && ((SenderImpl) getLink()).hasCredit();
    }

    @Override
    public boolean isReadable()
    {
        return getLink() instanceof ReceiverImpl
            && getLink().current() == this;
    }

    void setComplete()
    {
        _complete = true;
    }

    @Override
    public boolean isPartial()
    {
        return !_complete;
    }

    void setRemoteDeliveryState(DeliveryState remoteDeliveryState)
    {
        _remoteDeliveryState = remoteDeliveryState;
        _updated = true;
    }

    @Override
    public boolean isUpdated()
    {
        return _updated;
    }

    @Override
    public void clear()
    {
        _updated = false;
        getLink().getConnectionImpl().workUpdate(this);
    }


    void setDone()
    {
        _done = true;
    }

    boolean isDone()
    {
        return _done;
    }

    void setRemoteSettled(boolean remoteSettled)
    {
        _remoteSettled = remoteSettled;
        _updated = true;
    }

    @Override
    public boolean isBuffered()
    {
        if (_remoteSettled) return false;
        if (getLink() instanceof SenderImpl) {
            if (isDone()) {
                return false;
            } else {
                return _complete || data.getReadableBytes() > 0;
            }
        } else {
            return false;
        }
    }

    @Override
    public Object getContext()
    {
        return _context;
    }

    @Override
    public void setContext(Object context)
    {
        _context = context;
    }

    @Override
    public Record attachments()
    {
        if(_attachments == null)
        {
            _attachments = new RecordImpl();
        }

        return _attachments;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("DeliveryImpl [_tag=").append(Arrays.toString(_tag))
            .append(", _link=").append(_link)
            .append(", _deliveryState=").append(_deliveryState)
            .append(", _settled=").append(_settled)
            .append(", _remoteSettled=").append(_remoteSettled)
            .append(", _remoteDeliveryState=").append(_remoteDeliveryState)
            .append(", _flags=").append(_flags)
            .append(", _defaultDeliveryState=").append(_defaultDeliveryState)
            .append(", _transportDelivery=").append(_transportDelivery)
            .append(", _dataSize=").append(available())
            .append(", _complete=").append(_complete)
            .append(", _updated=").append(_updated)
            .append(", _done=").append(_done).append("]");
        return builder.toString();
    }

    @Override
    public int pending()
    {
        return data.getReadableBytes();
    }

    @Override
    public void setDefaultDeliveryState(DeliveryState state)
    {
        _defaultDeliveryState = state;
    }

    @Override
    public DeliveryState getDefaultDeliveryState()
    {
        return _defaultDeliveryState;
    }
}
