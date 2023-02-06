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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractObjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.IdsSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.NameSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchCriteriaToStringBuilder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchOperator;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.IPluginId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author Franz-Josef Elmer
 *
 */
@JsonObject("as.dto.plugin.search.PluginSearchCriteria")
public class PluginSearchCriteria extends AbstractObjectSearchCriteria<IPluginId>
{

    private static final long serialVersionUID = 1L;

    public IdsSearchCriteria<IPluginId> withIds()
    {
        return with(new IdsSearchCriteria<IPluginId>());
    }

    public NameSearchCriteria withName()
    {
        return with(new NameSearchCriteria());
    }
    
    public PluginTypeSearchCriteria withPluginType()
    {
        return with(new PluginTypeSearchCriteria());
    }
    
    public PluginKindSearchCriteria withPluginKind()
    {
        return with(new PluginKindSearchCriteria());
    }
    
    public PluginSearchCriteria withOrOperator()
    {
        return (PluginSearchCriteria) withOperator(SearchOperator.OR);
    }

    public PluginSearchCriteria withAndOperator()
    {
        return (PluginSearchCriteria) withOperator(SearchOperator.AND);
    }

    @Override
    protected SearchCriteriaToStringBuilder createBuilder()
    {
        SearchCriteriaToStringBuilder builder = super.createBuilder();
        builder.setName("PLUGIN");
        return builder;
    }
}
