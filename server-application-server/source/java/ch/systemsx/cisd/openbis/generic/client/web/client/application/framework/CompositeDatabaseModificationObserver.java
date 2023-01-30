/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.framework;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.SetUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;

/**
 * Composite class to implement {@link IDatabaseModificationObserver} interface for a collection of observers.
 * 
 * @author Tomasz Pylak
 */
public class CompositeDatabaseModificationObserver implements IDatabaseModificationObserver
{
    protected final List<IDatabaseModificationObserver> registeredObservers;

    public CompositeDatabaseModificationObserver()
    {
        this.registeredObservers = new ArrayList<IDatabaseModificationObserver>();
    }

    public void addObservers(List<? extends IDatabaseModificationObserver> observers)
    {
        for (IDatabaseModificationObserver observer : observers)
        {
            addObserver(observer);
        }
    }

    public void addObserver(IDatabaseModificationObserver observerOrNull)
    {
        if (observerOrNull != null)
        {
            registeredObservers.add(observerOrNull);
        }
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        Set<DatabaseModificationKind> relevantModifications =
                new HashSet<DatabaseModificationKind>();
        for (IDatabaseModificationObserver observer : registeredObservers)
        {
            SetUtils.addAll(relevantModifications, observer.getRelevantModifications());
        }
        return relevantModifications.toArray(DatabaseModificationKind.EMPTY_ARRAY);
    }

    @Override
    public void update(final Set<DatabaseModificationKind> observedModifications)
    {
        for (IDatabaseModificationObserver observer : registeredObservers)
        {
            if (SetUtils.containsAny(observedModifications, observer.getRelevantModifications()))
            {
                observer.update(observedModifications);
            }
        }

    }

}
