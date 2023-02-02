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

package ch.systemsx.cisd.openbis.plugin.screening.server.authorization;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.DelegatedPredicate;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.ExperimentSearchCriteria;

/**
 * @author Tomasz Pylak
 * @author Kaloyan Enimanev
 */
public final class WellSearchCriteriaPredicate extends
        DelegatedPredicate<ExperimentSearchCriteria, WellSearchCriteria>
{

    public WellSearchCriteriaPredicate()
    {
        super(new ExperimentSearchCriteriaPredicate());
    }

    @Override
    public ExperimentSearchCriteria tryConvert(WellSearchCriteria value)
    {
        if (value == null)
        {
            throw UserFailureException.fromTemplate("No well search criteria specified.");
        }

        return value.getExperimentCriteria();
    }

    @Override
    public String getCandidateDescription()
    {
        return "Well search criteria";
    }

}
