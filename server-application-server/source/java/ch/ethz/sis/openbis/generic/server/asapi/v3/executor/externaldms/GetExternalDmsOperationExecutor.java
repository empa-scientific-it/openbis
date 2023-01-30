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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.externaldms;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.get.GetObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.get.GetObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.ExternalDms;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.fetchoptions.ExternalDmsFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.get.GetExternalDmsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.get.GetExternalDmsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.IExternalDmsId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.get.GetObjectsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.get.IMapObjectByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.externaldms.IExternalDmsTranslator;

/**
 * @author anttil
 */
@Component
public class GetExternalDmsOperationExecutor extends GetObjectsOperationExecutor<IExternalDmsId, ExternalDms, ExternalDmsFetchOptions>
        implements IGetExternalDmsOperationExecutor
{

    @Autowired
    private IMapExternalDmsTechIdByIdExecutor mapExecutor;

    @Autowired
    private IExternalDmsTranslator translator;

    @Override
    protected Class<? extends GetObjectsOperation<IExternalDmsId, ExternalDmsFetchOptions>> getOperationClass()
    {
        return GetExternalDmsOperation.class;
    }

    @Override
    protected IMapObjectByIdExecutor<IExternalDmsId, Long> getExecutor()
    {
        return mapExecutor;
    }

    @Override
    protected ITranslator<Long, ExternalDms, ExternalDmsFetchOptions> getTranslator()
    {
        return translator;
    }

    @Override
    protected GetObjectsOperationResult<IExternalDmsId, ExternalDms> getOperationResult(Map<IExternalDmsId, ExternalDms> objectMap)
    {
        return new GetExternalDmsOperationResult(objectMap);
    }

}
