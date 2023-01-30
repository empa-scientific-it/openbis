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
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * An <code>IPredicate</code> implementation based on {@link ExperimentUpdatesDTO}. Checks that: 1) the user has rights to update the experiment 2) if
 * experiment is moved to a different projects the user has access to this project.
 * 
 * @author Tomasz Pylak
 */
public class ExperimentUpdatesPredicate extends AbstractExperimentPredicate<ExperimentUpdatesDTO>
{
    @Override
    public final String getCandidateDescription()
    {
        return "experiment updates";
    }

    @Override
    protected Status doEvaluation(final PersonPE person,
            final List<RoleWithIdentifier> allowedRoles,
            final ExperimentUpdatesDTO updates)
    {
        assert projectPredicate.initialized : "Predicate has not been initialized";
        assert experimentTechIdPredicate.initialized : "Predicate has not been initialized";

        // Skip all further checks if the person has instance-wide write permissions.
        if (hasInstanceWritePermissions(person, allowedRoles).isOK())
        {
            return Status.OK;
        }

        Status status;
        status =
                experimentTechIdPredicate.doEvaluation(person, allowedRoles, updates
                        .getExperimentId());
        if (status.equals(Status.OK) == false)
        {
            return status;
        }

        if (updates.getProjectIdentifier() != null)
        {
            status = projectPredicate.doEvaluation(person, allowedRoles, updates.getProjectIdentifier());
            return status;
        } else
        {
            return Status.OK;
        }
    }
}
