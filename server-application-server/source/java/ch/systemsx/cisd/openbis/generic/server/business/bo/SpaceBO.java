/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.Collections;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.DataSetTypeWithoutExperimentChecker;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.util.SpaceIdentifierHelper;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISpaceUpdates;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.IdentifierHelper;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;

/**
 * The only productive implementation of {@link ISpaceBO}. We are using an interface here to keep the system testable.
 * 
 * @author Christian Ribeaud
 */
public final class SpaceBO extends AbstractBusinessObject implements ISpaceBO
{

    private SpacePE space;

    public SpaceBO(final IDAOFactory daoFactory, final Session session,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            DataSetTypeWithoutExperimentChecker dataSetTypeChecker,
            IRelationshipService relationshipService)
    {
        super(daoFactory, session, managedPropertyEvaluatorFactory, dataSetTypeChecker, relationshipService);
    }

    //
    // IGroupBO
    //

    @Override
    public final void save() throws UserFailureException
    {
        assert space != null : "Space not defined";
        try
        {
            getSpaceDAO().createSpace(space);
        } catch (final DataAccessException e)
        {
            throwException(e, "Space '" + IdentifierHelper.createGroupIdentifier(space) + "'");
        }
    }

    @Override
    public void update(ISpaceUpdates updates)
    {
        loadDataByTechId(TechId.create(updates));
        if (space.getModificationDate().equals(updates.getModificationDate()) == false)
        {
            throwModifiedEntityException("Space");
        }

        space.setDescription(updates.getDescription());

        validateAndSave();
    }

    private void validateAndSave()
    {
        getSpaceDAO().validateAndSaveUpdatedEntity(space);
    }

    @Override
    public final void define(String groupCode, final String descriptionOrNull)
            throws UserFailureException
    {
        assert groupCode != null : "Unspecified space code.";
        space = new SpacePE();
        final SpaceIdentifier groupIdentifier =
                new SpaceIdentifier(groupCode);
        space.setCode(groupIdentifier.getSpaceCode());
        space.setDescription(descriptionOrNull);
        space.setRegistrator(findPerson());
    }

    @Override
    public SpacePE getSpace() throws UserFailureException
    {
        return space;
    }

    @Override
    public void load(final SpaceIdentifier spaceIdentifier) throws UserFailureException
    {
        space = SpaceIdentifierHelper.tryGetSpace(spaceIdentifier, session.tryGetPerson(), this);
        if (space == null)
        {
            throw new UserFailureException(String.format("Space '%s' does not exist.",
                    spaceIdentifier));
        }
    }

    @Override
    public void loadDataByTechId(TechId spaceId)
    {
        try
        {
            space = getSpaceDAO().getByTechId(spaceId);
        } catch (DataRetrievalFailureException exception)
        {
            throw new UserFailureException(exception.getMessage());
        }
    }

    @Override
    public void deleteByTechId(TechId spaceId, String reason) throws UserFailureException
    {
        loadDataByTechId(spaceId);
        try
        {
            getSpaceDAO().delete(space);
            getEventDAO().persist(createDeletionEvent(space, session.tryGetPerson(), reason));
        } catch (final DataAccessException ex)
        {
            throwException(ex, String.format("Space '%s'", space.getCode()));
        }
    }

    public static EventPE createDeletionEvent(SpacePE space, PersonPE registrator, String reason)
    {
        EventPE event = new EventPE();
        event.setEventType(EventType.DELETION);
        event.setEntityType(EntityType.SPACE);
        event.setIdentifiers(Collections.singletonList(space.getCode()));
        event.setDescription(getDeletionDescription(space));
        event.setReason(reason);
        event.setRegistrator(registrator);

        return event;
    }

    private static String getDeletionDescription(SpacePE space)
    {
        return String.format("%s", space.getCode());
    }
}
