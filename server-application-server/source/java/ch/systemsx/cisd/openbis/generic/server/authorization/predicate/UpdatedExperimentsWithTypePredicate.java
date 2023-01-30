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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewBasicExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.UpdatedExperimentsWithType;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;

/**
 * An <code>IPredicate</code> implementation for {@link UpdatedExperimentsWithType}.
 * 
 * @author Izabela Adamczyk
 */
public final class UpdatedExperimentsWithTypePredicate extends
        AbstractPredicate<UpdatedExperimentsWithType>
{

    private final IPredicate<ProjectIdentifier> delegate;

    @Override
    public final void init(IAuthorizationDataProvider provider)
    {
        delegate.init(provider);
    }

    @Override
    public final Status doEvaluation(final PersonPE person,
            final List<RoleWithIdentifier> allowedRoles, final UpdatedExperimentsWithType value)
    {
        Status s = Status.OK;
        for (NewBasicExperiment experiment : value.getUpdatedExperiments())
        {
            ExperimentIdentifier identifier =
                    new ExperimentIdentifierFactory(experiment.getIdentifier()).createIdentifier();
            s = delegate.evaluate(person, allowedRoles, identifier);
            if (s.equals(Status.OK) == false)
            {
                return s;
            }
        }
        return s;
    }

    // for tests only
    @Deprecated
    UpdatedExperimentsWithTypePredicate(IPredicate<ProjectIdentifier> delegate)
    {
        this.delegate = delegate;
    }

    public UpdatedExperimentsWithTypePredicate()
    {
        delegate = new ProjectIdentifierPredicate();
    }

    @Override
    public final String getCandidateDescription()
    {
        return "new experiments with type";
    }

}
