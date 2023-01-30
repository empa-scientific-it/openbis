/*
 * Copyright 2009 ETH Zuerich, CISD
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.DataSetTypeWithoutExperimentChecker;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AuthorizationGroupUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.shared.dto.AuthorizationGroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;

/**
 * {@link IAuthorizationGroupBO} implementation.
 * 
 * @author Izabela Adamczyk
 */
public class AuthorizationGroupBO extends AbstractBusinessObject implements IAuthorizationGroupBO
{

    private final IAuthorizationGroupFactory groupFactory;

    private AuthorizationGroupPE authorizationGroup;

    private boolean dataChanged;

    // For tests only
    @Private
    AuthorizationGroupBO(IDAOFactory daoFactory, Session session,
            IAuthorizationGroupFactory factory,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            DataSetTypeWithoutExperimentChecker dataSetTypeChecker,
            IRelationshipService relationshipService)
    {
        super(daoFactory, session, managedPropertyEvaluatorFactory, dataSetTypeChecker, relationshipService);
        this.groupFactory = factory;
    }

    public AuthorizationGroupBO(IDAOFactory daoFactory, Session session,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            DataSetTypeWithoutExperimentChecker dataSetTypeChecker,
            IRelationshipService relationshipService)
    {
        this(daoFactory, session, new AuthorizationGroupFactory(), managedPropertyEvaluatorFactory,
                dataSetTypeChecker, relationshipService);
    }

    interface IAuthorizationGroupFactory
    {
        /**
         * Creates a new {@link AuthorizationGroupPE}.
         */
        public AuthorizationGroupPE create(NewAuthorizationGroup newAuthorizationGroup,
                PersonPE registrator);
    }

    @Private
    static class AuthorizationGroupFactory implements IAuthorizationGroupFactory
    {
        @Override
        public AuthorizationGroupPE create(NewAuthorizationGroup newAuthorizationGroup,
                PersonPE registrator)
        {
            AuthorizationGroupPE authorizationGroup = new AuthorizationGroupPE();
            authorizationGroup.setCode(newAuthorizationGroup.getCode());
            authorizationGroup.setDescription(newAuthorizationGroup.getDescription());
            authorizationGroup.setRegistrator(registrator);
            return authorizationGroup;
        }
    }

    @Override
    public void define(NewAuthorizationGroup newAuthorizationGroup) throws UserFailureException
    {
        assert newAuthorizationGroup != null : "Undefined new authorization group";
        assert authorizationGroup == null : "Authorization group already defined";
        authorizationGroup =
                groupFactory.create(newAuthorizationGroup, findPerson());
        dataChanged = true;
    }

    @Override
    public void save() throws UserFailureException
    {
        assert authorizationGroup != null : "Authorization group not loaded.";
        if (dataChanged)
        {
            try
            {
                getAuthorizationGroupDAO().create(authorizationGroup);
            } catch (final DataAccessException ex)
            {
                throwException(ex,
                        String.format("Authorization group '%s'", authorizationGroup.getCode()));
            }
            dataChanged = false;
        }
    }

    @Override
    public void deleteByTechId(TechId authGroupId, String reason)
    {
        loadByTechId(authGroupId);
        String code = authorizationGroup.getCode();
        try
        {
            PersonPE[] persons = authorizationGroup.getPersons().toArray(new PersonPE[0]);
            for (PersonPE personPE : persons)
            {
                authorizationGroup.removePerson(personPE);
            }
            getAuthorizationGroupDAO().delete(authorizationGroup);
            getEventDAO().persist(createDeletionEvent(code, session.tryGetPerson(), reason));
        } catch (final DataAccessException ex)
        {
            throwException(ex, String.format("Authorization group '%s'", code));
        }

    }

    @Override
    public void loadByTechId(TechId techId)
    {
        try
        {
            authorizationGroup = getAuthorizationGroupDAO().getByTechId(techId);
        } catch (DataRetrievalFailureException exception)
        {
            throw new UserFailureException(String.format(
                    "Authorization group with ID '%s' does not exist.", techId));
        }
        dataChanged = false;
    }

    public static EventPE createDeletionEvent(String groupCode, PersonPE registrator, String reason)
    {
        EventPE event = new EventPE();
        event.setEventType(EventType.DELETION);
        event.setEntityType(EntityType.AUTHORIZATION_GROUP);
        event.setIdentifiers(Collections.singletonList(groupCode));
        event.setDescription(groupCode);
        event.setReason(reason);
        event.setRegistrator(registrator);
        return event;
    }

    @Override
    public AuthorizationGroupPE getAuthorizationGroup()
    {
        return authorizationGroup;
    }

    @Override
    public void update(AuthorizationGroupUpdates updates)
    {
        loadByTechId(updates.getId());
        if (authorizationGroup.getModificationDate().equals(updates.getVersion()) == false)
        {
            throwModifiedEntityException("User group");
        }
        authorizationGroup.setDescription(updates.getDescription());
        dataChanged = true;
    }

    @Override
    public List<String> addPersons(List<String> personsCodes)
    {
        assert authorizationGroup != null : "Not initialized";
        List<String> inexistent = new ArrayList<String>(personsCodes);
        List<PersonPE> users = getPersonDAO().listByCodes(personsCodes);
        for (PersonPE person : users)
        {
            inexistent.remove(person.getUserId());
            if (person.isActive())
            {
                authorizationGroup.addPerson(person);
            }
        }
        return inexistent;
    }

    @Override
    public void removePersons(List<String> personsCodes)
    {
        assert authorizationGroup != null : "Not initialized";
        List<PersonPE> users = getPersonDAO().listByCodes(personsCodes);
        for (PersonPE person : users)
        {
            authorizationGroup.removePerson(person);
        }
    }

}
