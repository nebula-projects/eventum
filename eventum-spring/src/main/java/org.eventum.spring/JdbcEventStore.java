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

package org.eventum.spring;

import org.eventum.core.EventStore;
import org.eventum.core.event.PersistEvent;
import org.eventum.core.event.PersistEventStatus;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public class JdbcEventStore implements EventStore{

    private JdbcTemplate jdbcTemplate;

    public JdbcEventStore(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(PersistEvent persistEvent) {

        try {
            jdbcTemplate.update("INSERT INTO eventum_events (id, type, data, metadata, entityId, " +
                            "status, retries, next_retry_time, created_time, modified_time) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    persistEvent.getId(), persistEvent.getType(), persistEvent.getData(),
                    persistEvent.getMetadata(), persistEvent.getEntityId(), persistEvent.getStatus().name(),
                    persistEvent.getRetries(), persistEvent.getNextRetryTime(),
                    currentTimestamp(), currentTimestamp());
        } catch (DuplicateKeyException e) {
            throw new IllegalArgumentException();
        }
    }

    public List<PersistEvent> findNeedRetry(int maxRecords) {
        return jdbcTemplate.query("SELECT * FROM eventum_events WHERE status='CREATED' AND next_retry_time<=? LIMIT ?",
                new Object[]{currentTimestamp(), maxRecords}, new RowMapper<PersistEvent>(){

                    public PersistEvent mapRow(ResultSet rs, int rownumber) throws SQLException {
                        PersistEvent persistEvent=new PersistEvent();
                        persistEvent.setId(rs.getString("id"));
                        persistEvent.setType(rs.getString("type"));
                        persistEvent.setData(rs.getString("data"));
                        persistEvent.setMetadata(rs.getString("metadata"));
                        persistEvent.setEntityId(rs.getString("entityId"));
                        persistEvent.setStatus(PersistEventStatus.valueOf(rs.getString("status")));
                        persistEvent.setRetries(rs.getInt("retries"));
                        persistEvent.setNextRetryTime(rs.getDate("next_retry_time"));
                        persistEvent.setCreatedTime(rs.getTimestamp("created_time"));
                        persistEvent.setModifiedTime(rs.getTimestamp("modified_time"));
                        return persistEvent;
                    }
                });
    }

    public void update(String id, PersistEventStatus status){
        jdbcTemplate.update("UPDATE eventum_events SET status=? WHERE id=?",
                status.name(),id);
    }

    public void updateRetries(String id, int retries, Date nextRetryTime) {
        jdbcTemplate.update("UPDATE eventum_events SET retries=?, next_retry_time=? WHERE id=?",
                retries, nextRetryTime,id);
    }

    public void removeCompleted(Timestamp modifiedDateBefore){
        jdbcTemplate.update("DELETE FROM eventum_events WHERE modified_time<?", modifiedDateBefore);
    }

    private static Timestamp currentTimestamp(){
        return new Timestamp(new Date().getTime());
    }
}
