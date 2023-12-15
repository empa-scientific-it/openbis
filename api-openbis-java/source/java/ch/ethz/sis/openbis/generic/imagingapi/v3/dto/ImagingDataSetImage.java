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

package ch.ethz.sis.openbis.generic.imagingapi.v3.dto;

import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@JsonObject("imaging.dto.ImagingDataSetImage")
public class ImagingDataSetImage implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private List<ImagingDataSetPreview> previews;

    @JsonProperty
    private Map<String, Serializable> config;

    @JsonProperty
    private Integer index;

    @JsonProperty
    private Map<String, Serializable> metadata;

    @JsonIgnore
    public List<ImagingDataSetPreview> getPreviews()
    {
        return previews;
    }

    public void setPreviews(
            List<ImagingDataSetPreview> previews)
    {
        this.previews = previews;
    }

    @JsonIgnore
    public Map<String, Serializable> getConfig()
    {
        return config;
    }

    public void setConfig(Map<String, Serializable> config)
    {
        this.config = config;
    }

    @JsonIgnore
    public Integer getIndex()
    {
        return index;
    }

    public void setIndex(Integer index)
    {
        this.index = index;
    }

    @JsonIgnore
    public Map<String, Serializable> getMetadata()
    {
        return metadata;
    }

    public void setMetadata(Map<String, Serializable> metadata)
    {
        this.metadata = metadata;
    }

    @Override
    public String toString()
    {
        return "ImagingDataSetImage:";
    }

}
