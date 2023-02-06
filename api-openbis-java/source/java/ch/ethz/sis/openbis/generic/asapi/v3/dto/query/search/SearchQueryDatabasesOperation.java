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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.query.search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.fetchoptions.QueryDatabaseFetchOptions;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.query.search.SearchQueryDatabasesOperation")
public class SearchQueryDatabasesOperation extends SearchObjectsOperation<QueryDatabaseSearchCriteria, QueryDatabaseFetchOptions>
{

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private SearchQueryDatabasesOperation()
    {
    }

    public SearchQueryDatabasesOperation(QueryDatabaseSearchCriteria criteria, QueryDatabaseFetchOptions fetchOptions)
    {
        super(criteria, fetchOptions);
    }

}
