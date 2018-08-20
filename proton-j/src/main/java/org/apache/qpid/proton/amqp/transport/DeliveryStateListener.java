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
package org.apache.qpid.proton.amqp.transport;

import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.amqp.messaging.Modified;
import org.apache.qpid.proton.amqp.messaging.Outcome;
import org.apache.qpid.proton.amqp.messaging.Rejected;
import org.apache.qpid.proton.amqp.messaging.Released;
import org.apache.qpid.proton.amqp.transaction.Declared;
import org.apache.qpid.proton.amqp.transaction.TransactionalState;
import org.apache.qpid.proton.engine.Delivery;

/**
 * Listener for DeliveryState update events
 */
public interface DeliveryStateListener {

    /**
     * Called when the delivery state of a Delivery has been updated and action is required.
     *
     * @param state
     *      The new Delivery State to act upon
     * @param outcome
     *      The new Delivery State to act upon
     * @param delivery
     *      The Delivery whose state has been updated.
     */
    default void handleAccepted(DeliveryState state, Accepted outcome, Delivery delivery) throws Exception {
        throw new UnsupportedOperationException("Accepted outcome handler not implemented");
    }

    /**
     * Called when the delivery state of a Delivery has been updated and action is required.
     *
     * @param state
     *      The new Delivery State to act upon
     * @param outcome
     *      The new Delivery State to act upon
     * @param delivery
     *      The Delivery whose state has been updated.
     */
    default void handleRejected(DeliveryState state, Rejected outcome, Delivery delivery) throws Exception {
        throw new UnsupportedOperationException("Rejected outcome handler not implemented");
    }

    /**
     * Called when the delivery state of a Delivery has been updated and action is required.
     *
     * @param state
     *      The new Delivery State to act upon
     * @param outcome
     *      The new Delivery State to act upon
     * @param delivery
     *      The Delivery whose state has been updated.
     */
    default void handleReleased(DeliveryState state, Released outcome, Delivery delivery) throws Exception {
        throw new UnsupportedOperationException("Released outcome handler not implemented");
    }

    /**
     * Called when the delivery state of a Delivery has been updated and action is required.
     *
     * @param state
     *      The new Delivery State to act upon
     * @param outcome
     *      The new Delivery State to act upon
     * @param delivery
     *      The Delivery whose state has been updated.
     */
    default void handleModified(DeliveryState state, Modified outcome, Delivery delivery) throws Exception {
        throw new UnsupportedOperationException("Modified outcome handler not implemented");
    }

    /**
     * Called when the delivery state of a Delivery has been updated and action is required.
     *
     * @param state
     *      The new Delivery State to act upon
     * @param outcome
     *      The new Delivery State to act upon
     * @param delivery
     *      The Delivery whose state has been updated.
     */
    default void handleDeclared(DeliveryState state, Declared outcome, Delivery delivery) throws Exception {
        throw new UnsupportedOperationException("Declared outcome handler not implemented");
    }

    /**
     * Called when the delivery state of a Delivery has been updated and action is required.
     *
     * @param state
     *      The new Delivery State to act upon
     * @param outcome
     *      The new Delivery State to act upon
     * @param delivery
     *      The Delivery whose state has been updated.
     */
    default void handleTransactional(TransactionalState state, Outcome outcome, Delivery delivery) throws Exception {
        throw new UnsupportedOperationException("Transactional outcome handler not implemented");
    }
}
