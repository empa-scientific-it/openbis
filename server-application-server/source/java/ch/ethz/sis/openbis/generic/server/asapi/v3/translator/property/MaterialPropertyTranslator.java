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
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.Material;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectHolder;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.material.IMaterialTranslator;

/**
 * @author pkupczyk
 */
public abstract class MaterialPropertyTranslator extends
        AbstractCachingTranslator<Long, ObjectHolder<Map<String, Material>>, MaterialFetchOptions> implements IMaterialPropertyTranslator
{

    @Autowired
    private IMaterialTranslator materialTranslator;

    @Override
    protected ObjectHolder<Map<String, Material>> createObject(TranslationContext context, Long objectId, MaterialFetchOptions fetchOptions)
    {
        return new ObjectHolder<Map<String, Material>>();
    }

    @Override
    protected Object getObjectsRelations(TranslationContext context, Collection<Long> objectIds, MaterialFetchOptions fetchOptions)
    {
        List<MaterialPropertyRecord> records = loadMaterialProperties(objectIds);

        Collection<Long> propertyValues = new HashSet<Long>();

        for (MaterialPropertyRecord record : records)
        {
            propertyValues.add(record.propertyValue);
        }

        Map<Long, Material> materials = materialTranslator.translate(context, propertyValues, fetchOptions);
        Map<Long, Map<String, Material>> materialProperties = new HashMap<Long, Map<String, Material>>();

        for (MaterialPropertyRecord record : records)
        {
            Map<String, Material> properties = materialProperties.get(record.objectId);
            if (properties == null)
            {
                properties = new HashMap<String, Material>();
                materialProperties.put(record.objectId, properties);
            }
            properties.put(record.propertyCode, materials.get(record.propertyValue));
        }

        return materialProperties;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void updateObject(TranslationContext context, Long objectId, ObjectHolder<Map<String, Material>> result, Object relations,
            MaterialFetchOptions fetchOptions)
    {
        Map<Long, Map<String, Material>> materialProperties = (Map<Long, Map<String, Material>>) relations;
        Map<String, Material> objectProperties = materialProperties.get(objectId);

        if (objectProperties == null)
        {
            objectProperties = new HashMap<String, Material>();
        }

        result.setObject(objectProperties);
    }

    protected abstract List<MaterialPropertyRecord> loadMaterialProperties(Collection<Long> objectIds);

}
