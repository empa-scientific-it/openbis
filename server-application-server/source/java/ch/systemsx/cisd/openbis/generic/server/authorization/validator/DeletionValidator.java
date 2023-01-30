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

package ch.systemsx.cisd.openbis.generic.server.authorization.validator;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.shared.authorization.AuthorizationConfigFacade;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetAccessPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentAccessPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleAccessPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * A {@link IValidator} implementation for a {@link Deletion}.
 * 
 * @author Izabela Adamczyk
 */
public final class DeletionValidator extends AbstractValidator<Deletion>
{
    //
    // IValidator
    //

    @Override
    public final boolean doValidation(final PersonPE person, final Deletion value)
    {
        // only creator of deletion and instance admin can see it
        return isRegistrator(person, value) || isInstanceAdmin(person)
                || isSpaceOrProjectAdmin(person, value);
    }

    private boolean isSpaceOrProjectAdmin(PersonPE person, Deletion value)
    {
        List<TechId> singletonList = Collections.singletonList(new TechId(value.getId()));

        Set<ExperimentAccessPE> experimentAccessData =
                authorizationDataProvider.getDeletedExperimentCollectionAccessData(singletonList);
        for (ExperimentAccessPE experimentAccessDatum : experimentAccessData)
        {
            if (verifySpace(person, experimentAccessDatum.getSpaceCode()))
            {
                return true;
            }

            if (experimentAccessDatum.getProjectCode() != null)
            {
                if (verifyProject(person, experimentAccessDatum.getSpaceCode(), experimentAccessDatum.getProjectCode()))
                {
                    return true;
                }
            }
        }

        Set<SampleAccessPE> samples =
                authorizationDataProvider.getDeletedSampleCollectionAccessData(singletonList);

        for (SampleAccessPE sample : samples)
        {
            if (sample.getSpaceCode() != null)
            {
                if (verifySpace(person, sample.getSpaceCode()))
                {
                    return true;
                }

                String projectCode = sample.getProjectCode() != null ? sample.getProjectCode() : sample.getExperimentProjectCode();

                if (projectCode != null)
                {
                    if (verifyProject(person, sample.getSpaceCode(), projectCode))
                    {
                        return true;
                    }
                }
            } else
            {
                if (isInstanceAdmin(person))
                {
                    return true;
                }
            }
        }

        Set<DataSetAccessPE> datasets =
                authorizationDataProvider.getDeletedDatasetCollectionAccessData(singletonList);
        for (DataSetAccessPE datasetAccessDatum : datasets)
        {
            SpaceIdentifier spaceIdentifier = datasetAccessDatum.getSpaceIdentifier();

            if (spaceIdentifier != null)
            {
                if (verifySpace(person, spaceIdentifier.getSpaceCode()))
                {
                    return true;
                }
            }

            ProjectIdentifier projectIdentifier = datasetAccessDatum.getProjectIdentifier();

            if (projectIdentifier != null)
            {
                if (verifyProject(person, projectIdentifier.getSpaceCode(), projectIdentifier.getProjectCode()))
                {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean verifySpace(PersonPE person, String spaceCode)
    {
        final Set<RoleAssignmentPE> roleAssignments = person.getAllPersonRoles();
        for (final RoleAssignmentPE roleAssignment : roleAssignments)
        {
            final SpacePE space = roleAssignment.getSpace();
            if (space != null && roleAssignment.getRole().equals(RoleCode.ADMIN))
            {
                if (space.getCode().equals(spaceCode))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean verifyProject(PersonPE person, String spaceCode, String projectCode)
    {
        AuthorizationConfigFacade configFacade = new AuthorizationConfigFacade(authorizationDataProvider.getAuthorizationConfig());

        if (configFacade.isProjectLevelEnabled(person.getUserId()))
        {
            final Set<RoleAssignmentPE> roleAssignments = person.getAllPersonRoles();
            for (final RoleAssignmentPE roleAssignment : roleAssignments)
            {
                final ProjectPE project = roleAssignment.getProject();
                if (project != null && roleAssignment.getRole().equals(RoleCode.ADMIN))
                {
                    if (project.getCode().equals(projectCode) && project.getSpace().getCode().equals(spaceCode))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isRegistrator(final PersonPE person, final Deletion value)
    {
        Person registrator = value.getRegistrator();
        return person.getUserId().equals(registrator.getUserId());
    }

    private static boolean isInstanceAdmin(final PersonPE person)
    {
        final Set<RoleAssignmentPE> roleAssignments = person.getAllPersonRoles();
        for (final RoleAssignmentPE roleAssignment : roleAssignments)
        {
            if (roleAssignment.getRoleWithHierarchy().isInstanceLevel() && roleAssignment.getRole().equals(RoleCode.ADMIN))
            {
                return true;
            }
        }
        return false;
    }
}
