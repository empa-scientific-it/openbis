/*
 * Copyright ETH 2014 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.property;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertiesHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.sample.IMapSampleByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatchProcessor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.progress.EntityProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.progress.UpdatePropertyProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.sample.ISampleAuthorizationValidator;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.EntityPropertiesConverter;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyWithSampleDataTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityInformationWithPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IEntityInformationProvider;
import ch.systemsx.cisd.openbis.generic.shared.util.EntityHelper;

/**
 * @author pkupczyk
 */
@Component
public class UpdateEntityPropertyExecutor implements IUpdateEntityPropertyExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private IEntityInformationProvider entityInformationProvider;

    @Autowired
    private IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory;
    
    @Autowired
    private IMapSampleByIdExecutor mapSampleByIdExecutor;

    @Autowired
    private ISampleAuthorizationValidator sampleAuthorizationValidator;

    @SuppressWarnings("unused")
    private UpdateEntityPropertyExecutor()
    {
    }

    public UpdateEntityPropertyExecutor(IDAOFactory daoFactory, IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory)
    {
        this.daoFactory = daoFactory;
        this.managedPropertyEvaluatorFactory = managedPropertyEvaluatorFactory;
    }

    @Override
    public void update(final IOperationContext context,
            final MapBatch<? extends IPropertiesHolder, ? extends IEntityInformationWithPropertiesHolder> holderToEntityMap)
    {
        final MapBatch<IEntityInformationWithPropertiesHolder, Map<String, Serializable>> entityToPropertiesMap =
                getEntityToPropertiesMap(holderToEntityMap);

        if (entityToPropertiesMap == null || entityToPropertiesMap.isEmpty())
        {
            return;
        }

        MapBatch<IEntityInformationWithPropertiesHolder, Map<String, ISampleId>> entityToSamplePropertiesMap =
                extractAndRemoveSampleProperties(holderToEntityMap, entityToPropertiesMap);
        updateSampleProps(context, entityToSamplePropertiesMap);

        final Map<EntityKind, EntityPropertiesConverter> converters = new HashMap<EntityKind, EntityPropertiesConverter>();

        new MapBatchProcessor<IEntityInformationWithPropertiesHolder, Map<String, Serializable>>(context, entityToPropertiesMap)
            {
                @Override
                public void process(IEntityInformationWithPropertiesHolder propertiesHolder, Map<String, Serializable> properties)
                {
                    EntityKind entityKind = propertiesHolder.getEntityType().getEntityKind();

                    if (converters.get(entityKind) == null)
                    {
                        EntityPropertiesConverter converter =
                                new EntityPropertiesConverter(entityKind, daoFactory, entityInformationProvider, managedPropertyEvaluatorFactory);
                        converters.put(entityKind, converter);
                    }

                    update(context, propertiesHolder, properties, converters.get(entityKind));
                }

                @Override
                public IProgress createProgress(IEntityInformationWithPropertiesHolder propertiesHolder,
                        Map<String, Serializable> properties,
                        int objectIndex, int totalObjectCount)
                {
                    return new UpdatePropertyProgress(propertiesHolder, properties, objectIndex, totalObjectCount);
                }
            };
    }

    private void updateSampleProps(IOperationContext context,
            MapBatch<IEntityInformationWithPropertiesHolder, Map<String, ISampleId>> entityToSamplePropertiesMap)
    {
        if (entityToSamplePropertiesMap != null && entityToSamplePropertiesMap.isEmpty() == false)
        {
            Map<ISampleId, SamplePE> samplesById = getSamples(context, entityToSamplePropertiesMap);
            new MapBatchProcessor<IEntityInformationWithPropertiesHolder, Map<String, ISampleId>>(context, entityToSamplePropertiesMap)
                {
                    @Override
                    public void process(IEntityInformationWithPropertiesHolder entity, Map<String, ISampleId> properties)
                    {
                        Map<String, EntityPropertyPE> existingPropertiesByCode = getExistingPropertiesByCode(entity);
                        Map<String, EntityTypePropertyTypePE> entityTypePropertyTypeByPropertyType =
                                getEntityTypePropertyTypeByPropertyType(entity);
                        updateProperties(context, entity, properties, samplesById, existingPropertiesByCode,
                                entityTypePropertyTypeByPropertyType);
                    }

                    @Override
                    public IProgress createProgress(IEntityInformationWithPropertiesHolder key, Map<String, ISampleId> value, int objectIndex,
                            int totalObjectCount)
                    {
                        return new EntityProgress("update properties of type sample", key, objectIndex, totalObjectCount);
                    }
                };
        }
    }

    private void updateProperties(final IOperationContext context, IEntityInformationWithPropertiesHolder entity,
            Map<String, ISampleId> properties, Map<ISampleId, SamplePE> samplesById,
            Map<String, EntityPropertyPE> existingPropertiesByCode,
            Map<String, EntityTypePropertyTypePE> entityTypePropertyTypeByPropertyType)
    {
        for (Entry<String, ISampleId> property : properties.entrySet())
        {
            String propertyCode = property.getKey();
            EntityTypePropertyTypePE etpt = entityTypePropertyTypeByPropertyType.get(propertyCode);
            if (etpt == null)
            {
                throw new UserFailureException("Not a property of data type SAMPLE: " + propertyCode);
            }
            EntityPropertyPE existingProperty = existingPropertiesByCode.get(propertyCode);
            ISampleId sampleId = property.getValue();
            SamplePE sample = validateAndGetSample(samplesById, sampleId, etpt, propertyCode);
            if (existingProperty != null)
            {
                if (sample == null)
                {
                    if (existingProperty.getEntityTypePropertyType().isMandatory() == false)
                    {
                        entity.removeProperty(existingProperty);
                    } else
                    {
                        throw new UserFailureException("Property " + propertyCode + " of entity type "
                                + etpt.getEntityType().getCode()
                                + " can not be deleted because it is mandatory.");
                    }
                } else if (existingProperty instanceof EntityPropertyWithSampleDataTypePE)
                {
                    ((EntityPropertyWithSampleDataTypePE) existingProperty).setSampleValue(sample);
                }
            } else if (sample != null)
            {
                EntityPropertyPE entityProperty = EntityPropertyPE.createEntityProperty(entity.getEntityKind());
                if (entityProperty instanceof EntityPropertyWithSampleDataTypePE)
                {
                    PersonPE registrator = context.getSession().tryGetPerson();
                    entityProperty.setRegistrator(registrator);
                    entityProperty.setAuthor(registrator);
                    entityProperty.setEntityTypePropertyType(etpt);
                    ((EntityPropertyWithSampleDataTypePE) entityProperty).setSampleValue(sample);
                    entity.addProperty(entityProperty);
                }
            }
        }
    }

    private SamplePE validateAndGetSample(Map<ISampleId, SamplePE> samplesById, ISampleId sampleId, EntityTypePropertyTypePE etpt,
            String propertyCode)
    {
        if (sampleId == null)
        {
            return null;
        }
        SamplePE sample = samplesById.get(sampleId);
        if (sample == null)
        {
            throw new UserFailureException("Unknown sample: " + sampleId);
        }
        SampleTypePE sampleType = etpt.getPropertyType().getSampleType();
        if (sampleType != null && sampleType.getCode().equals(sample.getSampleType().getCode()) == false)
        {
            throw new UserFailureException("Property " + propertyCode + " is not a sample of type "
                    + sampleType.getCode() + " but of type " + sample.getSampleType().getCode());
        }
        return sample;
    }

    private Map<String, EntityPropertyPE> getExistingPropertiesByCode(IEntityInformationWithPropertiesHolder entity)
    {
        Map<String, EntityPropertyPE> existingPropertiesByCode = new HashMap<>();
        for (EntityPropertyPE entityProperty : entity.getProperties())
        {
            PropertyTypePE propertyType = entityProperty.getEntityTypePropertyType().getPropertyType();
            existingPropertiesByCode.put(propertyType.getCode(), entityProperty);
        }
        return existingPropertiesByCode;
    }

    private Map<String, EntityTypePropertyTypePE> getEntityTypePropertyTypeByPropertyType(IEntityInformationWithPropertiesHolder entity)
    {
        Map<String, EntityTypePropertyTypePE> entityTypePropertyTypeByPropertyType = new HashMap<>();
        for (EntityTypePropertyTypePE etpt : entity.getEntityType().getEntityTypePropertyTypes())
        {
            PropertyTypePE propertyType = etpt.getPropertyType();
            if (DataTypeCode.SAMPLE.equals(propertyType.getType().getCode()))
            {
                entityTypePropertyTypeByPropertyType.put(propertyType.getCode(), etpt);
            }
        }
        return entityTypePropertyTypeByPropertyType;
    }

    private Map<ISampleId, SamplePE> getSamples(final IOperationContext context,
            MapBatch<IEntityInformationWithPropertiesHolder, Map<String, ISampleId>> entityToSamplePropertiesMap)
    {
        Set<ISampleId> sampleIds = getSampleIds(entityToSamplePropertiesMap);
        Map<ISampleId, SamplePE> samplesById = mapSampleByIdExecutor.map(context, sampleIds);
        Set<Long> validateSampleTechIds = sampleAuthorizationValidator.validate(context.getSession().tryGetPerson(),
                samplesById.values().stream().map(SamplePE::getId).collect(Collectors.toSet()));
        return samplesById.entrySet().stream().filter(e -> validateSampleTechIds.contains(e.getValue().getId()))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    private Set<ISampleId> getSampleIds(MapBatch<IEntityInformationWithPropertiesHolder, Map<String, ISampleId>> entityToSamplePropertiesMap)
    {
        Set<ISampleId> sampleIds = new HashSet<>();
        for (Map<String, ISampleId> map : entityToSamplePropertiesMap.getObjects().values())
        {
            Collection<ISampleId> values = map.values();
            for (ISampleId sample : values)
            {
                if (sample != null)
                {
                    sampleIds.add(sample);
                }
            }
        }
        return sampleIds;
    }

    private MapBatch<IEntityInformationWithPropertiesHolder, Map<String, ISampleId>> extractAndRemoveSampleProperties(
            MapBatch<? extends IPropertiesHolder, ? extends IEntityInformationWithPropertiesHolder> holderToEntityMap,
            MapBatch<IEntityInformationWithPropertiesHolder, Map<String, Serializable>> entityToPropertiesMap)
    {
        Map<IEntityInformationWithPropertiesHolder, Map<String, ISampleId>> map = new HashMap<>();
        Map<IEntityInformationWithPropertiesHolder, Map<String, Serializable>> objects = entityToPropertiesMap.getObjects();
        for (Entry<IEntityInformationWithPropertiesHolder, Map<String, Serializable>> entry : objects.entrySet())
        {
            IEntityInformationWithPropertiesHolder entity = entry.getKey();
            Collection<? extends EntityTypePropertyTypePE> etpts = entity.getEntityType().getEntityTypePropertyTypes();

            for (EntityTypePropertyTypePE etpt : etpts)
            {
                if (etpt.getPropertyType().getType().getCode() == DataTypeCode.SAMPLE)
                {
                    String code = etpt.getPropertyType().getCode();
                    Map<String, Serializable> properties = entry.getValue();
                    if (properties.containsKey(code))
                    {
                        String sample = (String) properties.remove(code);
                        ISampleId sampleId = null;
                        if (sample != null)
                        {
                            sampleId = sample.startsWith("/") ? new SampleIdentifier(sample) : new SamplePermId(sample);
                        }
                        Map<String, ISampleId> props = map.get(entity);
                        if (props == null)
                        {
                            props = new HashMap<>();
                            map.put(entity, props);
                        }
                        props.put(code, sampleId);
                    }
                }
            }
        }
        return new MapBatch<>(holderToEntityMap.getBatchIndex(), holderToEntityMap.getFromObjectIndex(),
                holderToEntityMap.getToObjectIndex(), map, holderToEntityMap.getTotalObjectCount());
    }

    private MapBatch<IEntityInformationWithPropertiesHolder, Map<String, Serializable>> getEntityToPropertiesMap(
            final MapBatch<? extends IPropertiesHolder, ? extends IEntityInformationWithPropertiesHolder> holderToEntityMap)
    {
        if (holderToEntityMap == null || holderToEntityMap.isEmpty())
        {
            return null;
        }

        Map<IEntityInformationWithPropertiesHolder, Map<String, Serializable>> entityToPropertiesMap =
                new HashMap<>();

        for (Map.Entry<? extends IPropertiesHolder, ? extends IEntityInformationWithPropertiesHolder> entry : holderToEntityMap.getObjects()
                .entrySet())
        {
            IPropertiesHolder holder = entry.getKey();
            IEntityInformationWithPropertiesHolder entity = entry.getValue();

            if (holder.getProperties() != null && false == holder.getProperties().isEmpty())
            {
                entityToPropertiesMap.put(entity, holder.getProperties());
            }
        }

        if (entityToPropertiesMap.isEmpty())
        {
            return null;
        }

        return new MapBatch<IEntityInformationWithPropertiesHolder, Map<String, Serializable>>(holderToEntityMap.getBatchIndex(),
                holderToEntityMap.getFromObjectIndex(), holderToEntityMap.getToObjectIndex(), entityToPropertiesMap,
                holderToEntityMap.getTotalObjectCount());
    }

    private void update(IOperationContext context, IEntityPropertiesHolder propertiesHolder, Map<String, Serializable> properties,
            EntityPropertiesConverter converter)
    {
        List<IEntityProperty> entityProperties = new LinkedList<IEntityProperty>();
        for (Map.Entry<String, Serializable> entry : properties.entrySet())
        {
            entityProperties.add(EntityHelper.createNewProperty(entry.getKey(), entry.getValue()));
        }

        Set<? extends EntityPropertyPE> existingProperties = propertiesHolder.getProperties();
        Map<String, Object> existingPropertyValuesByCode = new HashMap<String, Object>();

        for (EntityPropertyPE existingProperty : existingProperties)
        {
            String propertyCode =
                    existingProperty.getEntityTypePropertyType().getPropertyType().getCode();
            existingPropertyValuesByCode.put(propertyCode, getValue(existingProperty));
        }

        Set<? extends EntityPropertyPE> convertedProperties =
                convertProperties(context, propertiesHolder.getEntityType(), existingProperties, entityProperties, converter);

        if (isEquals(existingPropertyValuesByCode, convertedProperties) == false)
        {
            propertiesHolder.setProperties(convertedProperties);
        }
    }

    private <T extends EntityPropertyPE> Set<T> convertProperties(IOperationContext context, final EntityTypePE type,
            final Set<T> existingProperties, List<IEntityProperty> properties, EntityPropertiesConverter converter)
    {
        Set<String> propertiesToUpdate = new HashSet<String>();
        if (properties != null)
        {
            for (IEntityProperty property : properties)
            {
                propertiesToUpdate.add(property.getPropertyType().getCode());
            }
        }
        return converter.updateProperties(existingProperties, type, properties,
                context.getSession().tryGetPerson(), propertiesToUpdate);
    }

    private static Object getValue(EntityPropertyPE property)
    {
        String value = property.getValue();
        if (value != null)
        {
            return value;
        }
        MaterialPE materialValue = property.getMaterialValue();
        if (materialValue != null)
        {
            return materialValue;
        }
        return property.getVocabularyTerm();
    }

    private static boolean isEquals(Map<String, Object> existingPropertyValuesByCode,
            Set<? extends EntityPropertyPE> properties)
    {
        for (EntityPropertyPE property : properties)
        {
            Object existingValue =
                    existingPropertyValuesByCode.remove(property.getEntityTypePropertyType()
                            .getPropertyType().getCode());
            if (existingValue == null || existingValue.equals(getValue(property)) == false)
            {
                return false;
            }
        }
        return existingPropertyValuesByCode.isEmpty();
    }

}
