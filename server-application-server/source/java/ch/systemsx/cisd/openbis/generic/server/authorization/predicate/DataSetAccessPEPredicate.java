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
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetAccessPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * @author pkupczyk
 */
public class DataSetAccessPEPredicate extends AbstractPredicate<DataSetAccessPE>
{

    private final ProjectIdentifierPredicate projectPredicate;

    private final SpaceIdentifierPredicate spacePredicate;

    public DataSetAccessPEPredicate()
    {
        projectPredicate = new ProjectIdentifierPredicate(false);
        spacePredicate = new SpaceIdentifierPredicate(false);
    }

    @Override
    public final void init(IAuthorizationDataProvider provider)
    {
        projectPredicate.init(provider);
        spacePredicate.init(provider);
    }

    @Override
    public final String getCandidateDescription()
    {
        return "data set access";
    }

    @Override
    protected final Status doEvaluation(final PersonPE person,
            final List<RoleWithIdentifier> allowedRoles, final DataSetAccessPE value)
    {
        if (hasInstanceWritePermissions(person, allowedRoles).isOK())
        {
            return Status.OK;
        }

        SpaceIdentifier spaceIdentifier = value.getSpaceIdentifier();

        if (spaceIdentifier != null)
        {
            Status spaceResult = spacePredicate.doEvaluation(person, allowedRoles, spaceIdentifier);

            if (spaceResult.isOK())
            {
                return Status.OK;
            }
        }

        ProjectIdentifier projectIdentifier = value.getProjectIdentifier();

        if (projectIdentifier != null)
        {
            Status projectResult = projectPredicate.doEvaluation(person, allowedRoles, projectIdentifier);

            if (projectResult.isOK())
            {
                return Status.OK;
            }
        }

        return Status.createError(String.format("User '%s' does not have enough privileges.", person.getUserId()));
    }

}
