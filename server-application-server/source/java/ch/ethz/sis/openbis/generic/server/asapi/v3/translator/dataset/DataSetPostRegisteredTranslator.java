/*
 * Copyright 2015 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.dataset;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.lemnik.eodsql.QueryTool;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.EmptyFetchOptions;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectHolder;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class DataSetPostRegisteredTranslator extends AbstractCachingTranslator<Long, ObjectHolder<Boolean>, EmptyFetchOptions> implements
        IDataSetPostRegisteredTranslator
{

    @Override
    protected ObjectHolder<Boolean> createObject(TranslationContext context, Long input, EmptyFetchOptions fetchOptions)
    {
        return new ObjectHolder<Boolean>();
    }

    @Override
    protected Object getObjectsRelations(TranslationContext context, Collection<Long> dataSetIds, EmptyFetchOptions fetchOptions)
    {
        DataSetQuery query = QueryTool.getManagedQuery(DataSetQuery.class);
        Set<Long> notPostregisteredDataSets = new HashSet<Long>(query.getNotPostRegisteredDataSets(new LongOpenHashSet(dataSetIds)));
        Map<Long, Boolean> map = new HashMap<Long, Boolean>();
        for (Long dataSetId : dataSetIds)
        {
            map.put(dataSetId, notPostregisteredDataSets.contains(dataSetId) == false);
        }
        return map;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void updateObject(TranslationContext context, Long dataSetId, ObjectHolder<Boolean> output, Object relations,
            EmptyFetchOptions fetchOptions)
    {
        Map<Long, Boolean> entriesMap = (Map<Long, Boolean>) relations;
        output.setObject(entriesMap.get(dataSetId));
    }

}
