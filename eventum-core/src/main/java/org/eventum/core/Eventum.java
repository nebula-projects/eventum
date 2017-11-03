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
import org.eventum.core.retry.FixedBackoffRetryPolicy;
import org.eventum.core.retry.RetryPolicy;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Eventum {

    private ThreadLocal<PersistEvent> persistEventThreadLocal = new ThreadLocal<>();

    private boolean started = false;

    private Repository repository;
    private Publisher publisher;

    private EventConsumer eventConsumer;

    private EventWorker eventWorker;
    private EventCleanerWorker eventCleanerWorker;

    private final static int MAX_THREADS = 50;

    public Eventum(){
        this(new MemoryEventStore());
    }

    public Eventum(EventStore eventStore) {
        this(eventStore, new FixedBackoffRetryPolicy(100, 60));
    }

    public Eventum(EventStore eventStore, RetryPolicy retryPolicy) {
        this(eventStore, retryPolicy,new ThreadPoolExecutor(MAX_THREADS, MAX_THREADS,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(100)));
    }

    public Eventum(EventStore eventStore, RetryPolicy retryPolicy, Executor executor) {

        Objects.requireNonNull(retryPolicy);
        Objects.requireNonNull(eventStore);
        Objects.requireNonNull(executor);

        this.eventConsumer = new EventConsumer();
        this.repository = new Repository(eventStore, retryPolicy);
        this.publisher = new Publisher(repository, eventConsumer, executor);
        this.eventWorker = new EventWorker(repository, publisher);
        this.eventCleanerWorker = new EventCleanerWorker(repository);
    }


    public  void start() {
        startsWith(new WorkerOptions());
    }

    public synchronized void startsWith(WorkerOptions workerOptions) {
        if(!started){
            eventWorker.start(workerOptions);
            eventCleanerWorker.start(workerOptions);
            started = true;
        }
    }

    public synchronized void stop(){
        if(started){
            eventWorker.stop();
            eventCleanerWorker.stop();
            started = false;
        }
    }

    public Eventum registerEventHandler(EventHandler eventHandler){
        this.eventConsumer.registerHandler(eventHandler);
        return this;
    }

    public <T> T saveAndPublish(Event event) {

        save(event);
        return publish(event);
    }

    public void saveAndPublishAsync(Event event) {
        save(event);
        publishAsync(event);
    }

    public void saveAndPublishAsync(Event event, Executor executor) {
        save(event);
        publishAsync(event, executor);
    }

    public <T> T publish(Event event) {
        Objects.requireNonNull(event);
        return publisher.publish(event, persistEventThreadLocal.get());
    }

    public void publishAsync(Event event) {
        Objects.requireNonNull(event);

        publisher.publishAsync(event, persistEventThreadLocal.get());
    }

    public void publishAsync(Event event, Executor executor) {
        Objects.requireNonNull(event);
        Objects.requireNonNull(executor);

        publisher.publishAsync(event, persistEventThreadLocal.get(), executor);
    }

    public void save(Event event) {
        Objects.requireNonNull(event);

        PersistEvent persistEvent = EventConverter.getInstance().toPersistEvent(Optional.of(event));

        repository.handleCreate(persistEvent);

        persistEventThreadLocal.set(persistEvent);
    }

}
