
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

import java.util.Date;

import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.UnsignedInteger;
import org.apache.qpid.proton.codec.ProtonType;

public final class Properties implements Section, ProtonType
{
    private static final byte DESCRIPTOR_CODE = 0x73;

    private Object _messageId;
    private Binary _userId;
    private String _to;
    private String _subject;
    private String _replyTo;
    private Object _correlationId;
    private Symbol _contentType;
    private Symbol _contentEncoding;
    private Date _absoluteExpiryTime;
    private Date _creationTime;
    private String _groupId;
    private UnsignedInteger _groupSequence;
    private String _replyToGroupId;

    public Properties()
    {
    }

    public Properties(Properties other)
    {
        this._messageId = other._messageId;
        this._userId = other._userId;
        this._to = other._to;
        this._subject = other._subject;
        this._replyTo = other._replyTo;
        this._correlationId = other._correlationId;
        this._contentType = other._contentType;
        this._contentEncoding = other._contentEncoding;
        this._absoluteExpiryTime = other._absoluteExpiryTime;
        this._creationTime = other._creationTime;
        this._groupId = other._groupId;
        this._groupSequence = other._groupSequence;
        this._replyToGroupId = other._replyToGroupId;
    }

    public Object getMessageId()
    {
        return _messageId;
    }

    public void setMessageId(Object messageId)
    {
        _messageId = messageId;
    }

    public Binary getUserId()
    {
        return _userId;
    }

    public void setUserId(Binary userId)
    {
        _userId = userId;
    }

    public String getTo()
    {
        return _to;
    }

    public void setTo(String to)
    {
        _to = to;
    }

    public String getSubject()
    {
        return _subject;
    }

    public void setSubject(String subject)
    {
        _subject = subject;
    }

    public String getReplyTo()
    {
        return _replyTo;
    }

    public void setReplyTo(String replyTo)
    {
        _replyTo = replyTo;
    }

    public Object getCorrelationId()
    {
        return _correlationId;
    }

    public void setCorrelationId(Object correlationId)
    {
        _correlationId = correlationId;
    }

    public Symbol getContentType()
    {
        return _contentType;
    }

    public void setContentType(Symbol contentType)
    {
        _contentType = contentType;
    }

    public Symbol getContentEncoding()
    {
        return _contentEncoding;
    }

    public void setContentEncoding(Symbol contentEncoding)
    {
        _contentEncoding = contentEncoding;
    }

    public Date getAbsoluteExpiryTime()
    {
        return _absoluteExpiryTime;
    }

    public void setAbsoluteExpiryTime(Date absoluteExpiryTime)
    {
        _absoluteExpiryTime = absoluteExpiryTime;
    }

    public Date getCreationTime()
    {
        return _creationTime;
    }

    public void setCreationTime(Date creationTime)
    {
        _creationTime = creationTime;
    }

    public String getGroupId()
    {
        return _groupId;
    }

    public void setGroupId(String groupId)
    {
        _groupId = groupId;
    }

    public UnsignedInteger getGroupSequence()
    {
        return _groupSequence;
    }

    public void setGroupSequence(UnsignedInteger groupSequence)
    {
        _groupSequence = groupSequence;
    }

    public String getReplyToGroupId()
    {
        return _replyToGroupId;
    }

    public void setReplyToGroupId(String replyToGroupId)
    {
        _replyToGroupId = replyToGroupId;
    }

    @Override
    public String toString()
    {
        return "Properties{" +
               "messageId=" + _messageId +
               ", userId=" + _userId +
               ", to='" + _to + '\'' +
               ", subject='" + _subject + '\'' +
               ", replyTo='" + _replyTo + '\'' +
               ", correlationId=" + _correlationId +
               ", contentType=" + _contentType +
               ", contentEncoding=" + _contentEncoding +
               ", absoluteExpiryTime=" + _absoluteExpiryTime +
               ", creationTime=" + _creationTime +
               ", groupId='" + _groupId + '\'' +
               ", groupSequence=" + _groupSequence +
               ", replyToGroupId='" + _replyToGroupId + '\'' +
               '}';
    }

    @Override
    public byte getDescriptorCode() {
        return DESCRIPTOR_CODE;
    }

    @Override
    public SectionType getType() {
        return SectionType.Properties;
    }
}
