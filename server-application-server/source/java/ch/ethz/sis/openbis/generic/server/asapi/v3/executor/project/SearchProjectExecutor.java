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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.project;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.CodeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.CodesSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.IdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.PermIdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.ProjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.AbstractSearchObjectManuallyExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.CodeMatcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.CodesMatcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.Matcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.SimpleFieldMatcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.StringFieldMatcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.space.ISearchSpaceExecutor;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
@Component
public class SearchProjectExecutor extends AbstractSearchObjectManuallyExecutor<ProjectSearchCriteria, ProjectPE> implements ISearchProjectExecutor
{

    @Autowired
    private ISearchSpaceExecutor searchSpaceExecutor;

    @Autowired
    private IProjectAuthorizationExecutor authorizationExecutor;

    @Override
    public List<ProjectPE> search(IOperationContext context, ProjectSearchCriteria criteria)
    {
        authorizationExecutor.canSearch(context);
        return super.search(context, criteria);
    }

    @Override
    protected List<ProjectPE> listAll()
    {
        return daoFactory.getProjectDAO().listAllEntities();
    }

    @Override
    protected Matcher<ProjectPE> getMatcher(ISearchCriteria criteria)
    {
        if (criteria instanceof IdSearchCriteria<?>)
        {
            return new IdMatcher();
        } else if (criteria instanceof CodeSearchCriteria)
        {
            return new CodeMatcher<ProjectPE>();
        } else if (criteria instanceof CodesSearchCriteria)
        {
            return new CodesMatcher<ProjectPE>();
        } else if (criteria instanceof PermIdSearchCriteria)
        {
            return new PermIdMatcher();
        } else if (criteria instanceof SpaceSearchCriteria)
        {
            return new SpaceMatcher();
        } else
        {
            throw new IllegalArgumentException("Unknown search criteria: " + criteria.getClass());
        }
    }

    private class IdMatcher extends SimpleFieldMatcher<ProjectPE>
    {

        @Override
        protected boolean isMatching(IOperationContext context, ProjectPE object, ISearchCriteria criteria)
        {
            Object id = ((IdSearchCriteria<?>) criteria).getId();

            if (id == null)
            {
                return true;
            } else if (id instanceof ProjectPermId)
            {
                return object.getPermId().equals(((ProjectPermId) id).getPermId());
            } else if (id instanceof ProjectIdentifier)
            {
                return object.getIdentifier().equals(((ProjectIdentifier) id).getIdentifier());
            } else
            {
                throw new IllegalArgumentException("Unknown id: " + criteria.getClass());
            }
        }

    }

    private class PermIdMatcher extends StringFieldMatcher<ProjectPE>
    {

        @Override
        protected String getFieldValue(ProjectPE object)
        {
            return object.getPermId();
        }

    }

    private class SpaceMatcher extends Matcher<ProjectPE>
    {

        @Override
        public List<ProjectPE> getMatching(IOperationContext context, List<ProjectPE> objects, ISearchCriteria criteria)
        {
            List<SpacePE> spaceList = searchSpaceExecutor.search(context, (SpaceSearchCriteria) criteria);
            Set<SpacePE> spaceSet = new HashSet<SpacePE>(spaceList);

            List<ProjectPE> matches = new ArrayList<ProjectPE>();

            for (ProjectPE object : objects)
            {
                if (spaceSet.contains(object.getSpace()))
                {
                    matches.add(object);
                }
            }

            return matches;
        }

    }

}
