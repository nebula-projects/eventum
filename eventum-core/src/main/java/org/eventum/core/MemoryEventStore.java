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
import org.eventum.core.event.PersistEventStatus;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryEventStore implements EventStore {

    Map<String, PersistEvent> store = new ConcurrentHashMap<String, PersistEvent>();

    public void save(PersistEvent persistEvent) {

        persistEvent.setCreatedTime(new Timestamp(System.currentTimeMillis()));
        persistEvent.setModifiedTime(new Timestamp(System.currentTimeMillis()));

        store.put(persistEvent.getId(), persistEvent);

    }

    public List<PersistEvent> findNeedRetry(int maxRecords) {

        List<PersistEvent> persistEvents = new ArrayList<>();

        int count = 0;
        for(PersistEvent persistEvent : store.values()){
            if(persistEvent.getNextRetryTime().before(currentTimestamp())&&
                    persistEvent.getStatus() ==PersistEventStatus.CREATED) {
                count++;
                if(count>maxRecords) {
                    break;
                }
                persistEvents.add(persistEvent);
            }
        }
        return persistEvents;
    }

    public void update(String id, PersistEventStatus status) {
        PersistEvent persistEvent = store.get(id);
        if(persistEvent!=null){
            persistEvent.setStatus(status);
        }
    }

    public void updateRetries(String id, int retries, Date nextRetryTime) {
        PersistEvent persistEvent = store.get(id);
        if(persistEvent!=null){
            persistEvent.setRetries(retries);
            persistEvent.setNextRetryTime(nextRetryTime);
        }
    }

    public void removeCompleted(Timestamp dateBefore){
        for(Iterator<Map.Entry<String, PersistEvent>> it = store.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, PersistEvent> entry = it.next();
            if(isExpired(entry.getValue(), dateBefore)) {
                it.remove();
            }
        }
    }

    public Map<String, PersistEvent>  getAll(){
        return store;
    }

    private Timestamp currentTimestamp(){
        return new Timestamp(System.currentTimeMillis());
    }

    private boolean isExpired(PersistEvent persistEvent, Timestamp dateBefore){
        return (persistEvent.getStatus()==PersistEventStatus.COMPLETED ||
                persistEvent.getStatus()==PersistEventStatus.CANCELLED) &&
                persistEvent.getModifiedTime().before(dateBefore);
    }
}
