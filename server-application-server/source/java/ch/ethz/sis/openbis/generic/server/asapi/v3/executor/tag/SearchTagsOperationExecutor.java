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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.tag;

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ILocalSearchManager;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.TagSearchManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.Tag;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.fetchoptions.TagFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.search.SearchTagsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.search.SearchTagsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.search.TagSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.ISearchObjectExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.SearchObjectsPEOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.tag.ITagTranslator;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;

/**
 * @author pkupczyk
 */
@Component
public class SearchTagsOperationExecutor extends SearchObjectsPEOperationExecutor<Tag, MetaprojectPE, TagSearchCriteria, TagFetchOptions>
        implements ISearchTagsOperationExecutor
{

    @Autowired
    private ISearchTagExecutor searchExecutor;

    @Autowired
    private ITagTranslator translator;

    @Autowired
    private TagSearchManager tagSearchManager;

    @Override
    protected Class<? extends SearchObjectsOperation<TagSearchCriteria, TagFetchOptions>> getOperationClass()
    {
        return SearchTagsOperation.class;
    }

    @Override
    protected ISearchObjectExecutor<TagSearchCriteria, MetaprojectPE> getExecutor()
    {
        return searchExecutor;
    }

    @Override
    protected ITranslator<Long, Tag, TagFetchOptions> getTranslator()
    {
        return translator;
    }

    @Override
    protected SearchObjectsOperationResult<Tag> getOperationResult(SearchResult<Tag> searchResult)
    {
        return new SearchTagsOperationResult(searchResult);
    }

    @Override
    protected SearchObjectsOperationResult<Tag> doExecute(final IOperationContext context,
            final SearchObjectsOperation<TagSearchCriteria, TagFetchOptions> operation)
    {
        return executeDirectSQLSearch(context, operation);
    }

    @Override
    protected ILocalSearchManager<TagSearchCriteria, Tag, Long> getSearchManager()
    {
        return tagSearchManager;
    }

}
