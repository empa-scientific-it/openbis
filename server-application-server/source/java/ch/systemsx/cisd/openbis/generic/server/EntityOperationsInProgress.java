/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server;

import java.util.HashSet;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;

/**
 * The application state of all entity operations that are currently in progress.
 * 
 * @author jakubs
 */
public class EntityOperationsInProgress
{
    private static EntityOperationsInProgress instance;

    private static Object locker = new Object();

    /**
     * The singleton
     */
    public static EntityOperationsInProgress getInstance()
    {
        if (instance == null)
        {
            synchronized (locker)
            {
                if (instance == null)
                {
                    instance = new EntityOperationsInProgress();
                }
            }
        }
        return instance;
    }

    private final HashSet<TechId> pendingRegistrations;

    private EntityOperationsInProgress()
    {
        pendingRegistrations = new HashSet<TechId>();
    }

    /**
     * Check whether the registration with given id is being processed right now.
     */
    public boolean isRegistrationPending(TechId id)
    {
        if (id == null)
        {
            throw new IllegalArgumentException("Cannot ask if a registration is pending for null");
        }
        synchronized (locker)
        {
            return pendingRegistrations.contains(id);
        }
    }

    /**
     * Mark given id as being processed.
     */
    public void addRegistrationPending(TechId id)
    {
        if (id != null)
        {
            synchronized (locker)
            {
                pendingRegistrations.add(id);
            }
        }
    }

    /**
     * Remove the given registration id from the being processed list.
     */
    public void removeRegistrationPending(TechId id)
    {
        if (id != null)
        {
            synchronized (locker)
            {
                pendingRegistrations.remove(id);
            }
        }
    }
}
