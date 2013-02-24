/*
 * Copyright 2013 ETH Zuerich, CISD
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
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.PermId;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * An <code>IPredicate</code> implementation based on {@link ExperimentIdentifier}.
 * 
 * @author Bernd Rinn
 */
public class ExperimentIdentifierPredicate extends
        AbstractExperimentPredicate<ExperimentIdentifier>
{
    @Override
    public final String getCandidateDescription()
    {
        return "experiment identifier";
    }

    @Override
    protected Status doEvaluation(final PersonPE person,
            final List<RoleWithIdentifier> allowedRoles,
            final ExperimentIdentifier identifier)
    {
        assert spacePredicate.initialized : "Predicate has not been initialized";
        assert experimentTechIdPredicate.initialized : "Predicate has not been initialized";
        assert experimentPermIdPredicate.initialized : "Predicate has not been initialized";
        assert experimentAugmentedCodePredicate.initialized : "Predicate has not been initialized";
        Status status = null;
        if (identifier.getDatabaseId() != null)
        {
            status = experimentTechIdPredicate.doEvaluation(person,
                    allowedRoles, new TechId(identifier.getDatabaseId()));
            if (Status.OK.equals(status) == false)
            {
                return status;
            }
        }
        if (identifier.getPermId() != null)
        {
            status = experimentPermIdPredicate.doEvaluation(person,
                    allowedRoles, new PermId(identifier.getPermId()));
            if (Status.OK.equals(status) == false)
            {
                return status;
            }
        }
        if (identifier.getAugmentedCode() != null)
        {
            status = experimentAugmentedCodePredicate.doEvaluation(person,
                    allowedRoles, identifier.getAugmentedCode());
            if (Status.OK.equals(status) == false)
            {
                return status;
            }
        }
        if (status == null)
        {
            return Status.createError("No identifier given");
        }
        return Status.OK;
    }
}
