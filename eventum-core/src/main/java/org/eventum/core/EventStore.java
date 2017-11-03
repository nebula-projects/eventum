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
import java.util.Date;
import java.util.List;

public interface EventStore {

    void save(PersistEvent persistedEvent);

    List<PersistEvent> findNeedRetry(int maxRecords);

    void update(String id, PersistEventStatus status);

    void updateRetries(String id, int retries, Date nextRetryTime);

    void removeCompleted(Timestamp modifiedDateBefore);
}
