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

package ch.systemsx.cisd.openbis.plugin.screening.shared.authorization;

import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.DelegatedPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.AbstractTechIdPredicate.ExperimentTechIdPredicate;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMaterialsSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMaterialsSearchCriteria.SingleExperimentSearchCriteria;

/**
 * @author Tomasz Pylak
 */
public final class PlateMaterialsSearchCriteriaPredicate extends
        DelegatedPredicate<TechId, PlateMaterialsSearchCriteria>
{
    public PlateMaterialsSearchCriteriaPredicate()
    {
        super(new ExperimentTechIdPredicate());
    }

    @Override
    public final String getCandidateDescription()
    {
        return "plate materials search criteria";
    }

    @Override
    public TechId tryConvert(PlateMaterialsSearchCriteria value)
    {
        SingleExperimentSearchCriteria exp = value.getExperimentCriteria().tryGetExperiment();
        if (exp != null)
        {
            return exp.getExperimentId();
        } else
        {
            return null;
        }
    }

}
