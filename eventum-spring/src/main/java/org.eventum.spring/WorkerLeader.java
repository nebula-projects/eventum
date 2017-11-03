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

package org.eventum.spring;

import org.eventum.core.Eventum;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;

import java.io.Closeable;
import java.io.IOException;

public class WorkerLeader extends LeaderSelectorListenerAdapter implements Closeable {

    private final LeaderSelector leaderSelector;

    private Eventum eventum;

    public WorkerLeader(CuratorFramework client, String path, Eventum eventum){

        this.eventum = eventum;

        leaderSelector = new LeaderSelector(client, path, this);

        leaderSelector.autoRequeue();
    }

    public void start() throws IOException
    {
        leaderSelector.start();
    }

    @Override
    public void close() throws IOException
    {
        leaderSelector.close();
    }

    @Override
    public void takeLeadership(CuratorFramework client) throws Exception
    {
        // we are now the leader. This method should not return until we want to relinquish leadership

        final int  waitSeconds = (int)(5 * Math.random()) + 1;

        eventum.start();

        System.out.println("it is now the leader. Waiting " + waitSeconds + " seconds...");
        try
        {
            Thread.currentThread().join();
        }
        catch ( InterruptedException e )
        {
            System.err.println("it was interrupted.");
            Thread.currentThread().interrupt();
        }
        finally
        {
            System.out.println("it relinquishing leadership.\n");
            eventum.stop();
        }
    }
}
