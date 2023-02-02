/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.server.authorization;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.AbstractSpacePredicate;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.IProjectAuthorization;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.ProjectAuthorizationBuilder;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.project.ProjectProviderFromExperimentIdentifierString;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.project.ProjectProviderFromExperimentPermId;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.role.RolesProviderFromRolesWithIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.user.UserProviderFromPersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.util.SpaceCodeHelper;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ExperimentIdentifier;

/**
 * A predicate for {@link ExperimentIdentifier}.
 * <p>
 * <i>This is an internal class. Do not use it as a user of the API.</i>
 * 
 * @author Bernd Rinn
 */
public class ExperimentIdentifierPredicate extends AbstractSpacePredicate<ExperimentIdentifier>
{

    @Override
    public String getCandidateDescription()
    {
        return "experiment";
    }

    @Override
    protected Status doEvaluation(PersonPE person, List<RoleWithIdentifier> allowedRoles,
            ExperimentIdentifier value)
    {
        if (value.getPermId() != null)
        {
            IProjectAuthorization<String> pa = new ProjectAuthorizationBuilder<String>()
                    .withData(authorizationDataProvider)
                    .withUser(new UserProviderFromPersonPE(person))
                    .withRoles(new RolesProviderFromRolesWithIdentifier(allowedRoles))
                    .withObjects(new ProjectProviderFromExperimentPermId(value.getPermId()))
                    .build();

            if (pa.getObjectsWithoutAccess().isEmpty())
            {
                return Status.OK;
            }

            final ExperimentPE experimentOrNull =
                    authorizationDataProvider.tryGetExperimentByPermId(value.getPermId());
            if (experimentOrNull == null)
            {
                return Status.createError(String.format(
                        "User '%s' does not have enough privileges.", person.getUserId()));
            }
            final SpacePE space = experimentOrNull.getProject().getSpace();
            return evaluate(allowedRoles, person, space.getCode());
        }

        IProjectAuthorization<String> pa = new ProjectAuthorizationBuilder<String>()
                .withData(authorizationDataProvider)
                .withUser(new UserProviderFromPersonPE(person))
                .withRoles(new RolesProviderFromRolesWithIdentifier(allowedRoles))
                .withObjects(new ProjectProviderFromExperimentIdentifierString(value.getAugmentedCode()))
                .build();

        if (pa.getObjectsWithoutAccess().isEmpty())
        {
            return Status.OK;
        }

        final String spaceCode = SpaceCodeHelper.getSpaceCode(person, value.getSpaceCode());
        return evaluate(allowedRoles, person, spaceCode);
    }

}
