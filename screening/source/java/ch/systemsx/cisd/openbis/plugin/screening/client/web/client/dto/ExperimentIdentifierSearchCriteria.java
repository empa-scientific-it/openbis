/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.dto;

import ch.systemsx.cisd.common.shared.basic.utils.StringUtils;

/**
 * Allows to search in one experiment (given by identifier) or in all of them.
 * 
 * @author Tomasz Pylak
 */
public class ExperimentIdentifierSearchCriteria
{
    public static ExperimentIdentifierSearchCriteria createSearchAll()
    {
        return new ExperimentIdentifierSearchCriteria(null);
    }

    // if null, all experiments are taken into account
    private final String experimentIdentifierOrNull;

    public ExperimentIdentifierSearchCriteria(String experimentIdentifierOrNull)
    {
        this.experimentIdentifierOrNull = experimentIdentifierOrNull;
    }

    public String tryGetExperimentIdentifier()
    {
        return experimentIdentifierOrNull;
    }

    public boolean searchAllExperiments()
    {
        return StringUtils.isBlank(experimentIdentifierOrNull);
    }

}
