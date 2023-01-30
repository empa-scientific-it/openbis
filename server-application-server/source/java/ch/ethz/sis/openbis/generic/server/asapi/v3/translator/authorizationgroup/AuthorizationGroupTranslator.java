/*
 * Copyright 2017 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.authorizationgroup;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.AuthorizationGroup;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.fetchoptions.AuthorizationGroupFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.id.AuthorizationGroupPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.RoleAssignment;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.OperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.authorizationgroup.IAuthorizationGroupAuthorizationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.authorizationgroup.IAuthorizationGroupTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationResults;
import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Component
public class AuthorizationGroupTranslator extends AbstractCachingTranslator<Long, AuthorizationGroup, AuthorizationGroupFetchOptions>  implements IAuthorizationGroupTranslator
{
    @Autowired
    private IAuthorizationGroupAuthorizationExecutor authorizationExecutor;
    
    @Autowired
    private IAuthorizationGroupBaseTranslator baseTranslator;
    
    @Autowired
    private IAuthorizationGroupRegistratorTranslator registratorTranslator;
    
    @Autowired
    private IAuthorizationGroupUserTranslator userTranslator;

    @Autowired
    private IAuthorizationGroupRoleAssignmentTranslator roleAssignmentTranslator;

    @Override
    protected Set<Long> shouldTranslate(TranslationContext context, Collection<Long> inputs, AuthorizationGroupFetchOptions fetchOptions)
    {
        try
        {
            authorizationExecutor.canGet(new OperationContext(context.getSession()));
            return new HashSet<>(inputs);
        } catch (AuthorizationFailureException ex)
        {
            return new HashSet<>();
        }
    }

    @Override
    protected AuthorizationGroup createObject(TranslationContext context, Long input, AuthorizationGroupFetchOptions fetchOptions)
    {
        AuthorizationGroup authorizationGroup = new AuthorizationGroup();
        authorizationGroup.setFetchOptions(fetchOptions);
        return authorizationGroup;
    }

    @Override
    protected TranslationResults getObjectsRelations(TranslationContext context, Collection<Long> ids, AuthorizationGroupFetchOptions fetchOptions)
    {
        TranslationResults relations = new TranslationResults();
        relations.put(IAuthorizationGroupBaseTranslator.class, baseTranslator.translate(context, ids, null));
        
        if (fetchOptions.hasRegistrator())
        {
            relations.put(IAuthorizationGroupRegistratorTranslator.class, registratorTranslator.translate(context, ids, fetchOptions.withRegistrator()));
        }
        if (fetchOptions.hasUsers())
        {
            relations.put(IAuthorizationGroupUserTranslator.class, userTranslator.translate(context, ids, fetchOptions.withUsers()));
        }
        if (fetchOptions.hasRoleAssignments())
        {
            relations.put(IAuthorizationGroupRoleAssignmentTranslator.class, roleAssignmentTranslator.translate(context, ids, fetchOptions.withRoleAssignments()));
        }
        
        return relations;
    }

    @Override
    protected void updateObject(TranslationContext context, Long id, AuthorizationGroup group, Object objectRelations,
            AuthorizationGroupFetchOptions fetchOptions)
    {
        TranslationResults relations = (TranslationResults) objectRelations;
        AuthorizationGroupBaseRecord baseRecord = relations.get(IAuthorizationGroupBaseTranslator.class, id);
        group.setPermId(new AuthorizationGroupPermId(baseRecord.code));
        group.setCode(baseRecord.code);
        group.setDescription(baseRecord.description);
        group.setRegistrationDate(baseRecord.registrationDate);
        group.setModificationDate(baseRecord.modificationDate);
        
        if (fetchOptions.hasRegistrator())
        {
            group.setRegistrator(relations.get(IAuthorizationGroupRegistratorTranslator.class, id));
            group.getFetchOptions().withRegistratorUsing(fetchOptions.withRegistrator());
        }
        if (fetchOptions.hasUsers())
        {
            group.setUsers((List<Person>) relations.get(IAuthorizationGroupUserTranslator.class, id));
            group.getFetchOptions().withUsersUsing(fetchOptions.withUsers());
        }
        if (fetchOptions.hasRoleAssignments())
        {
            group.setRoleAssignments((List<RoleAssignment>) relations.get(IAuthorizationGroupRoleAssignmentTranslator.class, id));
            group.getFetchOptions().withRoleAssignmentsUsing(fetchOptions.withRoleAssignments());
        }
    }

}
