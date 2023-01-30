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
import ch.systemsx.cisd.openbis.generic.server.authorization.project.IProjectAuthorization;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.ProjectAuthorizationBuilder;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.project.ProjectProviderFromExperimentIdentifierString;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.project.ProjectProviderFromProjectPE;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.role.RolesProviderFromRolesWithIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.user.UserProviderFromPersonPE;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * @author pkupczyk
 */
public class NewSampleIdentifierPredicate extends AbstractPredicate<NewSample>
{

    private IAuthorizationDataProvider dataProvider;

    @Override
    public void init(IAuthorizationDataProvider provider)
    {
        this.dataProvider = provider;
    }

    @Override
    public String getCandidateDescription()
    {
        return "new sample identifier";
    }

    @Override
    protected Status doEvaluation(PersonPE person, List<RoleWithIdentifier> allowedRoles, NewSample value)
    {
        SampleIdentifier identifier = SampleIdentifierFactory.parse(value.getIdentifier(), value.getDefaultSpaceIdentifier());

        SampleIdentifierPredicate identifierPredicate = new SampleIdentifierPredicate(false, true)
            {
                @Override
                protected Status doEvaluationOfExperimentProject(PersonPE iPerson, List<RoleWithIdentifier> iAllowedRoles, SampleIdentifier iValue)
                {
                    return NewSampleIdentifierPredicate.this.doEvaluationOfExperimentProject(person, allowedRoles, value);
                }
            };
        identifierPredicate.init(dataProvider);
        return identifierPredicate.doEvaluation(person, allowedRoles, identifier);
    }

    private Status doEvaluationOfExperimentProject(PersonPE person, List<RoleWithIdentifier> allowedRoles, NewSample value)
    {
        SampleIdentifier identifier = SampleIdentifierFactory.parse(value.getIdentifier(), value.getDefaultSpaceIdentifier());

        // This predicate is used in create, update and createOrUpdate methods, therefore we need to cover cases where a sample already exists or not.

        SpacePE space = dataProvider.tryGetSpace(identifier.getSpaceLevel().getSpaceCode());

        if (space != null)
        {
            SamplePE sample = dataProvider.tryGetSampleBySpaceAndCode(space, identifier.getSampleCode());

            if (sample == null)
            {
                if (value.getExperimentIdentifier() != null)
                {
                    IProjectAuthorization<String> pa = new ProjectAuthorizationBuilder<String>()
                            .withData(dataProvider)
                            .withUser(new UserProviderFromPersonPE(person))
                            .withRoles(new RolesProviderFromRolesWithIdentifier(allowedRoles))
                            .withObjects(new ProjectProviderFromExperimentIdentifierString(value.getExperimentIdentifier()))
                            .build();

                    if (pa.getObjectsWithoutAccess().isEmpty())
                    {
                        return Status.OK;
                    }
                }
            } else
            {
                ProjectPE project = sample.getExperiment() != null ? sample.getExperiment().getProject() : null;

                IProjectAuthorization<ProjectPE> pa = new ProjectAuthorizationBuilder<ProjectPE>()
                        .withData(dataProvider)
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

        return Status.createError();
    }

}
