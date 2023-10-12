/*
 *  Copyright ETH 2023 Zürich, Scientific IT Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package ch.ethz.sis.openbis.generic.dssapi.v3.dto.service.fetchoptions;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptionsToStringBuilder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.service.CustomDSSService;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonObject("dss.dto.service.fetchoptions.CustomDSSServiceFetchOptions")
public class CustomDSSServiceFetchOptions extends FetchOptions<CustomDSSService> implements
        Serializable
{

    @JsonProperty
    private CustomDSSServiceSortOptions sort;

    @Override
    public SortOptions<CustomDSSService> sortBy()
    {
        if (sort == null)
        {
            sort = new CustomDSSServiceSortOptions();
        }
        return sort;
    }

    @Override
    public SortOptions<CustomDSSService> getSortBy()
    {
        return sort;
    }

    @Override
    protected FetchOptionsToStringBuilder getFetchOptionsStringBuilder()
    {
        FetchOptionsToStringBuilder f = new FetchOptionsToStringBuilder("CustomDSSService", this);
        return f;
    }
}
