/*
 * Copyright 2017 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.screening.systemtests.authorization.validator.experiment;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.ReturnValueFilter;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSessionProvider;
import ch.systemsx.cisd.openbis.plugin.screening.server.authorization.MaterialExperimentFeatureVectorSummaryValidator;
import ch.systemsx.cisd.openbis.plugin.screening.server.authorization.ScreeningExperimentValidator;
import ch.systemsx.cisd.openbis.plugin.screening.server.authorization.WellContentValidator;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialSimpleFeatureVectorSummary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellContent;

/**
 * @author pkupczyk
 */
@Component
public class ExperimentValidatorScreeningTestService
{

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    @ReturnValueFilter(validatorClass = ScreeningExperimentValidator.class)
    public ExperimentIdentifier testScreeningExperimentValidator(IAuthSessionProvider sessionProvider, ExperimentIdentifier experimentIdentifier)
    {
        return experimentIdentifier;
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    @ReturnValueFilter(validatorClass = MaterialExperimentFeatureVectorSummaryValidator.class)
    public MaterialSimpleFeatureVectorSummary testMaterialExperimentFeatureVectorSummaryValidator(IAuthSessionProvider sessionProvider,
            MaterialSimpleFeatureVectorSummary summary)
    {
        return summary;
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    @ReturnValueFilter(validatorClass = WellContentValidator.class)
    public WellContent testWellContentValidator(IAuthSessionProvider sessionProvider, WellContent wellContent)
    {
        return wellContent;
    }

}
