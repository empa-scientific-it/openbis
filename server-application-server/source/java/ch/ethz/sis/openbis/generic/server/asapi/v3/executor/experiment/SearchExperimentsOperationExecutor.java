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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.SearchExperimentsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.SearchExperimentsOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.ISearchObjectExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.SearchObjectsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ExperimentSearchManager;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ILocalSearchManager;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.experiment.IExperimentTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author pkupczyk
 */
@Component
public class SearchExperimentsOperationExecutor
        extends SearchObjectsOperationExecutor<Experiment, Long, ExperimentSearchCriteria, ExperimentFetchOptions>
        implements ISearchExperimentsOperationExecutor
{

    @Autowired
    private ISearchExperimentExecutor searchExecutor;

    @Autowired
    private IExperimentTranslator translator;

    @Autowired
    private ExperimentSearchManager experimentSearchManager;

    @Override
    protected Class<? extends SearchObjectsOperation<ExperimentSearchCriteria, ExperimentFetchOptions>> getOperationClass()
    {
        return SearchExperimentsOperation.class;
    }

    @Override
    protected ISearchObjectExecutor<ExperimentSearchCriteria, Long> getExecutor()
    {
        return searchExecutor;
    }

    @Override
    protected ITranslator<Long, Experiment, ExperimentFetchOptions> getTranslator()
    {
        return translator;
    }

    @Override
    protected ILocalSearchManager<ExperimentSearchCriteria, Experiment, Long> getSearchManager()
    {
        return experimentSearchManager;
    }

    @Override
    protected SearchObjectsOperationResult<Experiment> getOperationResult(SearchResult<Experiment> searchResult)
    {
        return new SearchExperimentsOperationResult(searchResult);
    }

    @Override
    protected SearchObjectsOperationResult<Experiment> doExecute(IOperationContext context, SearchObjectsOperation<ExperimentSearchCriteria, ExperimentFetchOptions> operation)
    {
        return executeDirectSQLSearch(context, operation);
    }
}
