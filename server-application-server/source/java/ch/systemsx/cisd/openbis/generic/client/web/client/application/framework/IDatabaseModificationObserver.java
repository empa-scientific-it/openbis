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
 * Interface to be implemented if a component wants to be notified of if for a list of relevant {@link DatabaseModificationKind} instances changes
 * have been observed.
 * 
 * @author Tomasz Pylak
 */
public interface IDatabaseModificationObserver
{
    /**
     * Informs about new modifications in the database. This method is called only if at least one observed modification is the relevant one.
     * 
     * @param observedModifications The new database modifications which have occurred.
     */
    void update(Set<DatabaseModificationKind> observedModifications);

    /**
     * @return the list of database object modifications in which this observer is interested. Used to call the refresh method only when it is really
     *         necessary.
     */
    DatabaseModificationKind[] getRelevantModifications();

}
