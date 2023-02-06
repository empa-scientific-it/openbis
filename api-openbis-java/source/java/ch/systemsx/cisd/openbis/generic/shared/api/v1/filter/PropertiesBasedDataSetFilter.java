/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.api.v1.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;

/**
 * Filter based on data set properties.
 *
 * @author Franz-Josef Elmer
 */
public class PropertiesBasedDataSetFilter implements IDataSetFilter
{
    private final Map<String, String> map;

    public PropertiesBasedDataSetFilter(Map<String, String> properties)
    {
        map = properties;
    }

    @Override
    public boolean pass(DataSet dataSet)
    {
        HashMap<String, String> properties = dataSet.getProperties();
        Set<Entry<String, String>> entrySet = map.entrySet();
        for (Entry<String, String> entry : entrySet)
        {
            String key = entry.getKey();
            String value = entry.getValue();
            if (value.equals(properties.get(key)) == false)
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString()
    {
        Set<Entry<String, String>> entrySet = map.entrySet();
        List<Entry<String, String>> sortedEntries = new ArrayList<Entry<String, String>>(entrySet);
        Collections.sort(sortedEntries, new Comparator<Entry<String, String>>()
            {
                @Override
                public int compare(Entry<String, String> o1, Entry<String, String> o2)
                {
                    return o1.getKey().compareTo(o2.getKey());
                }
            });
        return "Properties:" + sortedEntries;
    }

}
