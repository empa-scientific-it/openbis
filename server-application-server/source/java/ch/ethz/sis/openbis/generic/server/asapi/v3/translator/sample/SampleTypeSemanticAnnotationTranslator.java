/*
 * Copyright ETH 2015 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.sample;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.SemanticAnnotation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.fetchoptions.SemanticAnnotationFetchOptions;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectRelationRecord;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectToManyRelationTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.semanticannotation.ISemanticAnnotationTranslator;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.lemnik.eodsql.QueryTool;

/**
 * @author pkupczyk
 */
@Component
public class SampleTypeSemanticAnnotationTranslator extends ObjectToManyRelationTranslator<SemanticAnnotation, SemanticAnnotationFetchOptions>
        implements ISampleTypeSemanticAnnotationTranslator
{

    @Autowired
    private ISemanticAnnotationTranslator annotationTranslator;

    @Override
    protected List<ObjectRelationRecord> loadRecords(LongOpenHashSet propertyTypeIds)
    {
        SampleQuery query = QueryTool.getManagedQuery(SampleQuery.class);
        return query.getTypeAnnotationIds(propertyTypeIds);
    }

    @Override
    protected Map<Long, SemanticAnnotation> translateRelated(TranslationContext context,
            Collection<Long> annotationIds, SemanticAnnotationFetchOptions annotationFetchOptions)
    {
        return annotationTranslator.translate(context, annotationIds, annotationFetchOptions);
    }

    @Override
    protected Collection<SemanticAnnotation> createCollection()
    {
        return new ArrayList<>();
    }

}
