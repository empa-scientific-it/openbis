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
package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.property;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.SemanticAnnotation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.CommonUtils;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationResults;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeConverter;

/**
 * @author pkupczyk
 */
@Component
public class PropertyTypeTranslator extends AbstractCachingTranslator<Long, PropertyType, PropertyTypeFetchOptions> implements
        IPropertyTypeTranslator
{

    @Autowired
    private IPropertyTypeBaseTranslator baseTranslator;

    @Autowired
    private IPropertyTypeVocabularyTranslator vocabularyTranslator;

    @Autowired
    private IPropertyTypeMaterialTypeTranslator materialTypeTranslator;

    @Autowired
    private IPropertyTypeSampleTypeTranslator sampleTypeTranslator;

    @Autowired
    private IPropertyTypeRegistratorTranslator registratorTranslator;

    @Autowired
    private IPropertyTypeSemanticAnnotationTranslator annotationTranslator;

    @Override
    protected PropertyType createObject(TranslationContext context, Long typeId, PropertyTypeFetchOptions fetchOptions)
    {
        final PropertyType type = new PropertyType();
        type.setFetchOptions(fetchOptions);
        return type;
    }

    @Override
    protected TranslationResults getObjectsRelations(TranslationContext context, Collection<Long> typeIds,
            PropertyTypeFetchOptions fetchOptions)
    {
        TranslationResults relations = new TranslationResults();

        relations.put(IPropertyTypeBaseTranslator.class, baseTranslator.translate(context, typeIds, null));

        if (fetchOptions.hasVocabulary())
        {
            relations.put(IPropertyTypeVocabularyTranslator.class,
                    vocabularyTranslator.translate(context, typeIds, fetchOptions.withVocabulary()));
        }

        if (fetchOptions.hasMaterialType())
        {
            relations.put(IPropertyTypeMaterialTypeTranslator.class,
                    materialTypeTranslator.translate(context, typeIds, fetchOptions.withMaterialType()));
        }

        if (fetchOptions.hasSampleType())
        {
            relations.put(IPropertyTypeSampleTypeTranslator.class,
                    sampleTypeTranslator.translate(context, typeIds, fetchOptions.withSampleType()));
        }

        if (fetchOptions.hasRegistrator())
        {
            relations.put(IPropertyTypeRegistratorTranslator.class,
                    registratorTranslator.translate(context, typeIds, fetchOptions.withRegistrator()));
        }

        if (fetchOptions.hasSemanticAnnotations())
        {
            relations.put(IPropertyTypeSemanticAnnotationTranslator.class,
                    annotationTranslator.translate(context, typeIds, fetchOptions.withSemanticAnnotations()));
        }

        return relations;
    }

    @Override
    protected void updateObject(TranslationContext context, Long typeId, PropertyType result, Object objectRelations,
            PropertyTypeFetchOptions fetchOptions)
    {
        TranslationResults relations = (TranslationResults) objectRelations;
        PropertyTypeRecord baseRecord = relations.get(IPropertyTypeBaseTranslator.class, typeId);

        String businessCode = CodeConverter.tryToBusinessLayer(baseRecord.code, baseRecord.is_managed_internally);

        result.setCode(businessCode);
        result.setPermId(new PropertyTypePermId(businessCode));
        result.setLabel(baseRecord.label);
        result.setDescription(baseRecord.description);
        result.setDataType(DataType.valueOf(baseRecord.data_type));
        result.setManagedInternally(baseRecord.is_managed_internally);
        result.setSchema(baseRecord.schema);
        result.setTransformation(baseRecord.transformation);
        result.setRegistrationDate(baseRecord.registration_timestamp);
        result.setMetaData(CommonUtils.asMap(baseRecord.meta_data));
        result.setMultiValue(baseRecord.is_multi_value);

        if (fetchOptions.hasVocabulary())
        {
            result.setVocabulary(relations.get(IPropertyTypeVocabularyTranslator.class, typeId));
            result.getFetchOptions().withVocabularyUsing(fetchOptions.withVocabulary());
        }

        if (fetchOptions.hasMaterialType())
        {
            result.setMaterialType(relations.get(IPropertyTypeMaterialTypeTranslator.class, typeId));
            result.getFetchOptions().withMaterialTypeUsing(fetchOptions.withMaterialType());
        }

        if (fetchOptions.hasSampleType())
        {
            result.setSampleType(relations.get(IPropertyTypeSampleTypeTranslator.class, typeId));
            result.getFetchOptions().withSampleTypeUsing(fetchOptions.withSampleType());
        }

        if (fetchOptions.hasRegistrator())
        {
            result.setRegistrator(relations.get(IPropertyTypeRegistratorTranslator.class, typeId));
            result.getFetchOptions().withRegistratorUsing(fetchOptions.withRegistrator());
        }

        if (fetchOptions.hasSemanticAnnotations())
        {
            result.setSemanticAnnotations((List<SemanticAnnotation>) relations.get(IPropertyTypeSemanticAnnotationTranslator.class, typeId));
            result.getFetchOptions().withSemanticAnnotationsUsing(fetchOptions.withSemanticAnnotations());
        }

    }

}
