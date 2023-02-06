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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.service;

import java.io.Serializable;
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ILabelHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.INameHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPermIdHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.fetchoptions.ProcessingServiceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.id.DssServicePermId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author Franz-Josef Elmer
 */
@JsonObject("as.dto.service.ProcessingService")
public class ProcessingService implements INameHolder, ILabelHolder, IPermIdHolder, Serializable
{
    private static final long serialVersionUID = 1L;

    private ProcessingServiceFetchOptions fetchOptions;

    private DssServicePermId permId;

    private String name;

    private String label;

    private List<String> dataSetTypeCodes;

    public ProcessingServiceFetchOptions getFetchOptions()
    {
        return fetchOptions;
    }

    public void setFetchOptions(ProcessingServiceFetchOptions fetchOptions)
    {
        this.fetchOptions = fetchOptions;
    }

    @Override
    public DssServicePermId getPermId()
    {
        return permId;
    }

    public void setPermId(DssServicePermId permId)
    {
        this.permId = permId;
    }

    @Override
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public List<String> getDataSetTypeCodes()
    {
        return dataSetTypeCodes;
    }

    public void setDataSetTypeCodes(List<String> dataSetTypeCodes)
    {
        this.dataSetTypeCodes = dataSetTypeCodes;
    }

    @Override
    public String toString()
    {
        return "ProcessingService: " + permId;
    }
}
