/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.semanticannotation;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IEntityType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.fetchoptions.EntityTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectRelationRecord;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectToOneRelationTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.sample.ISampleTypeTranslator;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.lemnik.eodsql.QueryTool;

/**
 * @author pkupczyk
 */
@Component
public class SemanticAnnotationEntityTypeTranslator extends ObjectToOneRelationTranslator<IEntityType, EntityTypeFetchOptions>
        implements ISemanticAnnotationEntityTypeTranslator
{

    @Autowired
    private ISampleTypeTranslator sampleTypeTranslator;

    @Override
    protected List<ObjectRelationRecord> loadRecords(LongOpenHashSet annotationIds)
    {
        SemanticAnnotationQuery query = QueryTool.getManagedQuery(SemanticAnnotationQuery.class);
        return query.getSampleTypeIds(new LongOpenHashSet(annotationIds));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    protected Map<Long, IEntityType> translateRelated(TranslationContext context, Collection<Long> relatedIds,
            EntityTypeFetchOptions relatedFetchOptions)
    {
        SampleTypeFetchOptions sampleTypeFetchOptions = new SampleTypeFetchOptions();

        if (relatedFetchOptions.hasPropertyAssignments())
        {
            sampleTypeFetchOptions.withPropertyAssignmentsUsing(relatedFetchOptions.withPropertyAssignments());
        }

        return (Map) sampleTypeTranslator.translate(context, relatedIds, sampleTypeFetchOptions);
    }

}
