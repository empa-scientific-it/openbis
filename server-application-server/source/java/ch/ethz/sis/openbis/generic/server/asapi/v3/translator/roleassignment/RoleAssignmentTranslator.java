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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.roleassignment;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.Role;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.RoleAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.RoleLevel;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.fetchoptions.RoleAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.id.RoleAssignmentTechId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.OperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.roleassignment.IRoleAssignmentAuthorizationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationResults;
import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.openbis.generic.shared.authorization.IAuthorizationConfig;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class RoleAssignmentTranslator
        extends AbstractCachingTranslator<Long, RoleAssignment, RoleAssignmentFetchOptions>
        implements IRoleAssignmentTranslator
{
    @Autowired
    private IRoleAssignmentAuthorizationExecutor authorizationExecutor;

    @Autowired
    private IRoleAssignmentBaseTranslator baseTranslator;

    @Autowired
    private IRoleAssignmentRegistratorTranslator registratorTranslator;

    @Autowired
    private IRoleAssignmentPersonTranslator userTranslator;

    @Autowired
    private IRoleAssignmentAuthorizationGroupTranslator authorizationGroupTranslator;

    @Autowired
    private IRoleAssignmentSpaceTranslator spaceTranslator;

    @Autowired
    private IRoleAssignmentProjectTranslator projectTranslator;

    @Autowired
    private IAuthorizationConfig authorizationConfig;

    @Override
    protected Set<Long> shouldTranslate(TranslationContext context, Collection<Long> inputs, RoleAssignmentFetchOptions fetchOptions)
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
    protected void filterTranslated(TranslationContext context, Map<Long, RoleAssignment> translated)
    {
        if (authorizationConfig.isProjectLevelEnabled())
        {
            return;
        }

        Collection<Long> projectRoleIds = new HashSet<Long>();

        for (Map.Entry<Long, RoleAssignment> entry : translated.entrySet())
        {
            if (RoleLevel.PROJECT.equals(entry.getValue().getRoleLevel()))
            {
                projectRoleIds.add(entry.getKey());
            }
        }

        for (Long projectRoleId : projectRoleIds)
        {
            translated.remove(projectRoleId);
        }
    }

    @Override
    protected RoleAssignment createObject(TranslationContext context, Long input, RoleAssignmentFetchOptions fetchOptions)
    {
        RoleAssignment roleAssignment = new RoleAssignment();
        roleAssignment.setFetchOptions(fetchOptions);
        return roleAssignment;
    }

    @Override
    protected Object getObjectsRelations(TranslationContext context, Collection<Long> inputs, RoleAssignmentFetchOptions fetchOptions)
    {
        TranslationResults relations = new TranslationResults();

        relations.put(IRoleAssignmentBaseTranslator.class, baseTranslator.translate(context, inputs, null));
        if (fetchOptions.hasRegistrator())
        {
            relations.put(IRoleAssignmentRegistratorTranslator.class,
                    registratorTranslator.translate(context, inputs, fetchOptions.withRegistrator()));
        }
        if (fetchOptions.hasUser())
        {
            relations.put(IRoleAssignmentPersonTranslator.class, userTranslator.translate(context, inputs, fetchOptions.withUser()));
        }
        if (fetchOptions.hasAuthorizationGroup())
        {
            relations.put(IRoleAssignmentAuthorizationGroupTranslator.class,
                    authorizationGroupTranslator.translate(context, inputs, fetchOptions.withAuthorizationGroup()));
        }
        if (fetchOptions.hasSpace())
        {
            relations.put(IRoleAssignmentSpaceTranslator.class, spaceTranslator.translate(context, inputs, fetchOptions.withSpace()));
        }
        if (fetchOptions.hasProject())
        {
            relations.put(IRoleAssignmentProjectTranslator.class, projectTranslator.translate(context, inputs, fetchOptions.withProject()));
        }

        return relations;
    }

    @Override
    protected void updateObject(TranslationContext context, Long input, RoleAssignment output, Object objectRelations,
            RoleAssignmentFetchOptions fetchOptions)
    {
        TranslationResults relations = (TranslationResults) objectRelations;
        RoleAssignmentBaseRecord baseRecord = relations.get(IRoleAssignmentBaseTranslator.class, input);
        output.setId(new RoleAssignmentTechId(baseRecord.id));
        output.setRole(Role.valueOf(baseRecord.role_code));
        output.setRoleLevel(extractRoleLevel(baseRecord));
        output.setRegistrationDate(baseRecord.registrationDate);

        if (fetchOptions.hasRegistrator())
        {
            output.setRegistrator(relations.get(IRoleAssignmentRegistratorTranslator.class, input));
            output.getFetchOptions().withRegistratorUsing(fetchOptions.withRegistrator());
        }
        if (fetchOptions.hasUser())
        {
            output.setUser(relations.get(IRoleAssignmentPersonTranslator.class, input));
            output.getFetchOptions().withUserUsing(fetchOptions.withUser());
        }
        if (fetchOptions.hasAuthorizationGroup())
        {
            output.setAuthorizationGroup(relations.get(IRoleAssignmentAuthorizationGroupTranslator.class, input));
            output.getFetchOptions().withAuthorizationGroupUsing(fetchOptions.withAuthorizationGroup());
        }
        if (fetchOptions.hasSpace())
        {
            output.setSpace(relations.get(IRoleAssignmentSpaceTranslator.class, input));
            output.getFetchOptions().withSpaceUsing(fetchOptions.withSpace());
        }
        if (fetchOptions.hasProject())
        {
            output.setProject(relations.get(IRoleAssignmentProjectTranslator.class, input));
            output.getFetchOptions().withProjectUsing(fetchOptions.withProject());
        }
    }

    private RoleLevel extractRoleLevel(RoleAssignmentBaseRecord baseRecord)
    {
        if (baseRecord.space_id != null)
        {
            return RoleLevel.SPACE;
        }
        if (baseRecord.project_id != null)
        {
            return RoleLevel.PROJECT;
        }
        return RoleLevel.INSTANCE;
    }

}
