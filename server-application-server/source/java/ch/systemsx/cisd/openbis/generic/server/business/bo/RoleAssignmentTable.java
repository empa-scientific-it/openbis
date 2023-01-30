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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;

import ch.systemsx.cisd.common.collection.TableMap;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.DataSetTypeWithoutExperimentChecker;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.util.KeyExtractorFactory;
import ch.systemsx.cisd.openbis.generic.server.util.SpaceIdentifierHelper;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Grantee;
import ch.systemsx.cisd.openbis.generic.shared.dto.AuthorizationGroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewRoleAssignment;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;

/**
 * The only productive implementation of {@link IRoleAssignmentTable}. We are using an interface here to keep the system testable.
 * 
 * @author Christian Ribeaud
 */
public final class RoleAssignmentTable extends AbstractBusinessObject implements
        IRoleAssignmentTable
{
    private List<RoleAssignmentPE> roleAssignments;

    private TableMap<String, PersonPE> personsByUserId;

    private TableMap<String, AuthorizationGroupPE> authorizationGroupsByCode;

    public RoleAssignmentTable(final IDAOFactory daoFactory, final Session session,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            DataSetTypeWithoutExperimentChecker dataSetTypeChecker,
            IRelationshipService relationshipService)
    {
        super(daoFactory, session, managedPropertyEvaluatorFactory, dataSetTypeChecker, relationshipService);
    }

    private final PersonPE getPerson(final String userId)
    {
        if (personsByUserId == null)
        {
            personsByUserId =
                    new TableMap<String, PersonPE>(getPersonDAO().listPersons(),
                            KeyExtractorFactory.getPersonByUserIdKeyExtractor());
        }
        final PersonPE person = personsByUserId.tryGet(userId);
        if (person == null)
        {
            throw UserFailureException.fromTemplate("No person could be found for user id '%s'",
                    userId);
        }
        return person;
    }

    private final AuthorizationGroupPE getAuthorizationGroup(final String authGroupId)
    {
        if (authorizationGroupsByCode == null)
        {
            authorizationGroupsByCode =
                    new TableMap<String, AuthorizationGroupPE>(getAuthorizationGroupDAO().list(),
                            KeyExtractorFactory.getAuthorizationGroupByCodeKeyExtractor());
        }
        final AuthorizationGroupPE authGroup = authorizationGroupsByCode.tryGet(authGroupId);
        if (authGroup == null)
        {
            throw UserFailureException.fromTemplate(
                    "No authorization group could be found for code '%s'", authGroupId);
        }
        return authGroup;
    }

    //
    // IRoleAssignmentTable
    //

    @Override
    public final void add(final NewRoleAssignment newRoleAssignment)
    {
        if (roleAssignments == null)
        {
            roleAssignments = new ArrayList<RoleAssignmentPE>();
        }
        final RoleAssignmentPE roleAssignment = new RoleAssignmentPE();

        if (newRoleAssignment.getSpaceIdentifier() != null && newRoleAssignment.getProjectIdentifier() != null)
        {
            throw UserFailureException.fromTemplate(
                    "Role assignment can have either space or project specified but not both. Space was '%s'. Project was '%s'.",
                    newRoleAssignment.getSpaceIdentifier(), newRoleAssignment.getProjectIdentifier());
        }

        final SpaceIdentifier groupIdentifier = newRoleAssignment.getSpaceIdentifier();
        if (groupIdentifier != null)
        {
            final SpacePE group =
                    SpaceIdentifierHelper
                            .tryGetSpace(groupIdentifier, session.tryGetPerson(), this);
            if (group == null)
            {
                throw UserFailureException.fromTemplate("Specified space '%s' could not be found",
                        groupIdentifier);
            }
            roleAssignment.setSpace(group);
        }

        final ProjectIdentifier projectIdentifier = newRoleAssignment.getProjectIdentifier();
        if (projectIdentifier != null)
        {
            final List<ProjectPE> projects = getProjectDAO().tryFindProjects(Arrays.asList(projectIdentifier));

            if (projects == null || projects.isEmpty())
            {
                throw UserFailureException.fromTemplate("Specified project '%s' could not be found",
                        projectIdentifier);
            }
            roleAssignment.setProject(projects.get(0));
        }

        roleAssignment.setRegistrator(findPerson());
        roleAssignment.setRole(newRoleAssignment.getRole());
        if (Grantee.GranteeType.PERSON.equals(newRoleAssignment.getGrantee().getType()))
        {
            getPerson(newRoleAssignment.getGrantee().getCode()).addRoleAssignment(roleAssignment);
        } else
        {
            getAuthorizationGroup(newRoleAssignment.getGrantee().getCode().toUpperCase())
                    .addRoleAssignment(roleAssignment);
        }
        roleAssignments.add(roleAssignment);
    }

    //
    // AbstractBusinessObject
    //

    @Override
    public final void save() throws UserFailureException
    {
        assert roleAssignments != null : "Role assignments unspecified";
        for (final RoleAssignmentPE roleAssignment : roleAssignments)
        {
            try
            {
                getRoleAssignmentDAO().createRoleAssignment(roleAssignment);
            } catch (final DataIntegrityViolationException ex)
            {
                throwException(ex, "Role assignment");
            }
        }
    }

}
