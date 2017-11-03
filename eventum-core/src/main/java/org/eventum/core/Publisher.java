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

import org.eventum.core.event.Event;
import org.eventum.core.event.PersistEvent;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class Publisher {

    private Repository repository;
    private EventConsumer eventConsumer;
    private Executor executor;

    public Publisher(Repository repository, EventConsumer eventConsumer, Executor executor ) {
        this.repository = repository;
        this.eventConsumer = eventConsumer;
        this.executor = executor;
    }

    public void publishAsync(Event event, PersistEvent persistEvent) {
        publishAsync(event, persistEvent, this.executor);
    }

    public void publishAsync(Event event, PersistEvent persistEvent, Executor executor) {
        CompletableFuture.runAsync(
                () -> eventConsumer.handle(event), executor)
                .whenComplete((result, ex) -> { if(ex==null) repository.handleComplete(persistEvent);});
    }

    public <T> T publish(Event event, PersistEvent persistEvent){
        try {
            T t = eventConsumer.handle(event);
            repository.handleComplete(persistEvent);

            return t;
        }catch(Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
