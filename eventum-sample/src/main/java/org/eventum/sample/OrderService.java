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

import org.eventum.core.Eventum;
import org.eventum.sample.domain.Order;
import org.eventum.sample.domain.OrderStatus;
import org.eventum.sample.event.OrderCreatedEvent;
import org.eventum.sample.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private Eventum eventum;

    @Autowired
    private OrderRepository orderRepository;

    public Order create(){

        Order order = createOrder(OrderStatus.CREATED);

        OrderCreatedEvent event = create(order);

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                try{
                    orderRepository.create(order);
                    eventum.save(event);
                }catch (Exception e) {
                    e.printStackTrace();
                    transactionStatus.setRollbackOnly();
                }
            }
        });

        if(event!=null){
            eventum.publishAsync(event);
        }

        return order;
    }

    private static Order createOrder(OrderStatus orderStatus){
        Order order = new Order();
        order.setOrderId(UUID.randomUUID().toString());
        order.setCustomerId("eventum");
        order.setOrderStatus(orderStatus);

        return order;
    }

    private static OrderCreatedEvent create(Order order) {
        return new OrderCreatedEvent(order.getOrderId());
    }
}
