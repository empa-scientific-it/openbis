/*
 * Copyright ETH 2009 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.shared.util;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.*;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * @author Izabela Adamczyk
 */
public class EntityHelper
{
    /**
     * Returns <code>true</code> if both entities are <code>null</code> or have the same id (which
     * could be <code>null</code>).
     */
    public static <T extends IIdHolder> boolean equalEntities(T entity1OrNull, T entity2OrNull)
    {
        if (entity1OrNull == null || entity2OrNull == null)
        {
            return entity1OrNull == entity2OrNull;
        }
        if (entity1OrNull.getClass().equals(entity2OrNull.getClass()) == false)
        {
            return false;
        }
        Long id1 = HibernateUtils.getId(entity1OrNull);
        Long id2 = HibernateUtils.getId(entity2OrNull);
        return id1 == null ? id1 == id2 : id1.equals(id2);
    }

    /**
     * Creates {@link EntityType} appropriate for given {@link EntityKind}.
     */
    public static EntityType createEntityType(EntityKind kind, String code)
    {
        EntityType type = null;
        switch (kind)
        {
            case DATA_SET:
                type = new DataSetType();
                break;
            case EXPERIMENT:
                type = new ExperimentType();
                break;
            case MATERIAL:
                type = new MaterialType();
                break;
            case SAMPLE:
                type = new SampleType();
                break;
            default:
                throw new Error("Unknown kind '" + kind + "'");
        }
        type.setCode(code);
        return type;
    }

    /**
     * @return finds and returns an {@link IEntityProperty} for a specified code. Returns
     *         <code>null</code> if no matching property is found.
     */
    public static IEntityProperty tryFindProperty(Iterable<IEntityProperty> properties,
            final String propertyCode)
    {
        for (final IEntityProperty property : properties)
        {
            final PropertyType propertyType = property.getPropertyType();
            if (propertyType.getCode().equalsIgnoreCase(propertyCode))
            {
                return property;
            }
        }
        return null;
    }

    /**
     * does the same as {@link #tryFindProperty(Iterable, String)} but with arrays.
     */
    public static IEntityProperty tryFindProperty(IEntityProperty[] properties,
            final String propertyCode)
    {
        for (final IEntityProperty property : properties)
        {
            final PropertyType propertyType = property.getPropertyType();
            if (propertyType.getCode().equalsIgnoreCase(propertyCode))
            {
                return property;
            }
        }
        return null;
    }

    public static boolean removeProperty(List<NewProperty> properties, String propertyCode)
    {
        NewProperty property = tryFindProperty(properties, propertyCode);
        if (property != null)
        {
            properties.remove(property);
            return true;
        } else
        {
            return false;
        }
    }

    public static NewProperty tryFindProperty(List<NewProperty> properties, String propertyCode)
    {
        for (final NewProperty property : properties)
        {
            if (property.getPropertyCode().equalsIgnoreCase(propertyCode))
            {
                return property;
            }
        }
        return null;
    }

    public static String tryFindPropertyValue(
            ch.systemsx.cisd.openbis.generic.shared.dto.IEntityPropertiesHolder holder,
            String propertyCode)
    {
        EntityPropertyPE property = null;

        if (holder != null && holder.getProperties() != null)
        {
            property = EntityHelper.tryFindProperty(holder.getProperties(), propertyCode);
        }

        return (property != null) ? property.tryGetUntypedValue() : null;
    }

    private static EntityPropertyPE tryFindProperty(Set<? extends EntityPropertyPE> properties,
            String propertyCode)
    {

        for (final EntityPropertyPE property : properties)
        {
            final PropertyTypePE propertyType =
                    property.getEntityTypePropertyType().getPropertyType();
            if (propertyType.getCode().equalsIgnoreCase(propertyCode))
            {
                return property;
            }
        }
        return null;
    }

    public static String tryFindPropertyValue(IEntityPropertiesHolder holder, String propertyCode)
    {
        IEntityProperty property = null;

        if (holder != null && holder.getProperties() != null)
        {
            property = EntityHelper.tryFindProperty(holder.getProperties(), propertyCode);
        }

        return (property != null) ? property.tryGetOriginalValue() : null;
    }

    public static void updateSampleProperties(ICommonServer server, String sessionToken,
            Sample sample, Map<String, String> properties)
    {
        List<IEntityProperty> props = translatePropertiesMapToList(properties);
        ExperimentIdentifier experimentIdentifier =
                ExperimentIdentifierFactory.tryGetExperimentIdentifier(sample);
        ProjectIdentifier projectIdentifier =
                ProjectIdentifierFactory.tryGetProjectIdentifier(sample);
        SampleIdentifier sampleIdentifier = SampleIdentifierFactory.parse(sample.getIdentifier());
        Sample container = sample.getContainer();
        String containerIdentifier = container == null ? null : container.getIdentifier();
        SampleUpdatesDTO updates =
                new SampleUpdatesDTO(new TechId(sample), props, experimentIdentifier,
                        projectIdentifier,
                        Collections.<NewAttachment> emptySet(), sample.getVersion(),
                        sampleIdentifier, containerIdentifier, null);
        server.updateSample(sessionToken, updates);
    }

    public static void updateSampleProperties(ICommonServer server, String sessionToken, TechId id,
            Map<String, String> properties)
    {
        Sample sample = server.getSampleInfo(sessionToken, id).getParent();
        updateSampleProperties(server, sessionToken, sample, properties);
    }

    public static List<IEntityProperty> translatePropertiesMapToList(Map<String, String> properties)
    {
        List<IEntityProperty> props = new ArrayList<IEntityProperty>();
        for (Entry<String, String> entry : properties.entrySet())
        {
            props.add(createNewProperty(entry.getKey(), entry.getValue()));
        }
        return props;
    }

    /**
     * Creates a property with specified code and value. An already existing property with same code
     * will be removed.
     */
    public static void createOrUpdateProperty(IEntityPropertiesHolder holder, String propertyCode,
            String propertyValue)
    {
        IEntityProperty entityProperty =
                EntityHelper.tryFindProperty(holder.getProperties(), propertyCode);
        List<IEntityProperty> properties = holder.getProperties();
        if (entityProperty != null && entityProperty.getPropertyType().isMultiValue())
        {
            properties.removeIf(
                    prop -> prop.getPropertyType().getCode().equalsIgnoreCase(propertyCode));
            if (propertyValue != null && propertyValue.startsWith("["))
            {
                String[] values = propertyValue.substring(1, propertyValue.length() - 1).split(",");
                for (String value : values)
                {
                    IEntityProperty newProperty = createNewProperty(propertyCode, value.trim());
                    properties.add(newProperty);
                }
            } else
            {
                IEntityProperty newProperty = createNewProperty(propertyCode, propertyValue);
                properties.add(newProperty);
            }
        } else
        {
            IEntityProperty newProperty = createNewProperty(propertyCode, propertyValue);
            for (int i = 0; i < properties.size(); i++)
            {
                IEntityProperty property = properties.get(i);
                PropertyType propertyType = property.getPropertyType();
                if (propertyType.getCode().equalsIgnoreCase(propertyCode))
                {
                    properties.set(i, newProperty);
                    return;
                }
            }
            properties.add(newProperty);
        }

    }

    public static IEntityProperty createNewProperty(String propertyCode, Serializable propertyValue)
    {
        IEntityProperty property = createNewProperty(propertyCode);
        property.setValue(propertyValue);
        return property;
    }

    public static Map<String, String> convertToStringMap(List<IEntityProperty> properties)
    {
        Map<String, String> map = new HashMap<String, String>();
        if (properties != null)
        {
            for (IEntityProperty prop : properties)
            {
                map.put(prop.getPropertyType().getCode(), prop.tryGetAsString());
            }
        }

        return map;
    }

    private static IEntityProperty createNewProperty(String propertyCode)
    {
        IEntityProperty property = new EntityProperty();
        PropertyType propertyType = new PropertyType();
        propertyType.setCode(propertyCode);
        property.setPropertyType(propertyType);
        return property;
    }
}
