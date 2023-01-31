/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.shared.content;

import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Franz-Josef Elmer
 */
public class DelayedPersistenceManager implements IPersistenceManager
{
    private final IPersistenceManager persistenceManager;

    private final Timer timer;

    private final long delay;

    private boolean persistenceRequestScheduled;

    public DelayedPersistenceManager(IPersistenceManager persistenceManager)
    {
        this(persistenceManager, 10000);
    }

    public DelayedPersistenceManager(IPersistenceManager persistenceManager, long delay)
    {
        this.persistenceManager = persistenceManager;
        this.delay = delay;
        timer = new Timer("persistence manager timer", true);
    }

    @Override
    public Serializable load(Serializable defaultObject)
    {
        return persistenceManager.load(defaultObject);
    }

    @Override
    public void requestPersistence()
    {
        synchronized (persistenceManager)
        {
            if (persistenceRequestScheduled)
            {
                return;
            }
            timer.schedule(new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        persistenceManager.requestPersistence();
                        synchronized (persistenceManager)
                        {
                            persistenceRequestScheduled = false;
                        }
                    }
                }, delay);
            persistenceRequestScheduled = true;
        }
    }

}
