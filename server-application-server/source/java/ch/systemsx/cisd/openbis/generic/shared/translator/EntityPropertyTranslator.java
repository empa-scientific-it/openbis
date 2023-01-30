/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.translator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.common.collection.IValidator;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyWithSampleDataTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;

/**
 * Translates {@link EntityPropertyPE} to {@link IEntityProperty}.
 * 
 * @author Izabela Adamczyk
 */
public final class EntityPropertyTranslator
{
    private EntityPropertyTranslator()
    {
        // Can not be instantiated.
    }

    public final static IEntityProperty translate(final EntityPropertyPE propertyPE,
            Map<MaterialTypePE, MaterialType> materialTypeCache,
            Map<PropertyTypePE, PropertyType> cacheOrNull,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            IValidator<IIdentifierHolder> samplePropertyAccessValidator)
    {
        return translate(propertyPE, materialTypeCache, cacheOrNull, false, managedPropertyEvaluatorFactory,
                samplePropertyAccessValidator);
    }

    public final static IEntityProperty translate(final EntityPropertyPE propertyPE,
            Map<MaterialTypePE, MaterialType> materialTypeCache,
            Map<PropertyTypePE, PropertyType> cacheOrNull, boolean rawManagedProperties,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            IValidator<IIdentifierHolder> samplePropertyAccessValidator)
    {
        final IEntityProperty basicProperty =
                PropertyTranslatorUtils.createEntityProperty(propertyPE);
        final PropertyType propertyType =
                PropertyTypeTranslator.translate(propertyPE.getEntityTypePropertyType()
                        .getPropertyType(), materialTypeCache, cacheOrNull);
        final Long ordinal = propertyPE.getEntityTypePropertyType().getOrdinal();

        PropertyTranslatorUtils.initializeEntityProperty(basicProperty, propertyType, ordinal);

        final DataTypeCode typeCode = PropertyTranslatorUtils.getDataTypeCode(propertyPE);
        switch (typeCode)
        {
            case CONTROLLEDVOCABULARY:
                basicProperty.setVocabularyTerm(VocabularyTermTranslator.translate(propertyPE
                        .getVocabularyTerm()));
                break;
            case MATERIAL:
                basicProperty
                        .setMaterial(MaterialTranslator.translate(propertyPE.getMaterialValue(),
                                false, null, managedPropertyEvaluatorFactory, samplePropertyAccessValidator));
                break;
            case SAMPLE:
                if (propertyPE instanceof EntityPropertyWithSampleDataTypePE)
                {
                    SamplePE sampleProperty = ((EntityPropertyWithSampleDataTypePE) propertyPE).getSampleValue();
                    if (samplePropertyAccessValidator.isValid(sampleProperty))
                    {
                        basicProperty.setSample(SampleTranslator.translateWithoutRevealingData(sampleProperty));
                    }
                }
                break;
            default:
                basicProperty.setValue(propertyPE.tryGetUntypedValue());
        }

        final IEntityProperty result;
        if (propertyPE.getEntityTypePropertyType().isManaged() && rawManagedProperties == false)
        {
            result =
                    PropertyTranslatorUtils.createManagedEntityProperty(propertyPE, basicProperty,
                            managedPropertyEvaluatorFactory);
            PropertyTranslatorUtils.initializeEntityProperty(result, propertyType, ordinal);
        } else
        {
            result = basicProperty;
        }

        return result;
    }

    public final static List<IEntityProperty> translateRaw(
            final Set<? extends EntityPropertyPE> list,
            Map<MaterialTypePE, MaterialType> materialTypeCache,
            Map<PropertyTypePE, PropertyType> cacheOrNull,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            IValidator<IIdentifierHolder> samplePropertyAccessValidator)
    {
        if (list == null)
        {
            return null;
        }
        final List<IEntityProperty> result = new ArrayList<IEntityProperty>();
        for (final EntityPropertyPE property : list)
        {
            result.add(translate(property, materialTypeCache, cacheOrNull, true, managedPropertyEvaluatorFactory,
                    samplePropertyAccessValidator));
        }
        return result;
    }

    public final static List<IEntityProperty> translate(final Set<? extends EntityPropertyPE> list,
            Map<MaterialTypePE, MaterialType> materialTypeCache,
            Map<PropertyTypePE, PropertyType> cacheOrNull,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            IValidator<IIdentifierHolder> samplePropertyAccessValidator)
    {
        if (list == null)
        {
            return null;
        }
        final List<IEntityProperty> result = new ArrayList<IEntityProperty>();
        for (final EntityPropertyPE property : list)
        {
            result.add(translate(property, materialTypeCache, cacheOrNull, managedPropertyEvaluatorFactory,
                    samplePropertyAccessValidator));
        }
        return result;
    }

    public final static IEntityProperty[] translate(final EntityPropertyPE[] list,
            Map<MaterialTypePE, MaterialType> materialTypeCache,
            Map<PropertyTypePE, PropertyType> cacheOrNull,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            IValidator<IIdentifierHolder> samplePropertyAccessValidator)
    {
        if (list == null)
        {
            return null;
        }
        final IEntityProperty[] result = new IEntityProperty[list.length];
        int idx = 0;
        for (final EntityPropertyPE property : list)
        {
            result[idx++] = translate(property, materialTypeCache, cacheOrNull, managedPropertyEvaluatorFactory,
                    samplePropertyAccessValidator);
        }
        return result;
    }
}
