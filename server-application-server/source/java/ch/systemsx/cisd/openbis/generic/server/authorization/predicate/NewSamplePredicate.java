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
import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * An <code>IPredicate</code> implementation for {@link NewSample}.
 * 
 * @author Christian Ribeaud
 */
public final class NewSamplePredicate extends AbstractPredicate<NewSample>
{

    private ProjectIdentifierPredicate projectIdentifierPredicate;

    private SampleIdentifierPredicate sampleIdentifierPredicate;

    private NewSampleIdentifierPredicate newSampleIdentifierPredicate;

    public NewSamplePredicate()
    {
        projectIdentifierPredicate = new ProjectIdentifierPredicate(true);
        sampleIdentifierPredicate = new SampleIdentifierPredicate(false, true);
        newSampleIdentifierPredicate = new NewSampleIdentifierPredicate();
    }

    @Override
    public void init(IAuthorizationDataProvider provider)
    {
        projectIdentifierPredicate.init(provider);
        sampleIdentifierPredicate.init(provider);
        newSampleIdentifierPredicate.init(provider);
    }

    @Override
    protected Status doEvaluation(PersonPE person, List<RoleWithIdentifier> allowedRoles, NewSample value)
    {
        if (hasInstanceWritePermissions(person, allowedRoles).isOK())
        {
            return Status.OK;
        }

        Status status = Status.OK;

        if (value.getProjectIdentifier() == null && value.getExperimentIdentifier() == null)
        {
            if (value.getIdentifier() != null)
            {
                status = newSampleIdentifierPredicate.doEvaluation(person, allowedRoles, value);

                if (false == status.equals(Status.OK))
                {
                    return status;
                }
            }
        }

        if (value.getProjectIdentifier() != null)
        {
            ProjectIdentifier projectIdentifier = ProjectIdentifierFactory.parse(value.getProjectIdentifier(), value.getDefaultSpaceIdentifier());
            status = projectIdentifierPredicate.doEvaluation(person, allowedRoles, projectIdentifier);

            if (false == status.equals(Status.OK))
            {
                return status;
            }
        }

        if (value.getExperimentIdentifier() != null)
        {
            ExperimentIdentifier experimentIdentifier =
                    ExperimentIdentifierFactory.parse(value.getExperimentIdentifier(), value.getDefaultSpaceIdentifier());
            status = projectIdentifierPredicate.doEvaluation(person, allowedRoles, experimentIdentifier);

            if (false == status.equals(Status.OK))
            {
                return status;
            }
        }

        if (value.getContainerIdentifierForNewSample() != null)
        {
            SampleIdentifier containerIdentifier =
                    SampleIdentifierFactory.parse(value.getContainerIdentifierForNewSample(), value.getDefaultSpaceIdentifier());
            status = sampleIdentifierPredicate.doEvaluation(person, allowedRoles, containerIdentifier);

            if (false == status.equals(Status.OK))
            {
                return status;
            }
        }

        if (value.getParentsOrNull() != null && value.getParentsOrNull().length > 0)
        {
            for (String parent : value.getParentsOrNull())
            {
                SampleIdentifier parentIdentifier = SampleIdentifierFactory.parse(parent, value.getDefaultSpaceIdentifier());
                status = sampleIdentifierPredicate.doEvaluation(person, allowedRoles, parentIdentifier);

                if (false == status.equals(Status.OK))
                {
                    return status;
                }
            }
        }

        return status;
    }

    @Override
    public final String getCandidateDescription()
    {
        return "new sample";
    }

}
