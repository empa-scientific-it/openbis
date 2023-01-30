/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.IdsSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.openbis.generic.shared.basic.IPermIdHolder;

/**
 * @author Franz-Josef Elmer
 *
 */
public abstract class AbstractIdsMatcher<T extends IPermIdHolder> extends Matcher<T>
{
    @SuppressWarnings("unchecked")
    @Override
    public List<T> getMatching(IOperationContext context, List<T> objects, ISearchCriteria criteria)
    {
        Collection<IObjectId> ids = ((IdsSearchCriteria<IObjectId>) criteria).getFieldValue();

        if (ids != null && false == ids.isEmpty())
        {
            Collection<String> permIds = new HashSet<String>();

            for (IObjectId id : ids)
            {
                boolean wasPossible = addPermIdIfPossible(permIds, id);
                if (wasPossible == false)
                {
                    throw new IllegalArgumentException("Unknown id: " + id.getClass());
                }
            }

            List<T> matches = new ArrayList<T>();

            for (T object : objects)
            {
                if (permIds.contains(object.getPermId()))
                {
                    matches.add(object);
                }
            }

            return matches;
        } else
        {
            return new ArrayList<T>();
        }
    }
    
    protected abstract boolean addPermIdIfPossible(Collection<String> permIds, IObjectId id);

}
