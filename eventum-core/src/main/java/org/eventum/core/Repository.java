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
import org.eventum.core.retry.RetryPolicy;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public class Repository {

    private EventStore eventStore;
    private RetryPolicy retryPolicy;

    public Repository(EventStore eventStore, RetryPolicy retryPolicy) {
        this.eventStore = eventStore;
        this.retryPolicy = retryPolicy;
    }

    public void handleCreate(PersistEvent persistEvent) {

        persistEvent.setId(UUID.randomUUID().toString());
        persistEvent.setRetries(0);
        persistEvent.setNextRetryTime(retryPolicy.getNextRetryTime(0));
        persistEvent.setCreatedTime(new Timestamp(System.currentTimeMillis()));
        persistEvent.setModifiedTime(new Timestamp(System.currentTimeMillis()));

        eventStore.save(persistEvent);
    }

    public void handleComplete(PersistEvent persistEvent) {
        if(persistEvent!=null) {
            eventStore.update(persistEvent.getId(), PersistEventStatus.COMPLETED);
        }
    }

    public void handleFailure(PersistEvent persistEvent) {
        if(persistEvent!=null) {
            eventStore.update(persistEvent.getId(), PersistEventStatus.FAILURE);
        }
    }

    public void handleRetry(PersistEvent persistEvent) {
        if(persistEvent!=null) {
            int retryCount = persistEvent.getRetries()+1;
            if(retryCount > retryPolicy.getMaxRetryCount()) {
                eventStore.update(persistEvent.getId(), PersistEventStatus.MAX_RETRIES_EXCEED);
            }else {
                eventStore.updateRetries(persistEvent.getId(), retryCount,
                        new Timestamp(retryPolicy.getNextRetryTime(retryCount).getTime()));
            }
        }
    }

    public List<PersistEvent> findNeedRetry(int maxRecords){
        return eventStore.findNeedRetry(maxRecords);
    }

    public void clean(long expirationSeconds){
        Timestamp expirationTimestamp = new Timestamp(System.currentTimeMillis() - expirationSeconds * 1000);
        eventStore.removeCompleted(expirationTimestamp);
    }
}
