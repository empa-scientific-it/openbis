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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.person;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.IPersonId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.Me;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.PersonPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.update.PersonUpdate;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractUpdateEntityExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.CommonUtils;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IRoleAssignmentDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class UpdatePersonExecutor
        extends AbstractUpdateEntityExecutor<PersonUpdate, PersonPE, IPersonId, PersonPermId>
        implements IUpdatePersonExecutor
{
    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private IPersonAuthorizationExecutor authorizationExecutor;

    @Autowired
    private IMapPersonByIdExecutor mapPersonByIdExecutor;

    @Autowired
    private IUpdateHomeSpaceExecutor updateHomeSpaceExecutor;

    @Autowired
    private IUpdateWebAppSettingsExecutor updateWebAppSettingsExecutor;

    @Override
    protected IPersonId getId(PersonUpdate update)
    {
        return update.getUserId();
    }

    @Override
    protected PersonPermId getPermId(PersonPE entity)
    {
        return new PersonPermId(entity.getUserId());
    }

    @Override
    protected void checkData(IOperationContext context, PersonUpdate update)
    {
        IPersonId personId = update.getUserId();
        if (personId == null || personId instanceof Me)
        {
            PersonPE person = context.getSession().tryGetPerson();
            if (person != null)
            {
                update.setUserId(new PersonPermId(person.getUserId()));
            } else
            {
                throw new UserFailureException("Person to be updated not specified.");
            }
        }
    }

    @Override
    protected void checkAccess(IOperationContext context, IPersonId id, PersonPE entity)
    {
        if (entity.isActive() == false)
        {
            authorizationExecutor.canDeactivate(context);
        }
    }

    @Override
    protected void updateBatch(IOperationContext context, MapBatch<PersonUpdate, PersonPE> batch)
    {
        updateHomeSpaceExecutor.update(context, batch);
        updateWebAppSettingsExecutor.update(context, batch);
        Set<Entry<PersonUpdate, PersonPE>> entrySet = batch.getObjects().entrySet();
        for (Entry<PersonUpdate, PersonPE> entry : entrySet)
        {
            PersonUpdate personUpdate = entry.getKey();
            PersonPE person = entry.getValue();

            if (personUpdate.isActive() != null && personUpdate.isActive().isModified() && personUpdate.isActive().getValue() != null)
            {
                if (person.isActive() == true && personUpdate.isActive().getValue() == false)
                {
                    deactivate(context, person);
                } else if (person.isActive() == false && personUpdate.isActive().getValue() == true)
                {
                    activate(context, person);
                }
            }
        }
    }

    private void deactivate(IOperationContext context, PersonPE person)
    {
        authorizationExecutor.canDeactivate(context);
        if (person.equals(context.getSession().tryGetPerson()))
        {
            throw new UserFailureException("You can not deactivate yourself. Ask another instance admin to do that for you.");
        }
        IRoleAssignmentDAO roleAssignmenDAO = daoFactory.getRoleAssignmentDAO();
        person.setActive(false);
        person.setDisplaySettings(null);
        person.clearAuthorizationGroups();
        // Direct iteration over role assignments could lead to a
        // ConcurrentModificationException because roleAssignmentDAO.deleteRoleAssignment()
        // will remove the assignment from person.
        List<RoleAssignmentPE> roleAssignments =
                new ArrayList<RoleAssignmentPE>(person.getRoleAssignments());
        for (RoleAssignmentPE roleAssignment : roleAssignments)
        {
            roleAssignmenDAO.deleteRoleAssignment(roleAssignment);
        }
    }

    private void activate(IOperationContext context, PersonPE person)
    {
        authorizationExecutor.canActivate(context);
        person.setActive(true);
    }

    @Override
    protected void updateAll(IOperationContext context, MapBatch<PersonUpdate, PersonPE> batch)
    {
    }

    @Override
    protected Map<IPersonId, PersonPE> map(IOperationContext context, Collection<IPersonId> ids)
    {
        return mapPersonByIdExecutor.map(context, ids);
    }

    @Override
    protected List<PersonPE> list(IOperationContext context, Collection<Long> ids)
    {
        return CommonUtils.listPersons(daoFactory, ids);
    }

    @Override
    protected void save(IOperationContext context, List<PersonPE> entities, boolean clearCache)
    {
        for (PersonPE person : entities)
        {
            daoFactory.getPersonDAO().updatePerson(person);
        }
    }

    @Override
    protected void handleException(DataAccessException e)
    {
        DataAccessExceptionTranslator.throwException(e, "person", null);
    }

}
