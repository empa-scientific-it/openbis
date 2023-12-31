/*
 * Copyright ETH 2015 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.authorizationgroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.id.AuthorizationGroupPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.search.AuthorizationGroupSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.CodeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.CodesSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.IdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.PermIdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.PersonSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.AbstractSearchObjectManuallyExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.CodeMatcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.CodesMatcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.Matcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.SimpleFieldMatcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.StringFieldMatcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.person.ISearchPersonExecutor;
import ch.systemsx.cisd.openbis.generic.shared.dto.AuthorizationGroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Component
public class SearchAuthorizationGroupExecutor 
        extends AbstractSearchObjectManuallyExecutor<AuthorizationGroupSearchCriteria, AuthorizationGroupPE> 
        implements ISearchAuthorizationGroupExecutor
{
    @Autowired
    private IAuthorizationGroupAuthorizationExecutor authorizationExecutor;

    @Override
    public List<AuthorizationGroupPE> search(IOperationContext context, AuthorizationGroupSearchCriteria criteria)
    {
        authorizationExecutor.canSearch(context);
        return super.search(context, criteria);
    }

    @Override
    protected List<AuthorizationGroupPE> listAll()
    {
        return daoFactory.getAuthorizationGroupDAO().listAllEntities();
    }

    @Override
    protected Matcher<AuthorizationGroupPE> getMatcher(ISearchCriteria criteria)
    {
        if (criteria instanceof IdSearchCriteria<?>)
        {
            return new IdMatcher();
        } else if (criteria instanceof CodeSearchCriteria)
        {
            return new CodeMatcher<AuthorizationGroupPE>();
        } else if (criteria instanceof CodesSearchCriteria)
        {
            return new CodesMatcher<AuthorizationGroupPE>();
        } else if (criteria instanceof PermIdSearchCriteria)
        {
            return new PermIdMatcher();
        } else if (criteria instanceof PersonSearchCriteria)
        {
            return new UserMatcher();
        } else
        {
            throw new IllegalArgumentException("Unknown search criteria: " + criteria.getClass());
        }
    }

    private class IdMatcher extends SimpleFieldMatcher<AuthorizationGroupPE>
    {
        @Override
        protected boolean isMatching(IOperationContext context, AuthorizationGroupPE object, ISearchCriteria criteria)
        {
            Object id = ((IdSearchCriteria<?>) criteria).getId();

            if (id == null)
            {
                return true;
            } else if (id instanceof AuthorizationGroupPermId)
            {
                return object.getCode().equals(((AuthorizationGroupPermId) id).getPermId());
            } else
            {
                throw new IllegalArgumentException("Unknown id: " + criteria.getClass());
            }
        }
    }

    private class PermIdMatcher extends StringFieldMatcher<AuthorizationGroupPE>
    {
        @Override
        protected String getFieldValue(AuthorizationGroupPE object)
        {
            return object.getCode();
        }
    }

    @Autowired
    private ISearchPersonExecutor searchPersonExecutor;
    
    private class UserMatcher extends Matcher<AuthorizationGroupPE>
    {

        @Override
        public List<AuthorizationGroupPE> getMatching(IOperationContext context, List<AuthorizationGroupPE> groups, ISearchCriteria criteria)
        {
            Set<AuthorizationGroupPE> matchingGroups = new LinkedHashSet<>();
            for (PersonPE user : searchPersonExecutor.search(context, (PersonSearchCriteria) criteria))
            {
                for (AuthorizationGroupPE group : groups)
                {
                    if (group.getPersons().contains(user))
                    {
                        matchingGroups.add(group);
                    }
                }
            }
            return new ArrayList<>(matchingGroups);
        }
    }

}
