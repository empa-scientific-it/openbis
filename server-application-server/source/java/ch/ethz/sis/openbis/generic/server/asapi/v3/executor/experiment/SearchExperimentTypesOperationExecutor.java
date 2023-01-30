/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.experiment;

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ExperimentTypeSearchManager;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ILocalSearchManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.SearchExperimentTypesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.SearchExperimentTypesOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.ISearchObjectExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.SearchObjectsPEOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.experiment.IExperimentTypeTranslator;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;

/**
 * @author pkupczyk
 */
@Component
public class SearchExperimentTypesOperationExecutor
        extends SearchObjectsPEOperationExecutor<ExperimentType, ExperimentTypePE, ExperimentTypeSearchCriteria, ExperimentTypeFetchOptions>
        implements ISearchExperimentTypesOperationExecutor
{

    @Autowired
    private ISearchExperimentTypeExecutor searchExecutor;

    @Autowired
    private IExperimentTypeTranslator translator;

    @Autowired
    private ExperimentTypeSearchManager experimentTypeSearchManager;

    @Override
    protected Class<? extends SearchObjectsOperation<ExperimentTypeSearchCriteria, ExperimentTypeFetchOptions>> getOperationClass()
    {
        return SearchExperimentTypesOperation.class;
    }

    @Override
    protected ISearchObjectExecutor<ExperimentTypeSearchCriteria, ExperimentTypePE> getExecutor()
    {
        return searchExecutor;
    }

    @Override
    protected ITranslator<Long, ExperimentType, ExperimentTypeFetchOptions> getTranslator()
    {
        return translator;
    }

    @Override
    protected SearchObjectsOperationResult<ExperimentType> getOperationResult(final SearchResult<ExperimentType> searchResult)
    {
        return new SearchExperimentTypesOperationResult(searchResult);
    }

    @Override
    protected SearchObjectsOperationResult<ExperimentType> doExecute(final IOperationContext context,
            final SearchObjectsOperation<ExperimentTypeSearchCriteria, ExperimentTypeFetchOptions> operation)
    {
        return executeDirectSQLSearch(context, operation);
    }

    @Override
    protected ILocalSearchManager<ExperimentTypeSearchCriteria, ExperimentType, Long> getSearchManager() {
        return experimentTypeSearchManager;
    }

}
