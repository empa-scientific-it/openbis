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
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.experiment.ExperimentIdentifierId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.experiment.ExperimentPermIdId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.experiment.ExperimentTechIdId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.experiment.IExperimentId;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.PermId;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * Predicate for @IExperimentId instances.
 * 
 * @author Franz-Josef Elmer
 */
public class ExperimentIdPredicate extends AbstractExperimentPredicate<IExperimentId>
{

    @Override
    public String getCandidateDescription()
    {
        return "experiment id";
    }

    @Override
    protected Status doEvaluation(PersonPE person, List<RoleWithIdentifier> allowedRoles,
            IExperimentId experimentId)
    {
        assert projectPredicate.initialized : "Predicate has not been initialized";
        assert experimentTechIdPredicate.initialized : "Predicate has not been initialized";
        assert experimentPermIdPredicate.initialized : "Predicate has not been initialized";
        assert experimentAugmentedCodePredicate.initialized : "Predicate has not been initialized";

        if (experimentId instanceof ExperimentIdentifierId)
        {
            ExperimentIdentifierId identifierId = (ExperimentIdentifierId) experimentId;
            return experimentAugmentedCodePredicate.doEvaluation(person, allowedRoles,
                    identifierId.getIdentifier());
        }
        if (experimentId instanceof ExperimentPermIdId)
        {
            ExperimentPermIdId permIdId = (ExperimentPermIdId) experimentId;
            return experimentPermIdPredicate.doEvaluation(person, allowedRoles,
                    new PermId(permIdId.getPermId()));
        }
        if (experimentId instanceof ExperimentTechIdId)
        {
            ExperimentTechIdId techIdId = (ExperimentTechIdId) experimentId;
            return experimentTechIdPredicate.doEvaluation(person, allowedRoles,
                    new TechId(techIdId.getTechId()));
        }
        return Status.createError("Unsupported experiment id: " + experimentId);
    }

}
