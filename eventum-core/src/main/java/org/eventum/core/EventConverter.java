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
import org.eventum.core.event.Metadata;
import org.eventum.core.event.PersistEvent;
import org.eventum.core.event.PersistEventStatus;

import java.util.Optional;

public class EventConverter {

    private static final EventConverter eventConverter = new EventConverter();
    private ObjectMapper objectMapper;

    private EventConverter(){
    }

    public static EventConverter getInstance(){
        return eventConverter;
    }

    public EventConverter setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        return this;
    }

    public PersistEvent toPersistEvent(Optional<Event> event) {

        PersistEvent persistEvent = new PersistEvent();
        persistEvent.setEntityId(event.map(e->e.getEntityId()).orElseThrow(NullPointerException::new));
        persistEvent.setData(objectMapper.toJson(event.get()));
        persistEvent.setMetadata(objectMapper.toJson(new Metadata().setEventClassName(event.get().getClass().getCanonicalName())));
        persistEvent.setType(event.map(e->e.getType()).orElseThrow(NullPointerException::new));
        persistEvent.setStatus(PersistEventStatus.CREATED);

        return persistEvent;
    }

    public <T extends Event> T toEvent(PersistEvent persistEvent) {
        try{

        return objectMapper.parse(persistEvent.getData(),
                Class.forName(((Metadata)objectMapper.parse(persistEvent.getMetadata(), Metadata.class)).getEventClassName()));

        }catch(Exception e) {
            //TODO
        }
        return null;
    }
}
