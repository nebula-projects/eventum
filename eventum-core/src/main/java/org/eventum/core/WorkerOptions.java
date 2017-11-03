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

public class WorkerOptions {

    /**
     *  max time in milliseconds between successive task executions
     */
    private long maxPeriod = 500;

    /**
     * the max number of records for one query
     */
    private int maxRecordsPerFetch = 100;

    private long expirationSeconds = 60;

    public long getMaxPeriod() {
        return maxPeriod;
    }

    public WorkerOptions setMaxPeriod(long maxPeriod) {
        this.maxPeriod = maxPeriod;
        return this;
    }

    public int getMaxRecordsPerFetch() {
        return maxRecordsPerFetch;
    }

    public WorkerOptions setMaxRecordsPerFetch(int maxRecordsPerFetch) {
        this.maxRecordsPerFetch = maxRecordsPerFetch;
        return this;
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }

    public WorkerOptions setExpirationSeconds(long expirationSeconds) {
        this.expirationSeconds = expirationSeconds;
        return this;
    }

}
