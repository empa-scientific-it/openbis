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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.get.GetObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.get.GetObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.QueryDatabase;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.fetchoptions.QueryDatabaseFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.get.GetQueryDatabasesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.get.GetQueryDatabasesOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.id.IQueryDatabaseId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.get.AbstractGetObjectsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.query.IQueryDatabaseTranslator;
import ch.systemsx.cisd.openbis.plugin.query.shared.DatabaseDefinition;

/**
 * @author pkupczyk
 */
@Component
public class GetQueryDatabasesOperationExecutor extends
        AbstractGetObjectsOperationExecutor<IQueryDatabaseId, DatabaseDefinition, QueryDatabase, QueryDatabaseFetchOptions>
        implements IGetQueryDatabasesOperationExecutor
{

    @Autowired
    private IMapQueryDatabaseByIdExecutor mapExecutor;

    @Autowired
    private IQueryDatabaseTranslator translator;

    @Override
    protected Class<? extends GetObjectsOperation<IQueryDatabaseId, QueryDatabaseFetchOptions>> getOperationClass()
    {
        return GetQueryDatabasesOperation.class;
    }

    @Override
    protected Map<IQueryDatabaseId, DatabaseDefinition> map(IOperationContext context, List<? extends IQueryDatabaseId> ids,
            QueryDatabaseFetchOptions fetchOptions)
    {
        return mapExecutor.map(context, ids);
    }

    @Override
    protected Map<DatabaseDefinition, QueryDatabase> translate(TranslationContext context, Collection<DatabaseDefinition> objects,
            QueryDatabaseFetchOptions fetchOptions)
    {
        return translator.translate(context, objects, fetchOptions);
    }

    @Override
    protected GetObjectsOperationResult<IQueryDatabaseId, QueryDatabase> getOperationResult(Map<IQueryDatabaseId, QueryDatabase> objectMap)
    {
        return new GetQueryDatabasesOperationResult(objectMap);
    }

}
