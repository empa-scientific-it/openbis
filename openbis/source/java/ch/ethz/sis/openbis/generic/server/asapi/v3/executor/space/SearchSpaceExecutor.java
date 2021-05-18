/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.space;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.*;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpaceTechId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.*;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author pkupczyk
 */
@Component
public class SearchSpaceExecutor extends AbstractSearchObjectManuallyExecutor<SpaceSearchCriteria, SpacePE> implements ISearchSpaceExecutor
{

    @Autowired
    private ISpaceAuthorizationExecutor authorizationExecutor;

    @Override
    public List<SpacePE> search(IOperationContext context, SpaceSearchCriteria criteria)
    {
        authorizationExecutor.canSearch(context);
        return super.search(context, criteria);
    }

    @Override
    protected List<SpacePE> listAll()
    {
        return daoFactory.getSpaceDAO().listAllEntities();
    }

    @Override
    protected Matcher<SpacePE> getMatcher(ISearchCriteria criteria)
    {
        if (criteria instanceof IdSearchCriteria<?>)
        {
            return new IdMatcher();
        } else if (criteria instanceof PermIdSearchCriteria || criteria instanceof CodeSearchCriteria)
        {
            return new CodeMatcher<SpacePE>();
        } else if (criteria instanceof CodesSearchCriteria)
        {
            return new CodesMatcher<SpacePE>();
        } else
        {
            throw new IllegalArgumentException("Unknown search criteria: " + criteria.getClass());
        }
    }

    private class IdMatcher extends SimpleFieldMatcher<SpacePE>
    {

        @Override
        protected boolean isMatching(IOperationContext context, SpacePE object, ISearchCriteria criteria)
        {
            Object id = ((IdSearchCriteria<?>) criteria).getId();

            if (id == null)
            {
                return true;
            } else if (id instanceof SpacePermId)
            {
                return object.getCode().equals(((SpacePermId) id).getPermId());
            } else if (id instanceof SpaceTechId)
            {
                return object.getId().equals(((SpaceTechId) id).getTechId());
            } else
            {
                throw new IllegalArgumentException("Unknown id: " + id.getClass());
            }
        }

    }

}
