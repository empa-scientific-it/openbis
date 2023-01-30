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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.plugin;

import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ILocalSearchManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.Plugin;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.fetchoptions.PluginFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.search.PluginSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.search.SearchPluginsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.search.SearchPluginsOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.ISearchObjectExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.SearchObjectsPEOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.plugin.IPluginTranslator;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class SearchPluginsOperationExecutor
        extends SearchObjectsPEOperationExecutor<Plugin, ScriptPE, PluginSearchCriteria, PluginFetchOptions>
        implements ISearchPluginsOperationExecutor
{
    @Autowired 
    private ISearchPluginExecutor searchExecutor;
    
    @Autowired
    private IPluginTranslator translator;

    @Override
    protected ISearchObjectExecutor<PluginSearchCriteria, ScriptPE> getExecutor()
    {
        return searchExecutor;
    }

    @Override
    protected ITranslator<Long, Plugin, PluginFetchOptions> getTranslator()
    {
        return translator;
    }

    @Override
    protected SearchObjectsOperationResult<Plugin> getOperationResult(SearchResult<Plugin> searchResult)
    {
        return new SearchPluginsOperationResult(searchResult);
    }

    @Override
    protected ILocalSearchManager<PluginSearchCriteria, Plugin, Long> getSearchManager() {
        throw new RuntimeException("This method is not implemented yet.");
    }

    @Override
    protected Class<? extends SearchObjectsOperation<PluginSearchCriteria, PluginFetchOptions>> getOperationClass()
    {
        return SearchPluginsOperation.class;
    }

}
