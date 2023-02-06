/*
 * Copyright 2014 ETH Zuerich, CISD
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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.query.fetchoptions;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptionsToStringBuilder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.query.QueryDatabase;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/*
 * Class automatically generated with DtoGenerator
 */
@JsonObject("as.dto.query.fetchoptions.QueryDatabaseFetchOptions")
public class QueryDatabaseFetchOptions extends FetchOptions<QueryDatabase> implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private SpaceFetchOptions space;

    @JsonProperty
    private QueryDatabaseSortOptions sort;

    // Method automatically generated with DtoGenerator
    public SpaceFetchOptions withSpace()
    {
        if (space == null)
        {
            space = new SpaceFetchOptions();
        }
        return space;
    }

    // Method automatically generated with DtoGenerator
    public SpaceFetchOptions withSpaceUsing(SpaceFetchOptions fetchOptions)
    {
        return space = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public boolean hasSpace()
    {
        return space != null;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public QueryDatabaseSortOptions sortBy()
    {
        if (sort == null)
        {
            sort = new QueryDatabaseSortOptions();
        }
        return sort;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public QueryDatabaseSortOptions getSortBy()
    {
        return sort;
    }
    @Override
    protected FetchOptionsToStringBuilder getFetchOptionsStringBuilder()
    {
        FetchOptionsToStringBuilder f = new FetchOptionsToStringBuilder("QueryDatabase", this);
        f.addFetchOption("Space", space);
        return f;
    }

}
