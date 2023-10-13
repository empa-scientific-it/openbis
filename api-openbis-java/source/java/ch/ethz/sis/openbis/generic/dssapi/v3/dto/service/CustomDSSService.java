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

package ch.ethz.sis.openbis.generic.dssapi.v3.dto.service;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ILabelHolder;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.service.fetchoptions.CustomDSSServiceFetchOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.service.id.CustomDssServiceCode;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonObject("dss.dto.service.CustomDSSService")
public class CustomDSSService implements ILabelHolder, Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private CustomDSSServiceFetchOptions fetchOptions;

    @JsonProperty
    private CustomDssServiceCode code;

    @JsonProperty
    private String label;

    @JsonProperty
    private String description;

    @JsonIgnore
    public CustomDSSServiceFetchOptions getFetchOptions()
    {
        return fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public void setFetchOptions(CustomDSSServiceFetchOptions fetchOptions)
    {
        this.fetchOptions = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public CustomDssServiceCode getCode()
    {
        return code;
    }

    // Method automatically generated with DtoGenerator
    public void setCode(CustomDssServiceCode code)
    {
        this.code = code;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    @Override
    public String getLabel()
    {
        return label;
    }

    // Method automatically generated with DtoGenerator
    public void setLabel(String label)
    {
        this.label = label;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public String getDescription()
    {
        return description;
    }

    // Method automatically generated with DtoGenerator
    public void setDescription(String description)
    {
        this.description = description;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public String toString()
    {
        return "CustomDssService: " + code;
    }
}
