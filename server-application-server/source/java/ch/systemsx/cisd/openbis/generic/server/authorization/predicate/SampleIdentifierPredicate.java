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
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.project.ProjectProviderFromProjectIdentifierObject;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.project.ProjectProviderFromSpaceCodeAndSampleCode;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.project.ProjectProviderFromSpaceCodeAndSampleCode.SpaceCodeAndSampleCode;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.role.RolesProviderFromRolesWithIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.project.provider.user.UserProviderFromPersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.util.SpaceCodeHelper;

/**
 * @author pkupczyk
 */
public class SampleIdentifierPredicate extends AbstractPredicate<SampleIdentifier>
{

    private IAuthorizationDataProvider dataProvider;

    private final SpaceIdentifierPredicate spacePredicate;

    private final DatabaseInstanceIdentifierPredicate databaseInstanceIdentifierPredicate;

    boolean initialized;

    public SampleIdentifierPredicate()
    {
        this(true, false);
    }

    public SampleIdentifierPredicate(boolean isReadAccess)
    {
        this(isReadAccess, false);
    }

    public SampleIdentifierPredicate(boolean isReadAccess, boolean okForNonExistentSpaces)
    {
        spacePredicate = new SpaceIdentifierPredicate(okForNonExistentSpaces);
        databaseInstanceIdentifierPredicate = new DatabaseInstanceIdentifierPredicate(isReadAccess);
    }

    //
    // AbstractPredicate
    //

    @Override
    public final void init(IAuthorizationDataProvider provider)
    {
        assert initialized == false;
        dataProvider = provider;
        spacePredicate.init(provider);
        databaseInstanceIdentifierPredicate.init(provider);
        initialized = true;
    }

    @Override
    public final String getCandidateDescription()
    {
        return "sample identifier";
    }

    @Override
    protected final Status doEvaluation(final PersonPE person,
            final List<RoleWithIdentifier> allowedRoles, final SampleIdentifier value)
    {
        if (hasInstanceWritePermissions(person, allowedRoles).isOK())
        {
            return Status.OK;
        }

        if (value.isProjectLevel())
        {
            Status spaceResult = spacePredicate.doEvaluation(person, allowedRoles, value.getSpaceLevel());

            if (spaceResult.isError())
            {
                Status projectResult = doEvaluationOfProject(person, allowedRoles, value);

                if (projectResult.isOK())
                {
                    return Status.OK;
                }
            }

            return spaceResult;
        } else if (value.isSpaceLevel())
        {
            Status spaceResult = spacePredicate.doEvaluation(person, allowedRoles, value.getSpaceLevel());

            if (spaceResult.isError())
            {
                Status projectResult = doEvaluationOfExperimentProject(person, allowedRoles, value);

                if (projectResult.isOK())
                {
                    return Status.OK;
                }
            }

            return spaceResult;
        } else if (value.isDatabaseInstanceLevel())
        {
            return databaseInstanceIdentifierPredicate.doEvaluation(person, allowedRoles, null);
        } else
        {
            throw new RuntimeException("Unsupported sample identifier '" + value.toString() + "'");
        }
    }

    protected Status doEvaluationOfProject(PersonPE person, List<RoleWithIdentifier> allowedRoles, SampleIdentifier value)
    {
        IProjectAuthorization<ProjectIdentifier> pa = new ProjectAuthorizationBuilder<ProjectIdentifier>()
                .withData(dataProvider)
                .withUser(new UserProviderFromPersonPE(person))
                .withRoles(new RolesProviderFromRolesWithIdentifier(allowedRoles))
                .withObjects(new ProjectProviderFromProjectIdentifierObject(value.getProjectLevel()))
                .build();

        if (pa.getObjectsWithoutAccess().isEmpty())
        {
            return Status.OK;
        } else
        {
            return Status.createError();
        }
    }

    protected Status doEvaluationOfExperimentProject(PersonPE person, List<RoleWithIdentifier> allowedRoles, SampleIdentifier value)
    {
        String spaceCode = SpaceCodeHelper.getSpaceCode(person, value.getSpaceLevel());
        String sampleCode = value.getSampleCode();

        if (sampleCode == null)
        {
            return Status.createError();
        }

        IProjectAuthorization<SpaceCodeAndSampleCode> pa = new ProjectAuthorizationBuilder<SpaceCodeAndSampleCode>()
                .withData(dataProvider)
                .withUser(new UserProviderFromPersonPE(person))
                .withRoles(new RolesProviderFromRolesWithIdentifier(allowedRoles))
                .withObjects(new ProjectProviderFromSpaceCodeAndSampleCode(spaceCode, sampleCode))
                .build();

        if (pa.getObjectsWithoutAccess().isEmpty())
        {
            return Status.OK;
        } else
        {
            return Status.createError();
        }
    }

}
