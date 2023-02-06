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
package ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fetchoptions;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptionsToStringBuilder;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.DataSetFile;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("dss.dto.datasetfile.fetchoptions.DataSetFileFetchOptions")
public class DataSetFileFetchOptions extends FetchOptions<DataSetFile> implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private DataSetFileSortOptions sort;

    @Override
    public DataSetFileSortOptions sortBy()
    {
        if (sort == null)
        {
            sort = new DataSetFileSortOptions();
        }
        return sort;
    }

    @Override
    @JsonIgnore
    public DataSetFileSortOptions getSortBy()
    {
        return sort;
    }

    @Override
    protected FetchOptionsToStringBuilder getFetchOptionsStringBuilder()
    {
        return new FetchOptionsToStringBuilder("DataSetFile", this);
    }
}
