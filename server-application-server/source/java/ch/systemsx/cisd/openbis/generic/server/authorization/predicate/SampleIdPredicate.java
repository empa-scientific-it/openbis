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
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.sample.ISampleId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.sample.SampleIdentifierId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.sample.SamplePermIdId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.sample.SampleTechIdId;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.PermId;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * Predicate for @ISampleId instances.
 * 
 * @author Franz-Josef Elmer
 */
public class SampleIdPredicate extends AbstractSamplePredicate<ISampleId>
{

    public SampleIdPredicate()
    {
        super(true);
    }

    @Override
    public String getCandidateDescription()
    {
        return "sample id";
    }

    @Override
    protected Status doEvaluation(PersonPE person, List<RoleWithIdentifier> allowedRoles,
            ISampleId sampleId)
    {
        assert spacePredicate.initialized : "Predicate has not been initialized";
        assert sampleTechIdPredicate.initialized : "Predicate has not been initialized";
        assert samplePermIdPredicate.initialized : "Predicate has not been initialized";
        assert sampleAugmentedCodePredicate.initialized : "Predicate has not been initialized";

        if (sampleId instanceof SampleIdentifierId)
        {
            SampleIdentifierId identifierId = (SampleIdentifierId) sampleId;
            return sampleAugmentedCodePredicate.doEvaluation(person, allowedRoles,
                    identifierId.getIdentifier());
        }
        if (sampleId instanceof SamplePermIdId)
        {
            SamplePermIdId permIdId = (SamplePermIdId) sampleId;
            return samplePermIdPredicate.doEvaluation(person, allowedRoles,
                    new PermId(permIdId.getPermId()));
        }
        if (sampleId instanceof SampleTechIdId)
        {
            SampleTechIdId techIdId = (SampleTechIdId) sampleId;
            return sampleTechIdPredicate.doEvaluation(person, allowedRoles,
                    new TechId(techIdId.getTechId()));
        }
        return Status.createError("Unsupported sample id: " + sampleId);
    }

}
