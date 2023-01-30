/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

/**
 * Helper class for building properties sections for the parents of a sample.
 * 
 * @author Franz-Josef Elmer
 */
class ParentsPropertiesSectionBuilder
{
    private static final class Key
    {
        private final String code;

        private final DataTypeCode dataType;

        private final String value;

        Key(IEntityProperty property)
        {
            code = property.getPropertyType().getCode();
            dataType = property.getPropertyType().getDataType().getCode();
            value = property.tryGetAsString();
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj instanceof Key == false)
            {
                return false;
            }
            Key key = (Key) obj;
            return code.equals(key.code) && dataType.equals(key.dataType)
                    && value.equals(key.value);
        }

        @Override
        public int hashCode()
        {
            int hashCode = code.hashCode();
            hashCode = 37 * hashCode + dataType.hashCode();
            hashCode = 37 * hashCode + value.hashCode();
            return hashCode;
        }

    }

    private final List<Sample> samples = new ArrayList<Sample>();

    void addParent(Sample sample)
    {
        samples.add(sample);
    }

    Map<String, List<IEntityProperty>> getSections()
    {
        Map<String, List<IEntityProperty>> sections =
                new LinkedHashMap<String, List<IEntityProperty>>();
        Set<Key> commonKeys = new HashSet<ParentsPropertiesSectionBuilder.Key>();
        if (samples.size() > 1)
        {
            Map<Key, List<IEntityProperty>> keys = getCommonPropertiesGroups();
            List<IEntityProperty> commonProperties = new ArrayList<IEntityProperty>();
            for (Entry<Key, List<IEntityProperty>> entry : keys.entrySet())
            {
                Key key = entry.getKey();
                List<IEntityProperty> list = entry.getValue();
                if (list.size() == samples.size())
                {
                    commonProperties.add(list.get(0));
                    commonKeys.add(key);
                }
            }
            if (commonProperties.isEmpty() == false)
            {
                sections.put("Common Parent Properties:", commonProperties);
            }
        }
        Collections.sort(samples);
        for (Sample sample : samples)
        {
            List<IEntityProperty> properties = sample.getProperties();
            for (Iterator<IEntityProperty> iterator = properties.iterator(); iterator.hasNext();)
            {
                if (commonKeys.contains(new Key(iterator.next())))
                {
                    iterator.remove();
                }
            }
            if (properties.isEmpty() == false)
            {
                sections.put(sample.getIdentifier() + " [" + sample.getSampleType().getCode()
                        + "]:", properties);
            }
        }
        return sections;
    }

    private Map<Key, List<IEntityProperty>> getCommonPropertiesGroups()
    {
        Map<Key, List<IEntityProperty>> keys = new LinkedHashMap<Key, List<IEntityProperty>>();
        for (Sample sample : samples)
        {
            List<IEntityProperty> properties = sample.getProperties();
            for (IEntityProperty property : properties)
            {
                Key key = new Key(property);
                List<IEntityProperty> list = keys.get(key);
                if (list == null)
                {
                    list = new ArrayList<IEntityProperty>();
                    keys.put(key, list);
                }
                list.add(property);
            }
        }
        return keys;
    }
}
