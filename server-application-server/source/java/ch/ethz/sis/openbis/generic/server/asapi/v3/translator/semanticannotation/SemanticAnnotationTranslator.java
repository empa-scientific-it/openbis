/*
 * Copyright 2013 ETH Zuerich, CISD
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.SemanticAnnotation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.fetchoptions.SemanticAnnotationFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.id.SemanticAnnotationPermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationResults;

/**
 * @author pkupczyk
 */
@Component
public class SemanticAnnotationTranslator extends AbstractCachingTranslator<Long, SemanticAnnotation, SemanticAnnotationFetchOptions>
        implements ISemanticAnnotationTranslator
{

    @Autowired
    private ISemanticAnnotationEntityTypeTranslator entityTypeTranslator;

    @Autowired
    private ISemanticAnnotationPropertyTypeTranslator propertyTypeTranslator;

    @Autowired
    private ISemanticAnnotationPropertyAssignmentTranslator propertyAssignmentTranslator;

    @Autowired
    private ISemanticAnnotationBaseTranslator baseTranslator;

    @Override
    protected SemanticAnnotation createObject(TranslationContext context, Long annotationId, SemanticAnnotationFetchOptions fetchOptions)
    {
        SemanticAnnotation annotation = new SemanticAnnotation();
        annotation.setFetchOptions(new SemanticAnnotationFetchOptions());
        return annotation;
    }

    @Override
    protected Object getObjectsRelations(TranslationContext context, Collection<Long> annotationIds, SemanticAnnotationFetchOptions fetchOptions)
    {
        TranslationResults relations = new TranslationResults();

        relations.put(ISemanticAnnotationBaseTranslator.class, baseTranslator.translate(context, annotationIds, null));

        if (fetchOptions.hasEntityType())
        {
            relations.put(ISemanticAnnotationEntityTypeTranslator.class,
                    entityTypeTranslator.translate(context, annotationIds, fetchOptions.withEntityType()));
        }

        if (fetchOptions.hasPropertyType())
        {
            relations.put(ISemanticAnnotationPropertyTypeTranslator.class,
                    propertyTypeTranslator.translate(context, annotationIds, fetchOptions.withPropertyType()));
        }

        if (fetchOptions.hasPropertyAssignment())
        {
            relations.put(ISemanticAnnotationPropertyAssignmentTranslator.class,
                    propertyAssignmentTranslator.translate(context, annotationIds, fetchOptions.withPropertyAssignment()));
        }

        return relations;
    }

    @Override
    protected void updateObject(TranslationContext context, Long annotationId, SemanticAnnotation result, Object objectRelations,
            SemanticAnnotationFetchOptions fetchOptions)
    {
        TranslationResults relations = (TranslationResults) objectRelations;
        SemanticAnnotationBaseRecord baseRecord = relations.get(ISemanticAnnotationBaseTranslator.class, annotationId);

        result.setPermId(new SemanticAnnotationPermId(baseRecord.permId));
        result.setPredicateOntologyId(baseRecord.predicateOntologyId);
        result.setPredicateOntologyVersion(baseRecord.predicateOntologyVersion);
        result.setPredicateAccessionId(baseRecord.predicateAccessionId);
        result.setDescriptorOntologyId(baseRecord.descriptorOntologyId);
        result.setDescriptorOntologyVersion(baseRecord.descriptorOntologyVersion);
        result.setDescriptorAccessionId(baseRecord.descriptorAccessionId);
        result.setCreationDate(baseRecord.creationDate);

        if (fetchOptions.hasEntityType())
        {
            result.setEntityType(relations.get(ISemanticAnnotationEntityTypeTranslator.class, annotationId));
            result.getFetchOptions().withEntityTypeUsing(fetchOptions.withEntityType());
        }

        if (fetchOptions.hasPropertyType())
        {
            result.setPropertyType(relations.get(ISemanticAnnotationPropertyTypeTranslator.class, annotationId));
            result.getFetchOptions().withPropertyTypeUsing(fetchOptions.withPropertyType());
        }

        if (fetchOptions.hasPropertyAssignment())
        {
            result.setPropertyAssignment(relations.get(ISemanticAnnotationPropertyAssignmentTranslator.class, annotationId));
            result.getFetchOptions().withPropertyAssignmentUsing(fetchOptions.withPropertyAssignment());
        }
    }

}
