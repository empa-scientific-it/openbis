/*
 * Copyright ETH 2016 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.experiment;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.get.GetObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.get.GetObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.get.GetExperimentTypesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.get.GetExperimentTypesOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractGetEntityTypesOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.experiment.IExperimentTypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * @author pkupczyk
 */
@Component
public class GetExperimentTypesOperationExecutor extends AbstractGetEntityTypesOperationExecutor<ExperimentType, ExperimentTypeFetchOptions>
        implements IGetExperimentTypesOperationExecutor
{

    @Autowired
    private IExperimentTypeTranslator translator;

    @Override
    protected EntityKind getEntityKind()
    {
        return EntityKind.EXPERIMENT;
    }

    @Override
    protected Class<? extends GetObjectsOperation<IEntityTypeId, ExperimentTypeFetchOptions>> getOperationClass()
    {
        return GetExperimentTypesOperation.class;
    }

    @Override
    protected ITranslator<Long, ExperimentType, ExperimentTypeFetchOptions> getTranslator()
    {
        return translator;
    }

    @Override
    protected GetObjectsOperationResult<IEntityTypeId, ExperimentType> getOperationResult(Map<IEntityTypeId, ExperimentType> objectMap)
    {
        return new GetExperimentTypesOperationResult(objectMap);
    }

}
