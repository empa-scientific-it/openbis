/*
 * Copyright 2015 ETH Zuerich, CISD
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyFetchOptions;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectHolder;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.sample.ISampleAuthorizationValidator;

/**
 * @author pkupczyk
 */
public abstract class PropertyTranslator extends AbstractCachingTranslator<Long, ObjectHolder<Map<String, String>>, PropertyFetchOptions>
        implements IPropertyTranslator
{
    @Autowired
    private ISampleAuthorizationValidator sampleAuthorizationValidator;

    @Override
    protected ObjectHolder<Map<String, String>> createObject(TranslationContext context, Long objectId, PropertyFetchOptions fetchOptions)
    {
        return new ObjectHolder<Map<String, String>>();
    }

    @Override
    protected Object getObjectsRelations(TranslationContext context, Collection<Long> objectIds, PropertyFetchOptions fetchOptions)
    {
        List<PropertyRecord> records = loadProperties(objectIds);
        Set<Long> visibaleSamples = sampleAuthorizationValidator.validate(context.getSession().tryGetPerson(),
                records.stream().filter(r -> r.sample_id != null).map(r -> r.sample_id).collect(Collectors.toSet()));

        Map<Long, Map<String, String>> properties = new HashMap<Long, Map<String, String>>();
        for (PropertyRecord record : records)
        {
            Map<String, String> objectProperties = properties.get(record.objectId);

            if (objectProperties == null)
            {
                objectProperties = new HashMap<String, String>();
                properties.put(record.objectId, objectProperties);
            }

            if (record.propertyValue != null)
            {
                objectProperties.put(record.propertyCode, record.propertyValue);
            } else if (record.materialPropertyValueCode != null)
            {
                objectProperties.put(record.propertyCode, record.materialPropertyValueCode + " (" + record.materialPropertyValueTypeCode
                        + ")");
            } else if (record.vocabularyPropertyValue != null)
            {
                objectProperties.put(record.propertyCode, record.vocabularyPropertyValue);
            } else if (record.sample_perm_id != null)
            {
                if (visibaleSamples.contains(record.sample_id))
                {
                    objectProperties.put(record.propertyCode, record.sample_perm_id);
                }
            } else
            {
                // SAMPLE property with deleted sample. Thus, nothing is put to objectProperties
            }
        }

        return properties;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void updateObject(TranslationContext context, Long objectId, ObjectHolder<Map<String, String>> result, Object relations,
            PropertyFetchOptions fetchOptions)
    {
        Map<Long, Map<String, String>> properties = (Map<Long, Map<String, String>>) relations;
        Map<String, String> objectProperties = properties.get(objectId);

        if (objectProperties == null)
        {
            objectProperties = new HashMap<String, String>();
        }

        result.setObject(objectProperties);
    }

    protected abstract List<PropertyRecord> loadProperties(Collection<Long> entityIds);

}
