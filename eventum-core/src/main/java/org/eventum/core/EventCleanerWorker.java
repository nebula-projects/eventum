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

import java.util.concurrent.TimeUnit;

public class EventCleanerWorker {

    private Repository repository;

    private Thread eventCleanerThread;

    private volatile boolean started = false;

    public EventCleanerWorker(Repository repository) {
        this.repository = repository;
    }

    public synchronized void start(WorkerOptions workerOptions) {
        if(started){
            return;
        }

        started = true;

        eventCleanerThread =new Thread(()->{
            while(started && !Thread.currentThread().isInterrupted()) {

                repository.clean(workerOptions.getExpirationSeconds());

                sleep(60);
            }
        });
        eventCleanerThread.start();

    }

    public void stop(){
        if(eventCleanerThread!=null) {
            eventCleanerThread.interrupt();
            started = false;
        }
    }

    private void sleep(long seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        }catch(Exception e) {
            //ignore the exception
        }
    }
}
