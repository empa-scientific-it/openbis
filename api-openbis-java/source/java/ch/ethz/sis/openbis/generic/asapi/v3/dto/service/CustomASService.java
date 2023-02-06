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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.service;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ILabelHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.fetchoptions.CustomASServiceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.id.CustomASServiceCode;
import ch.systemsx.cisd.base.annotation.JsonObject;

/*
 * Class automatically generated with DtoGenerator
 */
@JsonObject("as.dto.service.CustomASService")
public class CustomASService implements ILabelHolder, Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private CustomASServiceFetchOptions fetchOptions;

    @JsonProperty
    private CustomASServiceCode code;

    @JsonProperty
    private String label;

    @JsonProperty
    private String description;

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public CustomASServiceFetchOptions getFetchOptions()
    {
        return fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public void setFetchOptions(CustomASServiceFetchOptions fetchOptions)
    {
        this.fetchOptions = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public CustomASServiceCode getCode()
    {
        return code;
    }

    // Method automatically generated with DtoGenerator
    public void setCode(CustomASServiceCode code)
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
        return "CustomASService: " + code;
    }

}
