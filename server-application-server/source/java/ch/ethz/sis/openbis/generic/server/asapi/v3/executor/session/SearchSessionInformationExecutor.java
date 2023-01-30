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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.session;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.IdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.id.SessionInformationPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.search.PersonalAccessTokenSessionNameSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.search.PersonalAccessTokenSessionSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.search.SessionInformationSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.search.UserNameSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.AbstractSearchObjectManuallyExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.Matcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.SimpleFieldMatcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.StringFieldMatcher;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.shared.IOpenBisSessionManager;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * @author pkupczyk
 */
@Component
public class SearchSessionInformationExecutor extends AbstractSearchObjectManuallyExecutor<SessionInformationSearchCriteria, Session>
        implements ISearchSessionInformationExecutor
{

    @Autowired
    private IOpenBisSessionManager sessionManager;

    @Autowired
    private ISessionInformationAuthorizationExecutor authorizationExecutor;

    @Override
    public List<Session> search(IOperationContext context, SessionInformationSearchCriteria criteria)
    {
        authorizationExecutor.canSearch(context);
        return super.search(context, criteria);
    }

    @Override
    protected List<Session> listAll()
    {
        return sessionManager.getSessions();
    }

    @Override
    protected Matcher<Session> getMatcher(ISearchCriteria criteria)
    {
        if (criteria instanceof IdSearchCriteria<?>)
        {
            return new IdMatcher();
        } else if (criteria instanceof UserNameSearchCriteria)
        {
            return new UserNameMatcher();
        } else if (criteria instanceof PersonalAccessTokenSessionSearchCriteria)
        {
            return new PersonalAccessTokenSessionMatcher();
        } else if (criteria instanceof PersonalAccessTokenSessionNameSearchCriteria)
        {
            return new PersonalAccessTokenSessionNameMatcher();
        } else
        {
            throw new IllegalArgumentException("Unknown search criteria: " + criteria.getClass());
        }
    }

    private static class IdMatcher extends SimpleFieldMatcher<Session>
    {
        @Override
        protected boolean isMatching(IOperationContext context, Session object, ISearchCriteria criteria)
        {
            Object id = ((IdSearchCriteria<?>) criteria).getId();

            if (id == null)
            {
                return true;
            } else if (id instanceof SessionInformationPermId)
            {
                return id.equals(new SessionInformationPermId(object.getSessionToken()));
            } else
            {
                throw new IllegalArgumentException("Unknown id: " + id.getClass());
            }
        }
    }

    private static class UserNameMatcher extends StringFieldMatcher<Session>
    {
        @Override protected String getFieldValue(final Session object)
        {
            return object.getUserName();
        }
    }

    private static class PersonalAccessTokenSessionMatcher extends SimpleFieldMatcher<Session>
    {
        @Override
        protected boolean isMatching(IOperationContext context, Session object, ISearchCriteria criteria)
        {
            Boolean flag = ((PersonalAccessTokenSessionSearchCriteria) criteria).getFieldValue();

            if (flag == null)
            {
                return true;
            } else
            {
                return flag == object.isPersonalAccessTokenSession();
            }
        }
    }

    private static class PersonalAccessTokenSessionNameMatcher extends StringFieldMatcher<Session>
    {
        @Override protected String getFieldValue(final Session object)
        {
            return object.getPersonalAccessTokenSessionName();
        }
    }

}
