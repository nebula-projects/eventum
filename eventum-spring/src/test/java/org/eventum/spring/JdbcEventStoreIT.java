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

import org.eventum.core.event.PersistEvent;
import org.eventum.core.event.PersistEventStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={TestConfig.class})
public class JdbcEventStoreIT {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EventDBUtil eventDbUtil;

    private JdbcEventStore jdbcEventStore;

    private PersistEvent persistEvent;

    @Before
    public void setUp(){
        jdbcEventStore = new JdbcEventStore(jdbcTemplate);
        persistEvent = createPersistEvent();
    }

    @After
    public void tearDown(){
        eventDbUtil.removeAll();
    }

    @Test
    public void testSave(){
        jdbcEventStore.save(persistEvent);

        PersistEvent actualPersistEvent = eventDbUtil.get(persistEvent.getId());

        assertEquals(persistEvent.getEntityId(), actualPersistEvent.getEntityId());
        assertEquals(persistEvent.getData(), actualPersistEvent.getData());
        assertEquals(persistEvent.getType(), actualPersistEvent.getType());
        assertEquals(persistEvent.getMetadata(), actualPersistEvent.getMetadata());
        assertEquals(persistEvent.getStatus(), actualPersistEvent.getStatus());
        assertEquals(persistEvent.getRetries(), actualPersistEvent.getRetries());
        assertEquals(removeMillis(persistEvent.getNextRetryTime()).getTime(), actualPersistEvent.getNextRetryTime().getTime());

        assertNotNull(actualPersistEvent.getCreatedTime());
    }

    @Test
    public void testUpdateStatus(){
        jdbcEventStore.save(persistEvent);

        jdbcEventStore.update(persistEvent.getId(), PersistEventStatus.COMPLETED);

        PersistEvent actualPersistEvent = eventDbUtil.get(persistEvent.getId());

        assertEquals(PersistEventStatus.COMPLETED, actualPersistEvent.getStatus());
    }

    @Test
    public void testUpdateRetries(){
        jdbcEventStore.save(persistEvent);

        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss").parse("2017-10-18 10:06:18");
        }catch(Exception e){
            //ignore
        }
        jdbcEventStore.updateRetries(persistEvent.getId(), 3, date);

        PersistEvent actualPersistEvent = eventDbUtil.get(persistEvent.getId());

        assertEquals(3, actualPersistEvent.getRetries());
        assertEquals(date.getTime(), actualPersistEvent.getNextRetryTime().getTime());
    }

    @Test
    public void testRemoveCompleted(){

        jdbcEventStore.save(persistEvent);

        PersistEvent actualPersistEvent = eventDbUtil.get(persistEvent.getId());

        assertNotNull(actualPersistEvent);

        sleep(3);

        jdbcEventStore.removeCompleted(new Timestamp(new Date().getTime()));

        actualPersistEvent = eventDbUtil.get(persistEvent.getId());

        assertNull(actualPersistEvent);

    }

    private PersistEvent createPersistEvent(){
        PersistEvent persistEvent = new PersistEvent();
        persistEvent.setId(UUID.randomUUID().toString());
        persistEvent.setEntityId("entityId-1");
        persistEvent.setType("Type");
        persistEvent.setData("data");
        persistEvent.setMetadata("metadata");
        persistEvent.setStatus(PersistEventStatus.CREATED);
        persistEvent.setRetries(1);
        persistEvent.setNextRetryTime(new Date());

        return persistEvent;
    }

    private static Date removeMillis(Date date){
        Calendar cal = Calendar.getInstance(); // locale-specific
        cal.setTime(date);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private static void sleep(int secs){
        try {
            TimeUnit.SECONDS.sleep(secs);
        }catch(InterruptedException e) {
            //ignore
        }
    }

}
