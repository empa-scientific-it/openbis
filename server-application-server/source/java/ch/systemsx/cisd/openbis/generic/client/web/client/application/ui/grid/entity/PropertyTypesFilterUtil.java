/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

/**
 * @author Tomasz Pylak
 */
public class PropertyTypesFilterUtil
{
    /** returns only these properties which have connection to a given entity kind */
    public static List<PropertyType> filterPropertyTypesForEntityKind(
            List<PropertyType> propertyTypes, EntityKind entityKind)
    {
        List<PropertyType> result = new ArrayList<PropertyType>();
        for (final PropertyType st : propertyTypes)
        {
            if (getPropertyAssignments(st, entityKind).size() > 0)
            {
                result.add(st);
            }
        }
        return result;
    }

    /**
     * returns these property types in specified criteria which are assigned to any of the specified entity types
     */
    public static PropertyTypesCriteria filterPropertyTypesForEntityTypes(
            PropertyTypesCriteria propertyTypesCriteriaOrNull, EntityKind entityKind,
            Set<? extends BasicEntityType> shownEntityTypesOrNull)
    {
        if (propertyTypesCriteriaOrNull != null && shownEntityTypesOrNull != null)
        {
            List<PropertyType> propertyTypes = propertyTypesCriteriaOrNull.tryGetPropertyTypes();
            if (propertyTypes != null)
            {
                propertyTypes =
                        PropertyTypesFilterUtil.filterPropertyTypesForEntityTypes(propertyTypes,
                                entityKind, shownEntityTypesOrNull);
                // Note: a new object has to be created, this reference may be kept somewhere else
                PropertyTypesCriteria newCriteria = new PropertyTypesCriteria();
                newCriteria.copyPagingConfig(propertyTypesCriteriaOrNull);
                newCriteria.setPropertyTypes(propertyTypes);
                return newCriteria;
            }
        }
        return propertyTypesCriteriaOrNull;
    }

    /** returns these property types which are assigned to any of the specified entity types */
    private static List<PropertyType> filterPropertyTypesForEntityTypes(
            List<PropertyType> propertyTypes, EntityKind entityKind,
            Set<? extends BasicEntityType> entityTypes)
    {
        Set<String> entityTypesCodes = extractCodes(entityTypes);
        Set<PropertyType> result = new HashSet<PropertyType>();
        for (final PropertyType propertyType : propertyTypes)
        {
            List<? extends EntityTypePropertyType<?>> assignments =
                    getPropertyAssignments(propertyType, entityKind);
            for (EntityTypePropertyType<?> assignment : assignments)
            {
                // TODO 2009-08-27, Tomasz Pylak: use entityTypes equality when sample types
                // obtained from sample lister will be filled with database instance as well.
                if (entityTypesCodes.contains(assignment.getEntityType().getCode()))
                {
                    result.add(propertyType);
                }
            }
        }
        return new ArrayList<PropertyType>(result);
    }

    private static Set<String> extractCodes(Set<? extends BasicEntityType> entityTypes)
    {
        Set<String> codes = new HashSet<String>();
        Iterator<? extends BasicEntityType> iterator = entityTypes.iterator();
        while (iterator.hasNext())
        {
            codes.add(iterator.next().getCode());
        }
        return codes;
    }

    private static List<? extends EntityTypePropertyType<?>> getPropertyAssignments(
            PropertyType propertyType, EntityKind entityKind)
    {
        switch (entityKind)
        {
            case DATA_SET:
                return propertyType.getDataSetTypePropertyTypes();
            case EXPERIMENT:
                return propertyType.getExperimentTypePropertyTypes();
            case MATERIAL:
                return propertyType.getMaterialTypePropertyTypes();
            case SAMPLE:
                return propertyType.getSampleTypePropertyTypes();
        }
        throw new IllegalStateException("unknown enumerator " + entityKind);
    }

    /** returns property types which are assigned to at least one sample type */
    public static List<PropertyType> filterSamplePropertyTypes(List<PropertyType> propertyTypes)
    {
        return filterPropertyTypesForEntityKind(propertyTypes, EntityKind.SAMPLE);
    }

    /** returns property types which are assigned to at least one experiment type */
    public static List<PropertyType> filterExperimentPropertyTypes(List<PropertyType> propertyTypes)
    {
        return filterPropertyTypesForEntityKind(propertyTypes, EntityKind.EXPERIMENT);
    }

    /** returns property types which are assigned to at least one dataset type */
    public static List<PropertyType> filterDataSetPropertyTypes(List<PropertyType> propertyTypes)
    {
        return filterPropertyTypesForEntityKind(propertyTypes, EntityKind.DATA_SET);
    }

    /** returns property types which are assigned to at least one material type */
    public static List<PropertyType> filterMaterialPropertyTypes(List<PropertyType> propertyTypes)
    {
        return filterPropertyTypesForEntityKind(propertyTypes, EntityKind.MATERIAL);
    }

    public static List<PropertyType> extractPropertyTypes(EntityType selectedType)
    {
        List<? extends EntityTypePropertyType<?>> entityTypePropertyTypes =
                selectedType.getAssignedPropertyTypes();
        List<PropertyType> propertyTypes = new ArrayList<PropertyType>();
        for (EntityTypePropertyType<?> etpt : entityTypePropertyTypes)
        {
            propertyTypes.add(etpt.getPropertyType());
        }
        return propertyTypes;
    }
}
