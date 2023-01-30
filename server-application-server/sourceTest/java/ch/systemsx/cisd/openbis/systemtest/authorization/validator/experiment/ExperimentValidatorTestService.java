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

package ch.systemsx.cisd.openbis.systemtest.authorization.validator.experiment;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.ReturnValueFilter;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.ExperimentByIdentiferValidator;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.ExperimentValidator;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSessionProvider;

/**
 * @author pkupczyk
 */
@Component
public class ExperimentValidatorTestService
{

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    @ReturnValueFilter(validatorClass = ExperimentByIdentiferValidator.class)
    public Experiment testExperimentByIdentifierValidator(IAuthSessionProvider sessionProvider, Experiment experiment)
    {
        return experiment;
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    @ReturnValueFilter(validatorClass = ExperimentValidator.class)
    public Experiment testExperimentValidator(IAuthSessionProvider sessionProvider, Experiment experiment)
    {
        return experiment;
    }

}
