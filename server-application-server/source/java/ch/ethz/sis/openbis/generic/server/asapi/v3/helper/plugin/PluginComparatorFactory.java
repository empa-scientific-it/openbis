/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.plugin;

import java.util.Comparator;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortParameter;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.Plugin;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.fetchoptions.PluginSortOptions;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sort.AbstractStringComparator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sort.ComparatorFactory;

/**
 * @author Franz-Josef Elmer
 */
public class PluginComparatorFactory extends ComparatorFactory
{
    private static final Comparator<Plugin> NAME_COMPARATOR = new AbstractStringComparator<Plugin>()
        {
            @Override
            protected String getValue(Plugin plugin)
            {
                return plugin.getName();
            }
        };

    @Override
    public boolean accepts(Class<?> sortOptionsClass)
    {
        return PluginSortOptions.class.isAssignableFrom(sortOptionsClass);
    }

    @Override
    public Comparator<Plugin> getComparator(String field, Map<SortParameter, String> parameters, ISearchCriteria criteria)
    {
        if (PluginSortOptions.NAME.equals(field))
        {
            return NAME_COMPARATOR;
        } else
        {
            return null;
        }
    }

    @Override
    public Comparator<Plugin> getDefaultComparator()
    {
        return NAME_COMPARATOR;
    }

}
