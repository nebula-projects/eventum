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


import org.eventum.core.EventConverter;
import org.eventum.core.event.PersistEvent;
import org.eventum.core.event.PersistEventStatus;
import org.eventum.sample.domain.Order;
import org.eventum.sample.domain.OrderStatus;
import org.eventum.sample.event.OrderCreatedEvent;
import org.eventum.sample.event.PaymentEvent;
import org.eventum.spring.AppConfig;
import org.eventum.spring.FastjsonObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={SampleConfig.class, AppConfig.class})
public class OrderServiceIT {

    @Autowired
    private OrderService orderService;

    @Autowired
    private DBUtil DBUtil;

    private PersistEvent persistEvent;

    @Before
    public void setUp(){
        EventConverter.getInstance().setObjectMapper(new FastjsonObjectMapper());
        DBUtil.removeAll();
    }

    @After
    public void tearDown(){
        DBUtil.removeAll();
    }

    @Test
    public void testCreateOrder(){

        Order order = orderService.create();

        assertNotNull(order);

        sleep(3);

        Order dbOrder = DBUtil.getOrder();

        assertEquals(OrderStatus.PAID, dbOrder.getOrderStatus());
        assertEquals(PersistEventStatus.COMPLETED, DBUtil.getEvent(OrderCreatedEvent.class.getName()).getStatus());
        assertEquals(PersistEventStatus.COMPLETED, DBUtil.getEvent(PaymentEvent.class.getName()).getStatus());

    }

    private static void sleep(int secs){
        try {
            TimeUnit.SECONDS.sleep(secs);
        }catch(InterruptedException e) {
            //ignore
        }
    }

}
