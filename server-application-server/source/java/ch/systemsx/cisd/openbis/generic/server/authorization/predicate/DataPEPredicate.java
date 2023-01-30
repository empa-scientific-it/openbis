/*
 * Copyright 2012 ETH Zuerich, CISD
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
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.IProjectAuthorization;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.ProjectAuthorizationBuilder;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.project.ProjectProviderFromProjectPE;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.role.RolesProviderFromRolesWithIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.user.UserProviderFromPersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author anttil
 */
public class DataPEPredicate extends PersistentEntityPredicate<DataPE>
{

    @Override
    public Status evaluate(PersonPE person, List<RoleWithIdentifier> allowedRoles, DataPE value) throws UserFailureException
    {
        Status result = super.evaluate(person, allowedRoles, value);

        if (result.isOK())
        {
            return Status.OK;
        }

        if (value != null)
        {
            ProjectPE project = value.getProject();

            if (project != null)
            {
                IProjectAuthorization<ProjectPE> pa = new ProjectAuthorizationBuilder<ProjectPE>()
                        .withData(provider)
                        .withUser(new UserProviderFromPersonPE(person))
                        .withRoles(new RolesProviderFromRolesWithIdentifier(allowedRoles))
                        .withObjects(new ProjectProviderFromProjectPE(project))
                        .build();

                if (pa.getObjectsWithoutAccess().isEmpty())
                {
                    return Status.OK;
                }
            }
        }

        return result;
    }

    @Override
    public SpacePE getSpace(DataPE dataSet)
    {
        return dataSet != null ? dataSet.getSpace() : null;
    }

}
