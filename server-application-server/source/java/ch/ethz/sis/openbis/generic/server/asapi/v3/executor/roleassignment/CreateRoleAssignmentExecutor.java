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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.roleassignment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.ObjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.create.RoleAssignmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.id.RoleAssignmentTechId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractCreateEntityExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.CommonUtils;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatchProcessor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.progress.CreateProgress;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.AuthorizationGroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Component
public class CreateRoleAssignmentExecutor 
        extends AbstractCreateEntityExecutor<RoleAssignmentCreation, RoleAssignmentPE, RoleAssignmentTechId> 
        implements ICreateRoleAssignmentExecutor
{
    @Autowired
    private IRoleAssignmentAuthorizationExecutor authorizationExecutor;

    @Autowired
    private IDAOFactory daoFactory;
    
    @Autowired
    private ISetRoleAssignmentUserExecutor setRoleAssignmentUserExecutor;
    
    @Autowired
    private ISetRoleAssignmentAuthorizationGroupExecutor setRoleAssignmentGroupExecutor;
    
    @Autowired
    private ISetRoleAssignmentSpaceExecutor setRoleAssignmentSpaceExecutor;
    
    @Autowired
    private ISetRoleAssignmentProjectExecutor setRoleAssignmentProjectExecutor;
    
    @Override
    protected IObjectId getId(RoleAssignmentPE entity)
    {
        // Note, we can not return an instance of RoleAssignmentId because entity.getId() == null
        return new ObjectIdentifier(renderAssignment(entity))
            {
                private static final long serialVersionUID = 1L;
            };
    }

    private String renderAssignment(RoleAssignmentPE entity)
    {
        StringBuilder builder = new StringBuilder();
        SpacePE space = entity.getSpace();
        ProjectPE project = entity.getProject();
        if (space != null)
        {
            builder.append("SPACE_");
        } else
        {
            builder.append(project != null ? "PROJECT_" : "INSTANCE_");
        }
        builder.append(entity.getRole());
        if (space != null)
        {
            builder.append(" [Space: ").append(space.getCode()).append("]");
        } else if (project != null)
        {
            builder.append(" [Project: ").append(project.getIdentifier()).append("]");
        }
        PersonPE user = entity.getPerson();
        if (user != null)
        {
            builder.append(" for user ").append(user.getUserId());
        } else
        {
            AuthorizationGroupPE group = entity.getAuthorizationGroup();
            if (group != null)
            {
                builder.append(" for authorization group ").append(group.getCode());
            }
        }
        return builder.toString();
    }

    @Override
    protected void checkData(IOperationContext context, RoleAssignmentCreation creation)
    {
        if (creation.getRole() == null)
        {
            throw new UserFailureException("Unspecified role.");
        }
        if (creation.getUserId() == null && creation.getAuthorizationGroupId() == null)
        {
            throw new UserFailureException("Either a user or an authorization group has to be specified.");
        }
        if (creation.getUserId() != null && creation.getAuthorizationGroupId() != null)
        {
            throw new UserFailureException("A user and an authorization group have been specified.");
        }
        if (creation.getSpaceId() != null && creation.getProjectId() != null)
        {
            throw new UserFailureException("A space and a project have been specified.");
        }
    }

    @Override
    protected void checkAccess(IOperationContext context)
    {
    }

    @Override
    protected void checkAccess(IOperationContext context, RoleAssignmentPE entity)
    {
        ProjectPE project = entity.getProject();
        if (project != null)
        {
            authorizationExecutor.canCreateProjectRole(context, project);
        } else 
        {
            SpacePE space = entity.getSpace();
            if (space != null)
            {
                authorizationExecutor.canCreateSpaceRole(context, space);
            } else
            {
                authorizationExecutor.canCreateInstanceRole(context);
            }
        }
    }

    @Override
    protected List<RoleAssignmentPE> createEntities(IOperationContext context, CollectionBatch<RoleAssignmentCreation> batch)
    {
        final List<RoleAssignmentPE> roleAssignments = new LinkedList<>();
        new CollectionBatchProcessor<RoleAssignmentCreation>(context, batch)
            {
                @Override
                public void process(RoleAssignmentCreation object)
                {
                    RoleAssignmentPE roleAssignment = new RoleAssignmentPE();
                    roleAssignment.setRole(RoleCode.valueOf(object.getRole().name()));
                    roleAssignment.setRegistrator(context.getSession().tryGetCreatorPerson());
                    roleAssignments.add(roleAssignment);
                }

                @Override
                public IProgress createProgress(RoleAssignmentCreation object, int objectIndex, int totalObjectCount)
                {
                    return new CreateProgress(object, objectIndex, totalObjectCount);
                }
            };
        return roleAssignments;
    }

    @Override
    protected RoleAssignmentTechId createPermId(IOperationContext context, RoleAssignmentPE entity)
    {
        return new RoleAssignmentTechId(entity.getId());
    }

    @Override
    protected void updateBatch(IOperationContext context, MapBatch<RoleAssignmentCreation, RoleAssignmentPE> batch)
    {
        setRoleAssignmentUserExecutor.set(context, batch);
        setRoleAssignmentGroupExecutor.set(context, batch);
        setRoleAssignmentSpaceExecutor.set(context, batch);
        setRoleAssignmentProjectExecutor.set(context, batch);
    }

    @Override
    protected void updateAll(IOperationContext context, MapBatch<RoleAssignmentCreation, RoleAssignmentPE> batch)
    {
    }

    @Override
    protected List<RoleAssignmentPE> list(IOperationContext context, Collection<Long> ids)
    {
        Set<Long> idSet = CommonUtils.asSet(ids);
        List<RoleAssignmentPE> result = new ArrayList<>();
        List<RoleAssignmentPE> entities = daoFactory.getRoleAssignmentDAO().listAllEntities();
        for (RoleAssignmentPE roleAssignment : entities)
        {
            if (idSet.contains(roleAssignment.getId()))
            {
                result.add(roleAssignment);
            }
        }
        return result;
    }

    @Override
    protected void save(IOperationContext context, List<RoleAssignmentPE> entities, boolean clearCache)
    {
        for (RoleAssignmentPE roleAssignment : entities)
        {
            daoFactory.getRoleAssignmentDAO().createRoleAssignment(roleAssignment);
        }
    }

    @Override
    protected void handleException(DataAccessException e)
    {
        DataAccessExceptionTranslator.throwException(e, "role assignment", null);
    }

}
