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
import ch.systemsx.cisd.openbis.generic.server.authorization.project.IProjectAuthorization;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.ProjectAuthorizationBuilder;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.project.ProjectProviderFromNewExperiment;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.role.RolesProviderFromRolesWithIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.user.UserProviderFromPersonPE;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * An <code>IPredicate</code> implementation for {@link NewExperiment}.
 * 
 * @author Christian Ribeaud
 */
public final class NewExperimentPredicate extends
        DelegatedPredicate<SpaceIdentifier, NewExperiment>
{
    public NewExperimentPredicate()
    {
        super(new ExistingSpaceIdentifierPredicate());
    }

    //
    // DelegatedPredicate
    //

    @Override
    public final SpaceIdentifier tryConvert(final NewExperiment value)
    {
        ExperimentIdentifier identifier =
                new ExperimentIdentifierFactory(value.getIdentifier()).createIdentifier();
        return identifier;
    }

    @Override
    public Status doEvaluation(PersonPE person, List<RoleWithIdentifier> allowedRoles, NewExperiment value)
    {
        IProjectAuthorization<NewExperiment> pa = new ProjectAuthorizationBuilder<NewExperiment>()
                .withData(authorizationDataProvider)
                .withUser(new UserProviderFromPersonPE(person))
                .withRoles(new RolesProviderFromRolesWithIdentifier(allowedRoles))
                .withObjects(new ProjectProviderFromNewExperiment(value))
                .build();

        if (pa.getObjectsWithoutAccess().isEmpty())
        {
            return Status.OK;
        }

        return super.doEvaluation(person, allowedRoles, value);
    }

    @Override
    public final String getCandidateDescription()
    {
        return "new experiment";
    }

}
