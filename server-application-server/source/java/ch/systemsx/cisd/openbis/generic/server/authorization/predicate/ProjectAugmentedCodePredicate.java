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
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.IProjectAuthorization;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.ProjectAuthorizationBuilder;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.project.ProjectProviderFromProjectIdentifierString;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.role.RolesProviderFromRolesWithIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.user.UserProviderFromPersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * A {@link IPredicate} for a project's augmented code (i.e. DB:/SPACE/PROJECT).
 * 
 * @author Bernd Rinn
 */
public class ProjectAugmentedCodePredicate extends DelegatedPredicate<SpaceIdentifier, String>
{
    public ProjectAugmentedCodePredicate()
    {
        super(new SpaceIdentifierPredicate(false));
    }

    @Override
    public SpaceIdentifier tryConvert(String value)
    {
        return new ProjectIdentifierFactory(value).createIdentifier();
    }

    @Override
    public Status doEvaluation(PersonPE person, List<RoleWithIdentifier> allowedRoles, String value)
    {
        IProjectAuthorization<String> pa = new ProjectAuthorizationBuilder<String>()
                .withData(authorizationDataProvider)
                .withUser(new UserProviderFromPersonPE(person))
                .withRoles(new RolesProviderFromRolesWithIdentifier(allowedRoles))
                .withObjects(new ProjectProviderFromProjectIdentifierString(value))
                .build();

        if (pa.getObjectsWithoutAccess().isEmpty())
        {
            return Status.OK;
        }

        return super.doEvaluation(person, allowedRoles, value);
    }

    @Override
    public String getCandidateDescription()
    {
        return "project";
    }

}
