/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.session;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.fetchoptions.PersonalAccessTokenFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.id.PersonalAccessTokenPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.SessionInformation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.fetchoptions.SessionInformationFetchOptions;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.roleassignment.RoleAssignmentUtils;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.person.IPersonTranslator;
import ch.systemsx.cisd.authentication.pat.PersonalAccessToken;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSession;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * @author pkupczyk
 */
@Component
public class SessionInformationTranslator extends
        AbstractCachingTranslator<Session, SessionInformation, SessionInformationFetchOptions>
        implements ISessionInformationTranslator
{

    @Autowired
    private IPersonTranslator personTranslator;

    @Autowired
    private IDAOFactory daoFactory;

    @Override
    protected SessionInformation createObject(TranslationContext context, Session session,
            SessionInformationFetchOptions fetchOptions)
    {
        SessionInformation sessionInformation = new SessionInformation();

        sessionInformation.setSessionToken(session.getSessionToken());
        sessionInformation.setUserName(session.getUserName());
        sessionInformation.setHomeGroupCode(session.tryGetHomeGroupCode());
        sessionInformation.setPersonalAccessTokenSession(session.isPersonalAccessTokenSession());
        sessionInformation.setPersonalAccessTokenSessionName(session.getPersonalAccessTokenSessionName());

        return sessionInformation;
    }

    @Override
    protected Object getObjectsRelations(TranslationContext context, Collection<Session> sessions,
            SessionInformationFetchOptions fetchOptions)
    {
        Collection<Long> personIds = new HashSet<>();
        Collection<Long> creatorIds = new HashSet<>();

        for (Session session : sessions)
        {
            if (fetchOptions.hasPerson() && session.tryGetPerson() != null)
            {
                personIds.add(session.tryGetPerson().getId());
            }
            if (fetchOptions.hasCreatorPerson() && session.tryGetCreatorPerson() != null)
            {
                creatorIds.add(session.tryGetCreatorPerson().getId());
            }
        }

        Relations relations = new Relations();

        if (!personIds.isEmpty())
        {
            relations.persons = personTranslator.translate(context, personIds, fetchOptions.withPerson());
        }
        if (!creatorIds.isEmpty())
        {
            relations.creators = personTranslator.translate(context, creatorIds, fetchOptions.withCreatorPerson());
        }

        return relations;
    }

    @Override
    protected void updateObject(TranslationContext context, Session session,
            SessionInformation sessionInformation, Object objectRelations,
            SessionInformationFetchOptions fetchOptions)
    {
        Relations relations = (Relations) objectRelations;

        if (fetchOptions.hasPerson())
        {
            if (session.tryGetPerson() != null)
            {
                sessionInformation.setPerson(relations.persons.get(session.tryGetPerson().getId()));
            }
            sessionInformation.getFetchOptions().withPersonUsing(fetchOptions.withPerson());
        }

        if (fetchOptions.hasCreatorPerson())
        {
            if (session.tryGetCreatorPerson() != null)
            {
                sessionInformation.setCreatorPerson(relations.creators.get(session.tryGetCreatorPerson().getId()));
            }
            sessionInformation.getFetchOptions().withCreatorPersonUsing(fetchOptions.withCreatorPerson());
        }
    }

    private static class Relations
    {
        private Map<Long, Person> persons;

        private Map<Long, Person> creators;
    }

}
