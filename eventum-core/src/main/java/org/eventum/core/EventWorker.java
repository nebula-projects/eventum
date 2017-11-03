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

import java.util.List;

public class EventWorker {

    private Repository repository;
    private Publisher publisher;

    private volatile boolean started = false;

    private Thread eventThread;

    public EventWorker(Repository repository, Publisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    public synchronized void start(WorkerOptions workerOptions) {
        if(started){
            return;
        }

        started = true;

        eventThread = new Thread(()->{
            while(started && !Thread.currentThread().isInterrupted()) {

                    List<PersistEvent> persistEvents = repository.findNeedRetry(workerOptions.getMaxRecordsPerFetch());

                    for (PersistEvent persistEvent : persistEvents) {
                        repository.handleRetry(persistEvent);
                        publisher.publishAsync(EventConverter.getInstance().toEvent(persistEvent), persistEvent);
                    }

                    if (persistEvents.isEmpty()) {
                        sleep(workerOptions.getMaxPeriod());
                    }
                }
        });

        eventThread.start();
    }

    public void stop(){
        if(eventThread!=null){
            eventThread.interrupt();
            started = false;
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        }catch(Exception e) {
            //ignore the exception
        }
    }


}
