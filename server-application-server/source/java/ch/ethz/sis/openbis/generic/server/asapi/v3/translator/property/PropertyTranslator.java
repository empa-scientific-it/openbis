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
package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.property;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyFetchOptions;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectHolder;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.sample.ISampleAuthorizationValidator;

/**
 * @author pkupczyk
 */
public abstract class PropertyTranslator extends
        AbstractCachingTranslator<Long, ObjectHolder<Map<String, Serializable>>, PropertyFetchOptions>
        implements IPropertyTranslator
{
    @Autowired
    private ISampleAuthorizationValidator sampleAuthorizationValidator;

    @Override
    protected ObjectHolder<Map<String, Serializable>> createObject(TranslationContext context,
            Long objectId, PropertyFetchOptions fetchOptions)
    {
        return new ObjectHolder<Map<String, Serializable>>();
    }

    @Override
    protected Object getObjectsRelations(TranslationContext context, Collection<Long> objectIds,
            PropertyFetchOptions fetchOptions)
    {
        List<PropertyRecord> records = loadProperties(objectIds);
        Set<Long> visibaleSamples =
                sampleAuthorizationValidator.validate(context.getSession().tryGetPerson(),
                        records.stream().filter(r -> r.sample_id != null).map(r -> r.sample_id)
                                .collect(Collectors.toSet()));

        Map<Long, Map<String, Serializable>> properties = new HashMap<Long, Map<String, Serializable>>();
        for (PropertyRecord record : records)
        {
            Map<String, Serializable> objectProperties = properties.get(record.objectId);

            if (objectProperties == null)
            {
                objectProperties = new HashMap<String, Serializable>();
                properties.put(record.objectId, objectProperties);
            }

            if (record.propertyValue != null)
            {
                objectProperties.put(record.propertyCode, record.propertyValue);
            } else if (record.materialPropertyValueCode != null)
            {
                objectProperties.put(record.propertyCode,
                        record.materialPropertyValueCode + " (" + record.materialPropertyValueTypeCode
                                + ")");
            } else if (record.vocabularyPropertyValue != null)
            {
                if(objectProperties.containsKey(record.propertyCode)) {
                    Serializable current = objectProperties.get(record.propertyCode);
                    Serializable newValue = composeMultiValueProperty(current, record.vocabularyPropertyValue);
                    objectProperties.put(record.propertyCode, newValue);
                } else {
                    objectProperties.put(record.propertyCode, record.vocabularyPropertyValue);
                }
            } else if (record.sample_perm_id != null)
            {
                if (visibaleSamples.contains(record.sample_id))
                {
                    if(objectProperties.containsKey(record.propertyCode)) {
                        Serializable current = objectProperties.get(record.propertyCode);
                        Serializable newValue = composeMultiValueProperty(current, record.sample_perm_id);
                        objectProperties.put(record.propertyCode, newValue);
                    } else
                    {
                        objectProperties.put(record.propertyCode, record.sample_perm_id);
                    }
                }
            } else if (record.integerArrayPropertyValue != null)
            {
                objectProperties.put(record.propertyCode,
                        convertArrayToString(record.integerArrayPropertyValue));
            } else if (record.realArrayPropertyValue != null)
            {
                objectProperties.put(record.propertyCode,
                        convertArrayToString(record.realArrayPropertyValue));
            } else if (record.stringArrayPropertyValue != null)
            {
                objectProperties.put(record.propertyCode,
                        convertArrayToString(record.stringArrayPropertyValue));
            } else if (record.timestampArrayPropertyValue != null)
            {
                objectProperties.put(record.propertyCode,
                        convertArrayToString(record.timestampArrayPropertyValue));
            } else if (record.jsonPropertyValue != null)
            {
                objectProperties.put(record.propertyCode, record.jsonPropertyValue);
            } else
            {
                // SAMPLE property with deleted sample. Thus, nothing is put to objectProperties
            }
        }

        return properties;
    }

    private Serializable composeMultiValueProperty(Serializable current, Serializable newValue) {
        Serializable[] result;
        if(current.getClass().isArray()) {
            Serializable[] values = (Serializable[]) current;
            result = new Serializable[values.length + 1];
            System.arraycopy(values, 0, result, 0, values.length);
            result[values.length] = newValue;
        } else {
            result = new Serializable[] {current, newValue};
        }
        return result;
    }

    private String convertArrayToString(String[] array)
    {
        return Stream.of(array)
                .reduce((x, y) -> x + ", " + y)
                .get();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void updateObject(TranslationContext context, Long objectId,
            ObjectHolder<Map<String, Serializable>> result, Object relations,
            PropertyFetchOptions fetchOptions)
    {
        Map<Long, Map<String, Serializable>> properties = (Map<Long, Map<String, Serializable>>) relations;
        Map<String, Serializable> objectProperties = properties.get(objectId);

        if (objectProperties == null)
        {
            objectProperties = new HashMap<String, Serializable>();
        }

        result.setObject(objectProperties);
    }

    protected abstract List<PropertyRecord> loadProperties(Collection<Long> entityIds);

}
