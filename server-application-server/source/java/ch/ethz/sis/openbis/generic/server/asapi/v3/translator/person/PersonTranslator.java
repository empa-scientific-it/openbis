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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.person;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.PersonPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.RoleAssignment;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationResults;

/**
 * @author pkupczyk
 */
@Component
public class PersonTranslator extends AbstractCachingTranslator<Long, Person, PersonFetchOptions> implements IPersonTranslator
{

    @Autowired
    private IPersonBaseTranslator baseTranslator;

    @Autowired
    private IPersonSpaceTranslator spaceTranslator;

    @Autowired
    private IPersonRegistratorTranslator registratorTranslator;

    @Autowired
    private IPersonRoleAssignmentTranslator roleAssignmentTranslator;

    @Autowired
    private IPersonWebAppSettingsTranslator webAppSettingsTranslator;

    @Override
    protected Person createObject(TranslationContext context, Long personId, PersonFetchOptions fetchOptions)
    {
        Person result = new Person();
        result.setFetchOptions(new PersonFetchOptions());
        return result;
    }

    @Override
    protected TranslationResults getObjectsRelations(TranslationContext context, Collection<Long> personIds, PersonFetchOptions fetchOptions)
    {
        TranslationResults relations = new TranslationResults();

        relations.put(IPersonBaseTranslator.class, baseTranslator.translate(context, personIds, null));

        if (fetchOptions.hasSpace())
        {
            relations.put(IPersonSpaceTranslator.class, spaceTranslator.translate(context, personIds, fetchOptions.withSpace()));
        }

        if (fetchOptions.hasRegistrator())
        {
            relations.put(IPersonRegistratorTranslator.class, registratorTranslator.translate(context, personIds, fetchOptions.withRegistrator()));
        }

        if (fetchOptions.hasRoleAssignments())
        {
            relations.put(IPersonRoleAssignmentTranslator.class,
                    roleAssignmentTranslator.translate(context, personIds, fetchOptions.withRoleAssignments()));
        }

        if (fetchOptions.hasAllWebAppSettings() || (fetchOptions.getWebAppSettings() != null && false == fetchOptions.getWebAppSettings().isEmpty()))
        {
            relations.put(IPersonWebAppSettingsTranslator.class, webAppSettingsTranslator.translate(context, personIds, fetchOptions));
        }

        return relations;
    }

    @Override
    protected void updateObject(TranslationContext context, Long personId, Person result, Object objectRelations,
            PersonFetchOptions fetchOptions)
    {
        TranslationResults relations = (TranslationResults) objectRelations;
        PersonBaseRecord baseRecord = relations.get(IPersonBaseTranslator.class, personId);

        result.setFirstName(baseRecord.firstName);
        result.setLastName(baseRecord.lastName);
        result.setUserId(baseRecord.userId);
        result.setPermId(new PersonPermId(baseRecord.userId));
        result.setEmail(baseRecord.email);
        result.setRegistrationDate(baseRecord.registrationDate);
        result.setActive(baseRecord.isActive);

        if (fetchOptions.hasSpace())
        {
            result.setSpace(relations.get(IPersonSpaceTranslator.class, personId));
            result.getFetchOptions().withSpaceUsing(fetchOptions.withSpace());
        }

        if (fetchOptions.hasRegistrator())
        {
            result.setRegistrator(relations.get(IPersonRegistratorTranslator.class, personId));
            result.getFetchOptions().withRegistratorUsing(fetchOptions.withRegistrator());
        }

        if (fetchOptions.hasRoleAssignments())
        {
            result.setRoleAssignments((List<RoleAssignment>) relations.get(IPersonRoleAssignmentTranslator.class, personId));
            result.getFetchOptions().withRoleAssignmentsUsing(fetchOptions.withRoleAssignments());
        }

        if (fetchOptions.hasAllWebAppSettings() || (fetchOptions.getWebAppSettings() != null && false == fetchOptions.getWebAppSettings().isEmpty()))
        {
            result.setWebAppSettings(relations.get(IPersonWebAppSettingsTranslator.class, personId));
            result.getFetchOptions().withWebAppSettingsUsing(fetchOptions.getWebAppSettings());
            result.getFetchOptions().withAllWebAppSettingsUsing(fetchOptions.hasAllWebAppSettings());
        }
    }
}
