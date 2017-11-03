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

package org.eventum.core;

import org.eventum.core.event.PersistEvent;
import org.eventum.core.mock.MockEvent;
import org.eventum.core.mock.MockEventHandler;
import org.eventum.core.retry.FixedBackoffRetryPolicy;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EventumTest {

    @BeforeClass
    public static void setUpBeforeClass(){
        EventConverter.getInstance().setObjectMapper(new FastjsonObjectMapper());
    }

    @Test
    public void testPublish() {
        MockEventHandler mockEventHandler = new MockEventHandler();
        MockEvent mockEvent = new MockEvent("entityId-1");

        Eventum eventum = new Eventum(new MemoryEventStore());
        eventum.registerEventHandler(mockEventHandler);

        assertFalse(mockEventHandler.isProcessed());

        long startTime = System.currentTimeMillis();
        eventum.publish(mockEvent);

        assertTrue(System.currentTimeMillis()-startTime>1000);

        assertTrue(mockEventHandler.isProcessed());

    }

    @Test
    public void testPublishAsync() {
        MockEventHandler mockEventHandler = new MockEventHandler();
        MockEvent mockEvent = new MockEvent("entityId-1");

        Eventum eventum = new Eventum(new MemoryEventStore());
        eventum.registerEventHandler(mockEventHandler);

        assertFalse(mockEventHandler.isProcessed());

        eventum.publishAsync(mockEvent);
        assertFalse(mockEventHandler.isProcessed());

        sleep(3000);

        assertTrue(mockEventHandler.isProcessed());

    }

    @Test
    public void testSaveAndPublish() {
        String entityId = "entityId-1";
        MockEventHandler mockEventHandler = new MockEventHandler();
        MockEvent mockEvent = new MockEvent(entityId);

        MemoryEventStore store = new MemoryEventStore();

        Eventum eventum = new Eventum(store);
        eventum.registerEventHandler(mockEventHandler);

        assertFalse(mockEventHandler.isProcessed());

        long startTime = System.currentTimeMillis();
        eventum.saveAndPublish(mockEvent);

        assertTrue(System.currentTimeMillis()-startTime>1000);

        assertTrue(mockEventHandler.isProcessed());

        Map<String, PersistEvent> persistEvents = store.getAll();

        PersistEvent persistEvent = persistEvents.values().iterator().next();
        assertEquals(1,persistEvents.size());
        assertEquals(mockEvent.getEntityId(), persistEvent.getEntityId());
        assertEquals(mockEvent.getType(), persistEvent.getType());

    }

    @Test
    public void testSaveAndPublishAsync() {
        String entityId = "entityId-1";
        MockEventHandler mockEventHandler = new MockEventHandler();
        MockEvent mockEvent = new MockEvent(entityId);

        MemoryEventStore store = new MemoryEventStore();

        Eventum eventum = new Eventum(store);
        eventum.registerEventHandler(mockEventHandler);
        eventum.start();

        assertFalse(mockEventHandler.isProcessed());

        eventum.saveAndPublishAsync(mockEvent);

        assertFalse(mockEventHandler.isProcessed());

        sleep(3000);

        assertTrue(mockEventHandler.isProcessed());

        Map<String, PersistEvent> persistEvents = store.getAll();

        PersistEvent persistEvent = persistEvents.values().iterator().next();
        assertEquals(1,persistEvents.size());
        assertEquals(mockEvent.getEntityId(), persistEvent.getEntityId());
        assertEquals(mockEvent.getType(), persistEvent.getType());

    }

    @Test
    public void testSaveWithMasterEventWorker() {
        String entityId = "entityId-1";
        MockEventHandler mockEventHandler = new MockEventHandler();
        MockEvent mockEvent = new MockEvent(entityId);

        MemoryEventStore store = new MemoryEventStore();

        Eventum eventum = new Eventum(store, new FixedBackoffRetryPolicy(100, 1));
        eventum.registerEventHandler(mockEventHandler);
        eventum.startsWith(new WorkerOptions().setMaster(true));

        assertFalse(mockEventHandler.isProcessed());

        eventum.save(mockEvent);

        sleep(4*1000);
        assertTrue(mockEventHandler.isProcessed());

    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        }catch(Exception e) {
            //ignore the exception
        }
    }
}
