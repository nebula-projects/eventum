/*
 * Copyright 2002-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eventum.sample;

import org.eventum.core.EventHandler;
import org.eventum.core.Eventum;
import org.eventum.sample.domain.OrderStatus;
import org.eventum.sample.event.OrderCreatedEvent;
import org.eventum.sample.event.PaymentEvent;
import org.eventum.sample.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class OrderCreatedEventHandler implements EventHandler<OrderCreatedEvent>{

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private Eventum eventum;

    public Class supportedEventClass(){
        return OrderCreatedEvent.class;
    }

    public Boolean process(OrderCreatedEvent event) {

        String orderId = event.getEntityId();

        PaymentEvent paymentEvent = new PaymentEvent(orderId);

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                try{
                    System.out.println("Process Order Created Event for order id:"+orderId);
                    orderRepository.changeStatus(orderId, OrderStatus.PAID.name());
                    eventum.saveAndPublish(paymentEvent);
                }catch (Exception e) {
                    e.printStackTrace();
                    transactionStatus.setRollbackOnly();
                }
            }
        });

        return true;
    }

}
