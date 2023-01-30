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

package ch.systemsx.cisd.openbis.generic.server.authorization.predicate;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.PermId;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * An <code>IPredicate</code> implementation based on {@link ProjectUpdatesDTO}. Checks that: 1) the user has rights to update the project 2) if
 * project is moved to a different space the user has access to this space.
 * 
 * @author Tomasz Pylak
 */
public class ProjectUpdatesPredicate extends AbstractProjectPredicate<ProjectUpdatesDTO>
{
    @Override
    public final String getCandidateDescription()
    {
        return "project updates";
    }

    @Override
    protected Status doEvaluation(final PersonPE person,
            final List<RoleWithIdentifier> allowedRoles,
            final ProjectUpdatesDTO updates)
    {
        assert spacePredicate.initialized : "Predicate has not been initialized";
        assert projectTechIdPredicate.initialized : "Predicate has not been initialized";
        assert projectPermIdPredicate.initialized : "Predicate has not been initialized";
        assert projectAugmentedCodePredicate.initialized : "Predicate has not been initialized";
        Status status;
        if (updates.getTechId() != null)
        {
            status = projectTechIdPredicate.doEvaluation(person,
                    allowedRoles, updates.getTechId());
        } else if (updates.getPermId() != null)
        {
            status = projectPermIdPredicate.doEvaluation(person,
                    allowedRoles, new PermId(updates.getPermId()));
        } else
        {
            status = projectAugmentedCodePredicate.doEvaluation(person,
                    allowedRoles, updates.getIdentifier());
        }
        if (status.equals(Status.OK) == false)
        {
            return status;
        }

        ProjectPE project = tryGetProject(updates);

        if (project != null)
        {
            String oldSpaceCode = project.getSpace().getCode();
            String newSpaceCode = updates.getSpaceCode();

            if (newSpaceCode != null && false == newSpaceCode.equals(oldSpaceCode))
            {
                status = spacePredicate.doEvaluation(person, allowedRoles, new SpaceIdentifier(newSpaceCode));
            }
        }

        return status;
    }

    private ProjectPE tryGetProject(ProjectUpdatesDTO updates)
    {
        if (updates.getTechId() != null)
        {
            return provider.tryGetProjectByTechId(updates.getTechId());
        } else if (updates.getPermId() != null)
        {
            return provider.tryGetProjectByPermId(new PermId(updates.getPermId()));
        } else if (updates.getIdentifier() != null)
        {
            return provider.tryGetProjectByIdentifier(ProjectIdentifierFactory.parse(updates.getIdentifier()));
        } else
        {
            return null;
        }
    }

}
