/*
 * Copyright 2016 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.entity.dataset;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.entity.common.ObjectRelationRecord;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.entity.property.PropertyAssignmentTranslator;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.lemnik.eodsql.QueryTool;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Component
public class DataSetPropertyAssignmentTranslator extends PropertyAssignmentTranslator implements IDataSetPropertyAssignmentTranslator
{

    @Override
    protected List<ObjectRelationRecord> loadRecords(LongOpenHashSet dataSetTypeIds)
    {
        DataSetQuery query = QueryTool.getManagedQuery(DataSetQuery.class);
        return query.getPropertyAssignmentIds(dataSetTypeIds);
    }

    @Override
    protected Map<Long, PropertyAssignment> translateRelated(TranslationContext context, 
            Collection<Long> dataSetTypePropertyTypeIds, PropertyAssignmentFetchOptions relatedFetchOptions)
    {
        DataSetQuery query = QueryTool.getManagedQuery(DataSetQuery.class);
        return getAssignments(query.getPropertyAssignments(new LongOpenHashSet(dataSetTypePropertyTypeIds)));
    }

}
