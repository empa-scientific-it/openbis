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

package ch.systemsx.cisd.openbis.systemtest.authorization.validator.dataset;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.ReturnValueFilter;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.DataSetByExperimentOrSampleIdentifierValidator;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.DataSetPEByExperimentOrSampleIdentifierValidator;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.ExternalDataValidator;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSessionProvider;

/**
 * @author pkupczyk
 */
@Component
public class DataSetValidatorTestService
{

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    @ReturnValueFilter(validatorClass = DataSetByExperimentOrSampleIdentifierValidator.class)
    public DataSet testDataSetByExperimentOrSampleIdentifierValidator(IAuthSessionProvider sessionProvider, DataSet dataSet)
    {
        return dataSet;
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    @ReturnValueFilter(validatorClass = DataSetPEByExperimentOrSampleIdentifierValidator.class)
    public DataPE testDataSetPEByExperimentOrSampleIdentifierValidator(IAuthSessionProvider sessionProvider, DataPE dataSet)
    {
        return dataSet;
    }

    @Transactional
    @RolesAllowed(value = { RoleWithHierarchy.PROJECT_OBSERVER })
    @ReturnValueFilter(validatorClass = ExternalDataValidator.class)
    public AbstractExternalData testExternalDataValidator(IAuthSessionProvider sessionProvider, AbstractExternalData externalData)
    {
        return externalData;
    }

}
