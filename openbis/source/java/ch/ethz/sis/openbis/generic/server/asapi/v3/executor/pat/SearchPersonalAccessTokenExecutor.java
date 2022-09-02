/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.pat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.IdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.id.PersonalAccessTokenPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.search.PersonalAccessTokenOwnerSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.search.PersonalAccessTokenSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.search.PersonalAccessTokenSessionNameSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.PersonSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.AbstractSearchObjectManuallyExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.Matcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.SimpleFieldMatcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.StringFieldMatcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.person.ISearchPersonExecutor;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPersonalAccessTokenDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonalAccessToken;

/**
 * @author pkupczyk
 */
@Component
public class SearchPersonalAccessTokenExecutor extends AbstractSearchObjectManuallyExecutor<PersonalAccessTokenSearchCriteria, PersonalAccessToken>
        implements ISearchPersonalAccessTokenExecutor
{

    @Autowired
    private IPersonalAccessTokenAuthorizationExecutor authorizationExecutor;

    @Autowired
    private IPersonalAccessTokenDAO personalAccessTokenDAO;

    @Autowired
    private ISearchPersonExecutor searchPersonExecutor;

    @Override
    public List<PersonalAccessToken> search(IOperationContext context, PersonalAccessTokenSearchCriteria criteria)
    {
        authorizationExecutor.canSearch(context);
        return super.search(context, criteria);
    }

    @Override
    protected List<PersonalAccessToken> listAll()
    {
        return personalAccessTokenDAO.listTokens();
    }

    @Override
    protected Matcher<PersonalAccessToken> getMatcher(ISearchCriteria criteria)
    {
        if (criteria instanceof IdSearchCriteria<?>)
        {
            return new IdMatcher();
        } else if (criteria instanceof PersonalAccessTokenOwnerSearchCriteria)
        {
            return new OwnerMatcher(searchPersonExecutor);
        } else if (criteria instanceof PersonalAccessTokenSessionNameSearchCriteria)
        {
            return new SessionNameMatcher();
        } else
        {
            throw new IllegalArgumentException("Unknown search criteria: " + criteria.getClass());
        }
    }

    private static class IdMatcher extends SimpleFieldMatcher<PersonalAccessToken>
    {

        @Override
        protected boolean isMatching(IOperationContext context, PersonalAccessToken object, ISearchCriteria criteria)
        {
            Object id = ((IdSearchCriteria<?>) criteria).getId();

            if (id == null)
            {
                return true;
            } else if (id instanceof PersonalAccessTokenPermId)
            {
                return id.equals(new PersonalAccessTokenPermId(object.getHash()));
            } else
            {
                throw new IllegalArgumentException("Unknown id: " + id.getClass());
            }
        }

    }

    private static class OwnerMatcher extends Matcher<PersonalAccessToken>
    {

        private ISearchPersonExecutor searchPersonExecutor;

        public OwnerMatcher(ISearchPersonExecutor searchPersonExecutor)
        {
            this.searchPersonExecutor = searchPersonExecutor;
        }

        @Override
        public List<PersonalAccessToken> getMatching(IOperationContext context, List<PersonalAccessToken> objects, ISearchCriteria criteria)
        {
            List<PersonPE> persons = searchPersonExecutor.search(context, (PersonSearchCriteria) criteria);

            Set<String> personIds = new HashSet<>();
            for (PersonPE person : persons)
            {
                personIds.add(person.getUserId());
            }

            List<PersonalAccessToken> matches = new ArrayList<>();
            for (PersonalAccessToken object : objects)
            {
                if (personIds.contains(object.getOwnerId()))
                {
                    matches.add(object);
                }
            }

            return matches;
        }

    }

    private static class SessionNameMatcher extends StringFieldMatcher<PersonalAccessToken>
    {

        @Override
        protected String getFieldValue(PersonalAccessToken object)
        {
            return object.getSessionName();
        }

    }
}
