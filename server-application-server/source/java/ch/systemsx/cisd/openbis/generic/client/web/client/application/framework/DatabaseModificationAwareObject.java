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

import java.util.Set;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;

/**
 * Stores an object and its database modification observer.
 * <p>
 * This class is useful when you cannot modify some class T to extend {@link IDatabaseModificationObserver} interface (because e.g. this class comes
 * from a library).
 * </p>
 * 
 * @author Tomasz Pylak
 */
public class DatabaseModificationAwareObject<T> implements IDatabaseModificationObserver
{
    private final T object;

    private final IDatabaseModificationObserver modificationObserver;

    public static <T> DatabaseModificationAwareObject<T> createUnaware(T object)
    {
        IDatabaseModificationObserver dummyObserver = createDummyModificationObserver();
        return new DatabaseModificationAwareObject<T>(object, dummyObserver);
    }

    protected static IDatabaseModificationObserver createDummyModificationObserver()
    {
        return new IDatabaseModificationObserver()
            {

                @Override
                public DatabaseModificationKind[] getRelevantModifications()
                {
                    return DatabaseModificationKind.EMPTY_ARRAY;
                }

                @Override
                public void update(Set<DatabaseModificationKind> observedModifications)
                {
                    // do nothing
                }

            };
    }

    public DatabaseModificationAwareObject(T holder,
            IDatabaseModificationObserver modificationObserver)
    {
        this.object = holder;
        this.modificationObserver = modificationObserver;
    }

    public IDatabaseModificationObserver getModificationObserver()
    {
        return modificationObserver;
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return modificationObserver.getRelevantModifications();
    }

    @Override
    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        modificationObserver.update(observedModifications);
    }

    /** @return the stored object */
    public T get()
    {
        return object;
    }

}
