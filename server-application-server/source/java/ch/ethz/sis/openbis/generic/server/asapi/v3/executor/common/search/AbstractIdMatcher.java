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

import java.util.Arrays;
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.IdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.IdsSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.openbis.generic.shared.basic.IPermIdHolder;

/**
 * @author Franz-Josef Elmer
 *
 */
public abstract class AbstractIdMatcher<T extends IPermIdHolder> extends Matcher<T>
{
    @SuppressWarnings("unchecked")
    @Override
    public List<T> getMatching(IOperationContext context, List<T> objects, ISearchCriteria criteria)
    {
        IObjectId id = ((IdSearchCriteria<IObjectId>) criteria).getId();

        if (id == null)
        {
            return objects;
        } else
        {
            IdsSearchCriteria<IObjectId> idsCriteria = new IdsSearchCriteria<IObjectId>();
            idsCriteria.thatIn(Arrays.asList(id));
            return createIdsMatcher().getMatching(context, objects, idsCriteria);
        }
    }
    
    protected abstract AbstractIdsMatcher<T> createIdsMatcher();


}
