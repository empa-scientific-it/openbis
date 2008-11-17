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

package ch.systemsx.cisd.openbis.plugin.generic.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.AbstractServer;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExternalDataTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IGenericBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IGroupBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IRoleAssignmentTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleTable;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.util.GroupIdentifierHelper;
import ch.systemsx.cisd.openbis.generic.shared.IGenericServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IMatchingEntity;
import ch.systemsx.cisd.openbis.generic.shared.dto.ListSampleCriteriaDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewRoleAssignment;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleGenerationDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SearchableEntity;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.plugin.ISampleServerPlugin;
import ch.systemsx.cisd.openbis.plugin.SampleServerPluginRegistry;
import ch.systemsx.cisd.openbis.plugin.generic.shared.ResourceNames;

/**
 * Implementation of client-server interface.
 * 
 * @author Franz-Josef Elmer
 */
@Component(ResourceNames.GENERIC_SERVER)
public final class GenericServer extends AbstractServer<IGenericServer> implements IGenericServer
{
    @Resource(name = ComponentNames.AUTHENTICATION_SERVICE)
    private IAuthenticationService authenticationService;

    public GenericServer()
    {
    }

    GenericServer(final IAuthenticationService authenticationService,
            final ISessionManager<Session> sessionManager, final IDAOFactory daoFactory,
            final IGenericBusinessObjectFactory boFactory)
    {
        super(sessionManager, daoFactory, boFactory);
        this.authenticationService = authenticationService;
    }

    //
    // AbstractServer
    //

    @Override
    protected final Class<IGenericServer> getProxyInterface()
    {
        return IGenericServer.class;
    }

    //
    // IInvocationLoggerFactory
    //

    /**
     * Creates a logger used to log invocations of objects of this class.
     */
    public final IGenericServer createLogger(final boolean invocationSuccessful)
    {
        return new GenericServerLogger(getSessionManager(), invocationSuccessful);
    }

    //
    // IGenericServer
    //

    public final List<GroupPE> listGroups(final String sessionToken,
            final DatabaseInstanceIdentifier identifier)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        final DatabaseInstancePE databaseInstance =
                GroupIdentifierHelper.getDatabaseInstance(identifier, getDAOFactory());
        final List<GroupPE> groups = getDAOFactory().getGroupDAO().listGroups(databaseInstance);
        final Long homeGroupID = session.tryGetHomeGroupId();
        for (final GroupPE group : groups)
        {
            group.setHome(homeGroupID != null && homeGroupID.equals(group.getId()));
        }
        Collections.sort(groups);
        return groups;
    }

    public final void registerGroup(final String sessionToken, final String groupCode,
            final String descriptionOrNull, final String groupLeaderOrNull)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        final IGroupBO groupBO = getBusinessObjectFactory().createGroupBO(session);
        groupBO.define(groupCode, descriptionOrNull, groupLeaderOrNull);
        groupBO.save();
    }

    public final void registerPerson(final String sessionToken, final String userID)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        final PersonPE person = getDAOFactory().getPersonDAO().tryFindPersonByUserId(userID);
        if (person != null)
        {
            throw UserFailureException.fromTemplate("Person '%s' already exists.", userID);
        }
        final String applicationToken = authenticationService.authenticateApplication();
        if (applicationToken == null)
        {
            throw new EnvironmentFailureException("Authentication service cannot be accessed.");
        }
        try
        {
            final Principal principal =
                    authenticationService.getPrincipal(applicationToken, userID);
            createPerson(principal, session.tryGetPerson());
        } catch (final IllegalArgumentException e)
        {
            throw new UserFailureException("Person '" + userID
                    + "' unknown by the authentication service.");
        }
    }

    public final List<RoleAssignmentPE> listRoles(final String sessionToken)
    {
        getSessionManager().getSession(sessionToken);
        return getDAOFactory().getRoleAssignmentDAO().listRoleAssignments();
    }

    public final void registerGroupRole(final String sessionToken, final RoleCode roleCode,
            final GroupIdentifier groupIdentifier, final String person)
    {
        final Session session = getSessionManager().getSession(sessionToken);

        final NewRoleAssignment newRoleAssignment = new NewRoleAssignment();
        newRoleAssignment.setUserId(person);
        newRoleAssignment.setGroupIdentifier(groupIdentifier);
        newRoleAssignment.setRole(roleCode);

        final IRoleAssignmentTable table =
                getBusinessObjectFactory().createRoleAssignmentTable(session);
        table.add(newRoleAssignment);
        table.save();

    }

    public final void registerInstanceRole(final String sessionToken, final RoleCode roleCode,
            final String person)
    {
        final Session session = getSessionManager().getSession(sessionToken);

        final NewRoleAssignment newRoleAssignment = new NewRoleAssignment();
        newRoleAssignment.setUserId(person);
        newRoleAssignment.setDatabaseInstanceIdentifier(new DatabaseInstanceIdentifier(
                DatabaseInstanceIdentifier.HOME));
        newRoleAssignment.setRole(roleCode);

        final IRoleAssignmentTable table =
                getBusinessObjectFactory().createRoleAssignmentTable(session);
        table.add(newRoleAssignment);
        table.save();

    }

    public final void deleteGroupRole(final String sessionToken, final RoleCode roleCode,
            final GroupIdentifier groupIdentifier, final String person)
    {

        final Session session = getSessionManager().getSession(sessionToken);

        final RoleAssignmentPE roleAssignment =
                getDAOFactory().getRoleAssignmentDAO().tryFindGroupRoleAssignment(roleCode,
                        groupIdentifier.getGroupCode(), person);
        if (roleAssignment == null)
        {
            throw new UserFailureException("Given role does not exist.");
        }
        final PersonPE personPE = session.tryGetPerson();
        if (roleAssignment.getPerson().compareTo(personPE) == 0
                && roleAssignment.getRole().compareTo(RoleCode.ADMIN) == 0)
        {
            boolean isInstanceAdmin = false;
            if (personPE != null && personPE.getRoleAssignments() != null)
            {
                for (final RoleAssignmentPE ra : personPE.getRoleAssignments())
                {
                    if (ra.getDatabaseInstance() != null && ra.getRole().equals(RoleCode.ADMIN))
                    {
                        isInstanceAdmin = true;
                    }
                }
            }
            if (isInstanceAdmin == false)
            {
                throw new UserFailureException(
                        "For safety reason you cannot give away your own group admin power. Ask instance admin to do that for you.");
            }
        }
        getDAOFactory().getRoleAssignmentDAO().deleteRoleAssignment(roleAssignment);
    }

    public final void deleteInstanceRole(final String sessionToken, final RoleCode roleCode,
            final String person)
    {

        final Session session = getSessionManager().getSession(sessionToken);
        final RoleAssignmentPE roleAssignment =
                getDAOFactory().getRoleAssignmentDAO().tryFindInstanceRoleAssignment(roleCode,
                        person);
        if (roleAssignment == null)
        {
            throw new UserFailureException("Given role does not exist.");
        }
        if (roleAssignment.getPerson().compareTo(session.tryGetPerson()) == 0
                && roleAssignment.getRole().compareTo(RoleCode.ADMIN) == 0
                && roleAssignment.getDatabaseInstance() != null)
        {
            throw new UserFailureException(
                    "For safety reason you cannot give away your own omnipotence. Ask another instance admin to do that for you.");
        }
        getDAOFactory().getRoleAssignmentDAO().deleteRoleAssignment(roleAssignment);
    }

    public final List<PersonPE> listPersons(final String sessionToken)
    {
        getSessionManager().getSession(sessionToken);
        final List<PersonPE> persons = getDAOFactory().getPersonDAO().listPersons();
        Collections.sort(persons);
        return persons;
    }

    public final List<SampleTypePE> listSampleTypes(final String sessionToken)
    {
        getSessionManager().getSession(sessionToken);
        return getDAOFactory().getSampleTypeDAO().listSampleTypes(true);
    }

    public final List<SamplePE> listSamples(final String sessionToken,
            final ListSampleCriteriaDTO criteria)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        final ISampleTable sampleTable = getBusinessObjectFactory().createSampleTable(session);
        sampleTable.loadSamplesByCriteria(criteria);
        sampleTable.enrichWithValidProcedure();
        sampleTable.enrichWithProperties();
        return sampleTable.getSamples();
    }

    public final SampleGenerationDTO getSampleInfo(final String sessionToken,
            final SampleIdentifier identifier)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        final ISampleBO sampleBO = getBusinessObjectFactory().createSampleBO(session);
        sampleBO.loadBySampleIdentifier(identifier);
        final SamplePE sample = sampleBO.getSample();
        final ISampleServerPlugin plugin =
                SampleServerPluginRegistry.getPlugin(this, sample.getSampleType());
        return plugin.getSlaveServer().getSampleInfo(getDAOFactory(), session, sample);
    }

    public final List<ExternalDataPE> listExternalData(final String sessionToken,
            final SampleIdentifier identifier)
    {
        final Session session = getSessionManager().getSession(sessionToken);
        final IExternalDataTable externalDataTable =
                getBusinessObjectFactory().createExternalDataTable(session);
        externalDataTable.loadBySampleIdentifier(identifier);
        return externalDataTable.getExternalData();
    }

    public final List<IMatchingEntity> listMatchingEntities(final String sessionToken,
            final SearchableEntity[] searchableEntities, final String queryText)
    {
        getSessionManager().getSession(sessionToken);
        final List<IMatchingEntity> list = new ArrayList<IMatchingEntity>();
        try
        {
            for (final SearchableEntity searchableEntity : searchableEntities)
            {
                final List<IMatchingEntity> entities =
                        getDAOFactory().getHibernateSearchDAO().searchEntitiesByTerm(
                                searchableEntity.getMatchingEntityClass(),
                                searchableEntity.getFields(), queryText);
                list.addAll(entities);
            }
        } catch (final DataAccessException ex)
        {
            throw new UserFailureException(ex.getMostSpecificCause().getMessage(), ex);
        }
        return list;
    }
}
