/*
 * Copyright 2017 ETH Zuerich, CISD
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
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
public class SpaceOrProjectPredicate extends AbstractSpacePredicate<String>
{

    private ProjectPEPredicate projectPEPredicate;

    public SpaceOrProjectPredicate(boolean okForNonExistentSpaces)
    {
        super(okForNonExistentSpaces);
        projectPEPredicate = new ProjectPEPredicate();
    }

    @Override
    public void init(IAuthorizationDataProvider dataProvider)
    {
        super.init(dataProvider);
        projectPEPredicate.init(dataProvider);
    }

    @Override
    protected boolean isNullValueAllowed()
    {
        return true;
    }

    @Override
    protected Status doEvaluation(PersonPE person, List<RoleWithIdentifier> allowedRoles, String valueOrNull)
    {
        if (hasInstanceWritePermissions(person, allowedRoles).isOK())
        {
            return Status.OK;
        }

        Status status = evaluate(allowedRoles, person, valueOrNull);

        if (status.isError())
        {
            // Even though the person does not have access to the space itself he/she may have access to some of the projects within that space.
            // If so, we still want to return OK for the objects from such projects to be returned as well. Other objects, i.e. objects from
            // projects that the person does not have access to will be filtered out by a return value filter.

            if (valueOrNull != null)
            {
                SpacePE space = authorizationDataProvider.tryGetSpace(valueOrNull);

                if (space != null)
                {
                    for (ProjectPE project : space.getProjects())
                    {
                        status = projectPEPredicate.evaluate(person, allowedRoles, project);
                        if (status.isOK())
                        {
                            break;
                        }
                    }
                }
            }
        }

        return status;
    }

    @Override
    public String getCandidateDescription()
    {
        return "space";
    }

}
