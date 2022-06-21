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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.pat;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.fetchoptions.PersonalAccessTokenFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.id.PersonalAccessTokenPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.IPersonId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.PersonPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.Query;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.QueryType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.fetchoptions.QueryFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.QueryDatabaseName;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.QueryTechId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.roleassignment.RoleAssignmentUtils;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationResults;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.person.IPersonTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.query.IQueryAuthorizationValidator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.query.IQueryBaseTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.query.IQueryRegistratorTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.query.IQueryTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.query.QueryBaseRecord;
import ch.systemsx.cisd.authentication.pat.PersonalAccessToken;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.plugin.query.shared.DatabaseDefinition;
import ch.systemsx.cisd.openbis.plugin.query.shared.IQueryDatabaseDefinitionProvider;

/**
 * @author pkupczyk
 */
@Component
public class PersonalAccessTokenTranslator extends
        AbstractCachingTranslator<PersonalAccessToken, ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.PersonalAccessToken, PersonalAccessTokenFetchOptions>
        implements IPersonalAccessTokenTranslator
{

    @Autowired
    private IPersonTranslator personTranslator;

    @Override protected Set<PersonalAccessToken> shouldTranslate(final TranslationContext context, final Collection<PersonalAccessToken> pats,
            final PersonalAccessTokenFetchOptions fetchOptions)
    {
        PersonPE person = context.getSession().tryGetPerson();

        if (person == null)
        {
            return Collections.emptySet();
        }

        if (person.isSystemUser() || RoleAssignmentUtils.isInstanceAdmin(person))
        {
            return new HashSet<>(pats);
        }

        Set<PersonalAccessToken> filtered = new HashSet<>();

        for (PersonalAccessToken pat : pats)
        {
            if (person.getId().equals(pat.getOwnerId()))
            {
                filtered.add(pat);
            }
        }

        return filtered;
    }

    @Override
    protected ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.PersonalAccessToken createObject(TranslationContext context, PersonalAccessToken pat,
            PersonalAccessTokenFetchOptions fetchOptions)
    {
        ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.PersonalAccessToken patV3 =
                new ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.PersonalAccessToken();

        patV3.setPermId(new PersonalAccessTokenPermId(pat.getHash()));
        patV3.setHash(pat.getHash());
        patV3.setSessionName(pat.getSessionName());
        patV3.setValidFromDate(pat.getValidFromDate());
        patV3.setValidToDate(pat.getValidToDate());
        patV3.setRegistrationDate(pat.getRegistrationDate());
        patV3.setModificationDate(pat.getModificationDate());
        patV3.setAccessDate(pat.getAccessDate());
        patV3.setFetchOptions(new PersonalAccessTokenFetchOptions());

        return patV3;
    }

    @Override
    protected Object getObjectsRelations(TranslationContext context, Collection<PersonalAccessToken> pats,
            PersonalAccessTokenFetchOptions fetchOptions)
    {
        Collection<Long> ownerIds = new HashSet<>();
        Collection<Long> registratorIds = new HashSet<>();
        Collection<Long> modifierIds = new HashSet<>();

        for (PersonalAccessToken pat : pats)
        {
            if (fetchOptions.hasOwner())
            {
                ownerIds.add(pat.getOwnerId());
            }
            if (fetchOptions.hasRegistrator())
            {
                registratorIds.add(pat.getRegistratorId());
            }
            if (fetchOptions.hasModifier())
            {
                modifierIds.add(pat.getModifierId());
            }
        }

        Relations relations = new Relations();

        if (!ownerIds.isEmpty())
        {
            relations.owners = personTranslator.translate(context, ownerIds, fetchOptions.withOwner());
        }
        if (!registratorIds.isEmpty())
        {
            relations.registrators = personTranslator.translate(context, registratorIds, fetchOptions.withRegistrator());
        }
        if (!modifierIds.isEmpty())
        {
            relations.modifiers = personTranslator.translate(context, modifierIds, fetchOptions.withModifier());
        }

        return relations;
    }

    @Override
    protected void updateObject(TranslationContext context, PersonalAccessToken pat,
            ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.PersonalAccessToken patV3, Object objectRelations,
            PersonalAccessTokenFetchOptions fetchOptions)
    {
        Relations relations = (Relations) objectRelations;

        if (fetchOptions.hasOwner())
        {
            patV3.setOwner(relations.owners.get(pat.getOwnerId()));
            patV3.getFetchOptions().withOwnerUsing(fetchOptions.withOwner());
        }

        if (fetchOptions.hasRegistrator())
        {
            patV3.setRegistrator(relations.registrators.get(pat.getRegistratorId()));
            patV3.getFetchOptions().withRegistratorUsing(fetchOptions.withRegistrator());
        }

        if (fetchOptions.hasModifier())
        {
            patV3.setModifier(relations.modifiers.get(pat.getModifierId()));
            patV3.getFetchOptions().withModifierUsing(fetchOptions.withModifier());
        }
    }

    private static class Relations
    {
        private Map<Long, Person> owners;

        private Map<Long, Person> registrators;

        private Map<Long, Person> modifiers;
    }

}
