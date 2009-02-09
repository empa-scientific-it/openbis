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

package ch.systemsx.cisd.openbis.generic.client.web.server.translator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * Encapsulates the translation logic of ETPEs.
 * @author Izabela Adamczyk
 */
abstract public class AbstractEntityTypePropertyTypeTranslator<ET extends EntityType, ETPT extends EntityTypePropertyType<ET>, ETPTPE extends EntityTypePropertyTypePE>
{

    public ETPT translate(ETPTPE entityTypePropertyType)
    {
        return translate(entityTypePropertyType, null, null);
    }

    protected final List<ETPT> translate(final Set<ETPTPE> sampleTypePropertyTypes,
            final ET sampleType)
    {
        if (HibernateUtils.isInitialized(sampleTypePropertyTypes) == false)
        {
            return DtoConverters.createUnmodifiableEmptyList();
        }
        final List<ETPT> result = new ArrayList<ETPT>();
        for (final ETPTPE sampleTypePropertyType : sampleTypePropertyTypes)
        {
            result.add(translate(sampleTypePropertyType, sampleType, null));
        }
        Collections.sort(result);
        return result;
    }

    private final ETPT translate(final ETPTPE etptPE, final ET entityType,
            final PropertyType propertyType)
    {
        ETPT result = create();
        if (propertyType != null)
        {
            result.setPropertyType(propertyType);
        } else
        {
            result.setPropertyType(PropertyTypeTranslator.translate(etptPE.getPropertyType()));
        }
        if (entityType != null)
        {
            result.setEntityType(entityType);
        } else
        {
            result.setEntityType(translate(etptPE.getEntityType()));
        }
        result.setManagedInternally(etptPE.isManagedInternally());
        result.setMandatory(etptPE.isMandatory());
        setSpecificFields(result, etptPE);
        return result;
    }

    protected final List<ETPT> translate(final Set<ETPTPE> sampleTypePropertyTypes,
            final PropertyType propertyType)
    {
        if (HibernateUtils.isInitialized(sampleTypePropertyTypes) == false)
        {
            return DtoConverters.createUnmodifiableEmptyList();
        }
        final List<ETPT> result = new ArrayList<ETPT>();
        for (final ETPTPE sampleTypePropertyType : sampleTypePropertyTypes)
        {
            result.add(translate(sampleTypePropertyType, null, propertyType));
        }
        Collections.sort(result);
        return result;
    }

    abstract ETPT create();

    abstract void setSpecificFields(ETPT result, final ETPTPE etptPE);

    abstract ET translate(EntityTypePE entityTypePE);

}
