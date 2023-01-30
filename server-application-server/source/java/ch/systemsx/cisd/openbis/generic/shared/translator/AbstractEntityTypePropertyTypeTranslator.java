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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * Encapsulates the translation logic of ETPEs.
 * 
 * @author Izabela Adamczyk
 */
abstract public class AbstractEntityTypePropertyTypeTranslator<ET extends EntityType, ETPT extends EntityTypePropertyType<ET>, ETPTPE extends EntityTypePropertyTypePE>
{

    public ETPT translate(ETPTPE entityTypePropertyType, Map<MaterialTypePE, MaterialType> materialTypeCache, 
            Map<PropertyTypePE, PropertyType> cacheOrNull)
    {
        return translate(entityTypePropertyType, null, null, materialTypeCache, cacheOrNull);
    }

    protected final List<ETPT> translate(final Set<ETPTPE> sampleTypePropertyTypes,
            final ET sampleType, Map<MaterialTypePE, MaterialType> materialTypeCache, 
            Map<PropertyTypePE, PropertyType> cacheOrNull)
    {
        if (HibernateUtils.isInitialized(sampleTypePropertyTypes) == false)
        {
            return DtoConverters.createUnmodifiableEmptyList();
        }
        final List<ETPT> result = new ArrayList<ETPT>();
        for (final ETPTPE sampleTypePropertyType : sampleTypePropertyTypes)
        {
            result.add(translate(sampleTypePropertyType, sampleType, null, materialTypeCache, cacheOrNull));
        }
        Collections.sort(result);
        return result;
    }

    private final ETPT translate(final ETPTPE etptPE, final ET entityType,
            final PropertyType propertyType, Map<MaterialTypePE, MaterialType> materialTypeCache, 
            Map<PropertyTypePE, PropertyType> cacheOrNull)
    {
        ETPT result = create();
        if (propertyType != null)
        {
            result.setPropertyType(propertyType);
        } else
        {
            if (entityType != null && (entityType instanceof MaterialType))
            {
                result.setPropertyType(PropertyTypeTranslator.translate(etptPE.getPropertyType(),
                        (MaterialType) entityType, materialTypeCache, cacheOrNull));
            } else
            {
                result.setPropertyType(PropertyTypeTranslator.translate(etptPE.getPropertyType(),
                        null, materialTypeCache, cacheOrNull));
            }
        }
        if (entityType != null)
        {
            result.setEntityType(entityType);
        } else
        {
            result.setEntityType(translate(etptPE.getEntityType(), materialTypeCache, cacheOrNull));
        }
        result.setModificationDate(etptPE.getModificationDate());
        result.setManagedInternally(etptPE.isManagedInternally());
        result.setMandatory(etptPE.isMandatory());
        result.setOrdinal(etptPE.getOrdinal());
        result.setSection(etptPE.getSection());
        result.setDynamic(etptPE.isDynamic());
        boolean managed = etptPE.isManaged();
        result.setManaged(managed);
        boolean shownInEditView = etptPE.isShownInEditView();
        result.setShownInEditView(shownInEditView);
        Script script = ScriptTranslator.translate(etptPE.getScript());
        if (script != null && managed && shownInEditView)
        {
            result.setShowRawValue(etptPE.getShowRawValue());
        }
        result.setScript(script);
        return result;
    }

    protected final List<ETPT> translate(final Set<ETPTPE> sampleTypePropertyTypes,
            final PropertyType propertyType, Map<MaterialTypePE, MaterialType> materialTypeCache, 
            Map<PropertyTypePE, PropertyType> cacheOrNull)
    {
        if (HibernateUtils.isInitialized(sampleTypePropertyTypes) == false)
        {
            return DtoConverters.createUnmodifiableEmptyList();
        }
        final List<ETPT> result = new ArrayList<ETPT>();
        for (final ETPTPE sampleTypePropertyType : sampleTypePropertyTypes)
        {
            result.add(translate(sampleTypePropertyType, null, propertyType, materialTypeCache, cacheOrNull));
        }
        Collections.sort(result);
        return result;
    }

    abstract ETPT create();

    abstract ET translate(EntityTypePE entityTypePE, Map<MaterialTypePE, MaterialType> materialTypeCache, 
            Map<PropertyTypePE, PropertyType> cacheOrNull);

}
