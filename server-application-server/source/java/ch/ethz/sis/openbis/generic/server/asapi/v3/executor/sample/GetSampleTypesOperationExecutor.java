/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.sample;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.get.GetObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.get.GetObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.get.GetSampleTypesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.get.GetSampleTypesOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractGetEntityTypesOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.sample.ISampleTypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * @author pkupczyk
 */
@Component
public class GetSampleTypesOperationExecutor extends AbstractGetEntityTypesOperationExecutor<SampleType, SampleTypeFetchOptions>
        implements IGetSampleTypesOperationExecutor
{

    @Autowired
    private ISampleTypeTranslator translator;

    @Override
    protected EntityKind getEntityKind()
    {
        return EntityKind.SAMPLE;
    }

    @Override
    protected Class<? extends GetObjectsOperation<IEntityTypeId, SampleTypeFetchOptions>> getOperationClass()
    {
        return GetSampleTypesOperation.class;
    }

    @Override
    protected ITranslator<Long, SampleType, SampleTypeFetchOptions> getTranslator()
    {
        return translator;
    }

    @Override
    protected GetObjectsOperationResult<IEntityTypeId, SampleType> getOperationResult(Map<IEntityTypeId, SampleType> objectMap)
    {
        return new GetSampleTypesOperationResult(objectMap);
    }

}
