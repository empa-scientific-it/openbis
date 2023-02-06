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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.LinkedDataFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.ExternalDms;
import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.common.annotation.TechPreview;

/*
 * Class automatically generated with DtoGenerator
 */
@JsonObject("as.dto.dataset.LinkedData")
public class LinkedData implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private LinkedDataFetchOptions fetchOptions;

    /**
     * @deprecated Use {@link ContentCopy} instead.
     */
    @Deprecated
    @JsonProperty
    private String externalCode;

    /**
     * @deprecated Use {@link ContentCopy} instead.
     */
    @Deprecated
    @JsonProperty
    private ExternalDms externalDms;

    @JsonProperty
    private List<ContentCopy> contentCopies;

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public LinkedDataFetchOptions getFetchOptions()
    {
        return fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public void setFetchOptions(LinkedDataFetchOptions fetchOptions)
    {
        this.fetchOptions = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public String getExternalCode()
    {
        return externalCode;
    }

    // Method automatically generated with DtoGenerator
    public void setExternalCode(String externalCode)
    {
        this.externalCode = externalCode;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public ExternalDms getExternalDms()
    {
        return externalDms;
    }

    // Method automatically generated with DtoGenerator
    public void setExternalDms(ExternalDms externalDms)
    {
        this.externalDms = externalDms;
    }

    @JsonIgnore
    public List<ContentCopy> getContentCopies()
    {
        return contentCopies;
    }

    public void setContentCopies(List<ContentCopy> contentCopies)
    {
        this.contentCopies = contentCopies;
    }

    // Method automatically generated with DtoGenerator
    @Override
    public String toString()
    {
        return "LinkedData " + externalCode;
    }

}
