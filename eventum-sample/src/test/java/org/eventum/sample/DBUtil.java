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

import org.eventum.core.event.PersistEvent;
import org.eventum.core.event.PersistEventStatus;
import org.eventum.sample.domain.Order;
import org.eventum.sample.domain.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class DBUtil {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public PersistEvent getEvent(String eventType){
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM eventum_events where type=?",
                    new PersistEventRowMapper(), eventType);
        }catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public Order getOrder(){
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM sample_orders",
                    new OrderRowMapper());
        }catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public void removeAll(){
        jdbcTemplate.update("DELETE FROM sample_orders");
        jdbcTemplate.update("DELETE FROM eventum_events");
    }

    class PersistEventRowMapper implements RowMapper<PersistEvent> {

        public PersistEvent mapRow(ResultSet rs, int rownumber) throws SQLException {
            PersistEvent persistEvent=new PersistEvent();
            persistEvent.setId(rs.getString("id"));
            persistEvent.setType(rs.getString("type"));
            persistEvent.setData(rs.getString("data"));
            persistEvent.setMetadata(rs.getString("metadata"));
            persistEvent.setEntityId(rs.getString("entityId"));
            persistEvent.setStatus(PersistEventStatus.valueOf(rs.getString("status")));
            persistEvent.setRetries(rs.getInt("retries"));
            persistEvent.setNextRetryTime(rs.getTimestamp("next_retry_time"));
            persistEvent.setCreatedTime(rs.getTimestamp("created_time"));
            persistEvent.setModifiedTime(rs.getTimestamp("modified_time"));
            return persistEvent;
        }
    }

    class OrderRowMapper implements RowMapper<Order> {

        public Order mapRow(ResultSet rs, int rownumber) throws SQLException {
            Order order=new Order();
            order.setId(rs.getLong("id"));
            order.setOrderId(rs.getString("orderId"));
            order.setCustomerId(rs.getString("customerId"));
            order.setOrderStatus(OrderStatus.valueOf(rs.getString("status")));
            order.setCreatedTime(rs.getTimestamp("created_time"));
            order.setModifiedTime(rs.getTimestamp("modified_time"));
            return order;
        }
    }


}

