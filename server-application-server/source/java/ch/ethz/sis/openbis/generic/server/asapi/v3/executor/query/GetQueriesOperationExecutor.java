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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.query;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.get.GetObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.get.GetObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.Query;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.fetchoptions.QueryFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.get.GetQueriesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.get.GetQueriesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.IQueryId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.get.GetObjectsPEOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.get.IMapObjectByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.query.IQueryTranslator;
import ch.systemsx.cisd.openbis.generic.shared.dto.QueryPE;

/**
 * @author pkupczyk
 */
@Component
public class GetQueriesOperationExecutor extends GetObjectsPEOperationExecutor<IQueryId, QueryPE, Query, QueryFetchOptions>
        implements IGetQueriesOperationExecutor
{

    @Autowired
    private IMapQueryByIdExecutor mapExecutor;

    @Autowired
    private IQueryTranslator translator;

    @Override
    protected Class<? extends GetObjectsOperation<IQueryId, QueryFetchOptions>> getOperationClass()
    {
        return GetQueriesOperation.class;
    }

    @Override
    protected IMapObjectByIdExecutor<IQueryId, QueryPE> getExecutor()
    {
        return mapExecutor;
    }

    @Override
    protected ITranslator<Long, Query, QueryFetchOptions> getTranslator()
    {
        return translator;
    }

    @Override
    protected GetObjectsOperationResult<IQueryId, Query> getOperationResult(Map<IQueryId, Query> objectMap)
    {
        return new GetQueriesOperationResult(objectMap);
    }

}
