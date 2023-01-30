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

import java.util.HashSet;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.SetUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;

/**
 * {@link CompositeDatabaseModificationObserver} extension with update of added observers stopped until main observer update is finished. If main
 * observer fails to update other observers will not update at all.
 * 
 * @author Piotr Buczek
 */
public class CompositeDatabaseModificationObserverWithMainObserver extends
        CompositeDatabaseModificationObserver
{
    private final IDatabaseModificationObserverWithCallback mainObserver;

    public CompositeDatabaseModificationObserverWithMainObserver(
            IDatabaseModificationObserverWithCallback mainObserver)
    {
        super();
        assert mainObserver != null : "main observer cannot be null";
        this.mainObserver = mainObserver;
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        Set<DatabaseModificationKind> relevantModifications =
                new HashSet<DatabaseModificationKind>();
        SetUtils.addAll(relevantModifications, mainObserver.getRelevantModifications());
        SetUtils.addAll(relevantModifications, super.getRelevantModifications());
        return relevantModifications.toArray(DatabaseModificationKind.EMPTY_ARRAY);
    }

    @Override
    public void update(final Set<DatabaseModificationKind> observedModifications)
    {
        if (SetUtils.containsAny(observedModifications, mainObserver.getRelevantModifications()))
        {
            mainObserver.setSuccessfulUpdateCallback(new IDelegatedAction()
                {
                    @Override
                    public void execute()
                    {
                        updateOtherObservers(observedModifications);
                    }
                });
            mainObserver.update(observedModifications);
        } else
        {
            updateOtherObservers(observedModifications);
        }
    }

    private void updateOtherObservers(Set<DatabaseModificationKind> observedModifications)
    {
        super.update(observedModifications);
    }

}
