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

package org.eventum.sample.repository;

import org.apache.ibatis.annotations.Param;
import org.eventum.sample.domain.Order;
import org.eventum.sample.domain.OrderStatus;
import org.springframework.stereotype.Component;

public interface OrderRepository {

    void create(Order order);

    void changeStatus(@Param("orderId")String orderId, @Param("orderStatus")String orderStatus);

    Order get(@Param("orderId")String orderId);
}
