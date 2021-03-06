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

package org.eventum.core.event;

public class DefaultEvent implements Event {

    private String entityId;
    private String type;

    public DefaultEvent(String entityId, String type){
        if(entityId == null || type == null) {
            throw new IllegalArgumentException("The entityId or type can't be null.");
        }
        this.entityId = entityId;
        this.type = type;
    }

    public String getEntityId(){
        return this.entityId;
    }

    public String getType(){
        return this.type;
    }
}
