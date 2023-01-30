/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.space;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.SpaceCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractCreateEntityExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatchProcessor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.progress.CreateProgress;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationServiceUtils;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IRoleAssignmentTable;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Grantee;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewRoleAssignment;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifierFactory;

/**
 * @author pkupczyk
 */
@Component
public class CreateSpaceExecutor extends AbstractCreateEntityExecutor<SpaceCreation, SpacePE, SpacePermId> implements
        ICreateSpaceExecutor
{

    @Resource(name = ComponentNames.COMMON_BUSINESS_OBJECT_FACTORY)
    ICommonBusinessObjectFactory businessObjectFactory;

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private ISpaceAuthorizationExecutor authorizationExecutor;

    @Override
    protected List<SpacePE> createEntities(final IOperationContext context, CollectionBatch<SpaceCreation> batch)
    {
        final List<SpacePE> spaces = new LinkedList<SpacePE>();

        new CollectionBatchProcessor<SpaceCreation>(context, batch)
            {
                @Override
                public void process(SpaceCreation object)
                {
                    SpacePE space = new SpacePE();
                    space.setCode(object.getCode());
                    space.setDescription(object.getDescription());
                    space.setRegistrator(context.getSession().tryGetPerson());
                    spaces.add(space);
                }

                @Override
                public IProgress createProgress(SpaceCreation object, int objectIndex, int totalObjectCount)
                {
                    return new CreateProgress(object, objectIndex, totalObjectCount);
                }
            };

        return spaces;
    }

    @Override
    protected SpacePermId createPermId(IOperationContext context, SpacePE entity)
    {
        return new SpacePermId(entity.getCode());
    }

    @Override
    protected void checkData(IOperationContext context, SpaceCreation creation)
    {
        if (StringUtils.isEmpty(creation.getCode()))
        {
            throw new UserFailureException("Code cannot be empty.");
        }

        SpaceIdentifierFactory.assertValidCode(creation.getCode());
    }

    @Override
    protected void checkAccess(IOperationContext context)
    {

    }

    @Override
    protected void checkAccess(IOperationContext context, SpacePE entity)
    {
        authorizationExecutor.canCreate(context, entity);
    }

    @Override
    protected void updateBatch(IOperationContext context, MapBatch<SpaceCreation, SpacePE> batch)
    {
        // nothing to do
    }

    @Override
    protected void updateAll(IOperationContext context, MapBatch<SpaceCreation, SpacePE> batch)
    {
        // nothing to do
    }

    @Override
    protected List<SpacePE> list(IOperationContext context, Collection<Long> ids)
    {
        return daoFactory.getSpaceDAO().listByIDs(ids);
    }

    @Override
    protected void save(IOperationContext context, List<SpacePE> entities, boolean clearCache)
    {
        for (SpacePE entity : entities)
        {
            daoFactory.getSpaceDAO().createSpace(entity);
        }

        // If the user who registers this space is _not_ instance admin,
        // make him space admin for the freshly created space.

        Session session = context.getSession();
        PersonPE person = session.tryGetPerson();

        if (person != null
                && person.isSystemUser() == false
                && new AuthorizationServiceUtils(daoFactory, session.getUserName())
                        .doesUserHaveRole(RoleCode.ADMIN.toString(), null) == false)
        {
            final IRoleAssignmentTable table = businessObjectFactory.createRoleAssignmentTable(session);

            for (SpacePE entity : entities)
            {
                final NewRoleAssignment newRoleAssignment = new NewRoleAssignment();
                newRoleAssignment.setSpaceIdentifier(new SpaceIdentifier(entity.getCode()));

                newRoleAssignment.setGrantee(Grantee.createPerson(session.getUserName()));
                newRoleAssignment.setRole(RoleCode.ADMIN);

                table.add(newRoleAssignment);
            }

            table.save();
        }

        daoFactory.getSessionFactory().getCurrentSession().flush();
    }

    @Override
    protected void handleException(DataAccessException e)
    {
        DataAccessExceptionTranslator.throwException(e, "space", null);
    }

    @Override
    protected IObjectId getId(SpacePE entity)
    {
        return new SpacePermId(entity.getPermId());
    }

}
